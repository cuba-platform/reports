/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.global.ViewRepository;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportTemplate;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;

/**
 * @author degtyarjov
 * @version $Id$
 */

@ManagedBean
public class ReportImportExport {
    protected static final String ENCODING = "CP866";

    @Inject
    protected ReportingApi reportingApi;

    @Inject
    protected ViewRepository viewRepository;

    @Inject
    protected Persistence persistence;

    public byte[] exportReports(Collection<Report> reports) throws IOException, FileStorageException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream);
        zipOutputStream.setMethod(ZipArchiveOutputStream.STORED);
        zipOutputStream.setEncoding(ENCODING);
        for (Report report : reports) {
            try {
                byte[] reportBytes = exportReport(report);
                ArchiveEntry singleReportEntry = newStoredEntry(replaceForbiddenCharacters(report.getName()) + ".zip", reportBytes);
                zipOutputStream.putArchiveEntry(singleReportEntry);
                zipOutputStream.write(reportBytes);
                zipOutputStream.closeArchiveEntry();
            } catch (Exception ex) {
                throw new RuntimeException("Exception occured while exporting report\"" + report.getName() + "\".", ex);
            }
        }
        zipOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Exports single report to ZIP archive with name <report name>.zip.
     * There are 2 files in archive: report.xml and a template file (odt, xls or other..)
     *
     * @param report Report object that must be exported.
     * @return ZIP archive as a byte array.
     * @throws IOException
     * @throws FileStorageException
     */
    private byte[] exportReport(Report report) throws IOException, FileStorageException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream);
        zipOutputStream.setMethod(ZipArchiveOutputStream.STORED);
        zipOutputStream.setEncoding(ENCODING);


        report = reloadReport(report);

        String xml = report.getXml();
        byte[] xmlBytes = xml.getBytes();
        ArchiveEntry zipEntryReportObject = newStoredEntry("report.xml", xmlBytes);
        zipOutputStream.putArchiveEntry(zipEntryReportObject);
        zipOutputStream.write(xmlBytes);

        if (report.getTemplates() != null) {
            for (int i = 0; i < report.getTemplates().size(); i++) {
                ReportTemplate template = report.getTemplates().get(i);

                if (template.getContent() != null) {
                    byte[] fileBytes = template.getContent();
                    ArchiveEntry zipEntryTemplate = newStoredEntry(
                            "templates/" + Integer.toString(i) + "/" + template.getName(),
                            fileBytes);
                    zipOutputStream.putArchiveEntry(zipEntryTemplate);
                    zipOutputStream.write(fileBytes);
                }
            }
        }

        zipOutputStream.closeArchiveEntry();
        zipOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public Collection<Report> importReports(byte[] zipBytes) throws IOException, FileStorageException {
        LinkedList<Report> reports = null;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);
        ZipArchiveInputStream archiveReader;
        archiveReader = new ZipArchiveInputStream(byteArrayInputStream);
        while (archiveReader.getNextZipEntry() != null) {
            if (reports == null) {
                reports = new LinkedList<>();
            }
            final byte[] buffer = readBytesFromEntry(archiveReader);
            Report report = importReport(buffer);
            reports.add(report);
        }
        byteArrayInputStream.close();
        return reports;
    }

    private Report importReport(byte[] zipBytes) throws IOException, FileStorageException {
        Report report = null;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);
        ZipArchiveInputStream archiveReader;
        archiveReader = new ZipArchiveInputStream(byteArrayInputStream);
        ZipArchiveEntry archiveEntry;
        // importing report.xml to report object
        while (((archiveEntry = archiveReader.getNextZipEntry()) != null) && (report == null)) {
            if (archiveEntry.getName().equals("report.xml")) {
                String xml = new String(readBytesFromEntry(archiveReader));
                report = reportingApi.convertToReport(xml);
                report.setXml(xml);
            }
        }

        byteArrayInputStream.close();

        // importring template files
        // not using zipInputStream.reset here because marks not supported.
        byteArrayInputStream = new ByteArrayInputStream(zipBytes);
        archiveReader = new ZipArchiveInputStream(byteArrayInputStream);

        if ((report != null) && (report.getTemplates() != null)) {
            // unpack templates
            int i = 0;
            while ((archiveEntry = archiveReader.getNextZipEntry()) != null
                    && (i < report.getTemplates().size())) {

                if (!archiveEntry.getName().equals("report.xml") && !archiveEntry.isDirectory()) {
                    String[] namePaths = archiveEntry.getName().split("/");
                    int index = Integer.parseInt(namePaths[1]);

                    if (index >= 0) {
                        ReportTemplate template = report.getTemplates().get(index);
                        template.setContent(readBytesFromEntry(archiveReader));
                    }
                    i++;
                }
            }
        }
        byteArrayInputStream.close();

        Persistence persistence = AppBeans.get(Persistence.class);

        if (report != null) {
            Transaction tx = persistence.createTransaction();
            try {
                EntityManager em = persistence.getEntityManager();
                ReportTemplate defaultTemplate = report.getDefaultTemplate();
                List<ReportTemplate> loadedTemplates = report.getTemplates();

                report.setDefaultTemplate(null);
                report.setTemplates(null);
                report = em.merge(report);

                List<ReportTemplate> reportTemplates = new ArrayList<>();
                for (ReportTemplate reportTemplate : loadedTemplates) {
                    reportTemplate.setReport(report);
                    ReportTemplate merged = em.merge(reportTemplate);
                    reportTemplates.add(merged);
                    if (merged.equals(defaultTemplate)) {
                        report.setDefaultTemplate(merged);
                    }
                }
                report.setTemplates(reportTemplates);

                tx.commit();
            } finally {
                tx.end();
            }
        }

        return report;
    }

    private ArchiveEntry newStoredEntry(String name, byte[] data) {
        ZipArchiveEntry zipEntry = new ZipArchiveEntry(name);
        zipEntry.setSize(data.length);
        zipEntry.setCompressedSize(zipEntry.getSize());
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        zipEntry.setCrc(crc32.getValue());
        return zipEntry;
    }

    private String replaceForbiddenCharacters(String fileName) {
        return fileName.replaceAll("[\\,/,:,\\*,\",<,>,\\|]", "");
    }

    private byte[] readBytesFromEntry(ZipArchiveInputStream archiveReader) throws IOException {
        return IOUtils.toByteArray(archiveReader);
    }

    private Report reloadReport(Report report) {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            View exportView = viewRepository.getView(report.getClass(), ReportingBean.REPORT_EDIT_VIEW_NAME);
            report = em.find(Report.class, report.getId(), exportView);
            em.fetch(report, exportView);
            tx.commit();
            return report;
        } finally {
            tx.end();
        }
    }
}
