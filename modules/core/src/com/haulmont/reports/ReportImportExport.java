/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.global.ViewRepository;
import com.haulmont.cuba.security.app.Authenticated;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.*;
import com.haulmont.reports.exception.ReportingException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.ExternalizableConverter;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.zip.CRC32;

/**
 * @author degtyarjov
 * @version $Id$
 */
@ManagedBean(ReportImportExportAPI.NAME)
public class ReportImportExport implements ReportImportExportAPI, ReportImportExportMBean {
    public static final String ENCODING = "CP866";

    @Inject
    protected ReportingApi reportingApi;

    @Inject
    protected ViewRepository viewRepository;

    @Inject
    protected Persistence persistence;

    public byte[] exportReports(Collection<Report> reports){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream);
        try {
            zipOutputStream.setMethod(ZipArchiveOutputStream.STORED);
            zipOutputStream.setEncoding(ENCODING);
            for (Report report : reports) {
                try {
                    byte[] reportBytes = exportReport(report);
                    ArchiveEntry singleReportEntry = newStoredEntry(replaceForbiddenCharacters(report.getName()) + ".zip", reportBytes);
                    zipOutputStream.putArchiveEntry(singleReportEntry);
                    zipOutputStream.write(reportBytes);
                    zipOutputStream.closeArchiveEntry();
                } catch (IOException e) {
                    throw new ReportingException(String.format("Exception occurred while exporting report [%s]", report.getName()), e);
                }
            }
        } finally {
            IOUtils.closeQuietly(zipOutputStream);
        }

        return byteArrayOutputStream.toByteArray();
    }

    public Collection<Report> importReports(byte[] zipBytes) {
        LinkedList<Report> reports = null;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);
        ZipArchiveInputStream archiveReader = new ZipArchiveInputStream(byteArrayInputStream);
        try {
            try {
                while (archiveReader.getNextZipEntry() != null) {
                    if (reports == null) {
                        reports = new LinkedList<>();
                    }
                    final byte[] buffer = readBytesFromEntry(archiveReader);
                    Report report = importReport(buffer);
                    reports.add(report);
                }
            } catch (IOException e) {
                throw new ReportingException("Exception occurred while importing report", e);
            }
        } finally {
            IOUtils.closeQuietly(byteArrayInputStream);
        }

        if (reports == null) {
            throw new ReportingException("Unable to import reports because correct data not found in the archive");
        }

        return reports;
    }

    /**
     * Deploys report from folder
     * Folder should have the following structure, in other cases RuntimeException will be thrown
     * <p/>
     * folder
     * sub-folder1
     * report.xml
     * template.doc
     * sub-folder2
     * report.xml
     * template.docx
     *
     * @param path to folder with reports
     * @return status
     * @throws IOException
     */
    @Authenticated
    public String deployAllReportsFromPath(String path) throws IOException {
        File directory = new File(path);
        if (directory.exists() && directory.isDirectory()) {
            File[] subDirectories = directory.listFiles();
            if (subDirectories != null) {
                Map<String, Object> map = new HashMap<>();

                for (File subDirectory : subDirectories) {
                    if (subDirectory.isDirectory()) {
                        if (!subDirectory.getName().startsWith(".")) {
                            File[] files = subDirectory.listFiles();
                            if (files != null) {
                                byte[] bytes = zipSingleReportFiles(files);
                                String name = replaceForbiddenCharacters(subDirectory.getName()) + ".zip";
                                map.put(name, bytes);
                            }
                        }
                    } else {
                        throw new ReportingException("Report deployment failed. Root folder should have special structure.");
                    }
                }
                importReports(zipContent(map));
                return String.format("%d reports deployed", map.size());
            }
        }

        return "No reports deployed.";
    }

    /**
     * Exports single report to ZIP archive with name <report name>.zip.
     * There are 2 files in archive: report.xml and a template file (odt, xls or other..)
     *
     * @param report Report object that must be exported.
     * @return ZIP archive as a byte array.
     * @throws IOException
     */
    protected byte[] exportReport(Report report) throws IOException {
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
                            "templates/" + i + "/" + template.getName(), fileBytes);
                    zipOutputStream.putArchiveEntry(zipEntryTemplate);
                    zipOutputStream.write(fileBytes);
                }
            }
        }

        zipOutputStream.closeArchiveEntry();
        zipOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }


    protected Report importReport(byte[] zipBytes) throws IOException {
        Report report = null;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);
        ZipArchiveInputStream archiveReader;
        archiveReader = new ZipArchiveInputStream(byteArrayInputStream);
        ZipArchiveEntry archiveEntry;
        // importing report.xml to report object
        while (((archiveEntry = archiveReader.getNextZipEntry()) != null) && (report == null)) {
            if (archiveEntry.getName().equals("report.xml")) {
                String xml = new String(readBytesFromEntry(archiveReader));
                if (xml.startsWith("<Report>")) {//old versions
                    report = legacyReportsFromXml(Report.class, xml);
                    if (report.getDefaultTemplate() == null) {
                        report.setDefaultTemplate(report.getTemplateByCode(ReportService.DEFAULT_TEMPLATE_CODE));
                    }

                    if (report.getBands() == null) {
                        report.setBands(new HashSet<BandDefinition>());
                    }
                    collectBands(report.getRootBandDefinition(), report.getBands());
                    report.setXml(reportingApi.convertToXml(report));
                } else {
                    report = reportingApi.convertToReport(xml);
                    report.setXml(xml);
                }
            }
        }

        byteArrayInputStream.close();

        if (report == null) {
            throw new ReportingException("Unable to convert data from archive to report");
        }

        // importring template files
        // not using zipInputStream.reset here because marks not supported.
        byteArrayInputStream = new ByteArrayInputStream(zipBytes);
        archiveReader = new ZipArchiveInputStream(byteArrayInputStream);

        if (report.getTemplates() != null) {
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
                        if (StringUtils.isBlank(template.getName())) {
                            template.setName(namePaths[2]);
                        }
                    }
                    i++;
                }
            }
        }
        byteArrayInputStream.close();

        report = saveReport(report);
        return report;
    }

    protected Report saveReport(Report report) {
        Persistence persistence = AppBeans.get(Persistence.class);
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            ReportTemplate defaultTemplate = report.getDefaultTemplate();
            List<ReportTemplate> loadedTemplates = report.getTemplates();

            report.setDefaultTemplate(null);
            report.setTemplates(null);
            if (report.getGroup() != null) {
                ReportGroup reportGroup = em.merge(report.getGroup());
                report.setGroup(reportGroup);
            }
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
        return report;
    }

    protected byte[] zipSingleReportFiles(File[] files) throws IOException {
        Map<String, Object> map = new HashMap<>();
        int templatesCount = 0;
        for (File file : files) {
            if (!file.isDirectory()) {
                byte[] data = FileUtils.readFileToByteArray(file);
                String name;
                if (file.getName().endsWith(".xml")) {
                    name = file.getName();
                } else {
                    name = "templates/" + templatesCount++ + "/" + file.getName();
                }

                map.put(name, data);
            }
        }

        return zipContent(map);
    }

    protected byte[] zipContent(Map<String, Object> stringObjectMap) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream);
        zipOutputStream.setMethod(ZipArchiveOutputStream.STORED);
        zipOutputStream.setEncoding(ENCODING);

        for (Map.Entry<String, Object> entry : stringObjectMap.entrySet()) {
            byte[] data = (byte[]) entry.getValue();
            ArchiveEntry archiveEntry = newStoredEntry(entry.getKey(), data);
            zipOutputStream.putArchiveEntry(archiveEntry);
            zipOutputStream.write(data);
            zipOutputStream.closeArchiveEntry();
        }

        zipOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    protected ArchiveEntry newStoredEntry(String name, byte[] data) {
        ZipArchiveEntry zipEntry = new ZipArchiveEntry(name);
        zipEntry.setSize(data.length);
        zipEntry.setCompressedSize(zipEntry.getSize());
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        zipEntry.setCrc(crc32.getValue());
        return zipEntry;
    }

    protected String replaceForbiddenCharacters(String fileName) {
        return fileName.replaceAll("[\\,/,:,\\*,\",<,>,\\|]", "");
    }

    protected byte[] readBytesFromEntry(ZipArchiveInputStream archiveReader) throws IOException {
        return IOUtils.toByteArray(archiveReader);
    }

    protected Report reloadReport(Report report) {
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

    protected <T> T legacyReportsFromXml(Class clazz, String xml) {
        XStream xStream = createLegacyReportsXStream(clazz);
        Object o = xStream.fromXML(xml);
        return (T) o;
    }

    protected XStream createLegacyReportsXStream(Class clazz) {
        XStream xStream = new XStream() {
            @Override
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {
                    @Override
                    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                        if (definedIn == Object.class) {
                            return false;
                        }
                        return super.shouldSerializeMember(definedIn, fieldName);
                    }
                };
            }
        };

        xStream.aliasField("id", BandDefinition.class, "uuid");
        xStream.aliasField("id", DataSet.class, "uuid");
        xStream.aliasField("id", ReportInputParameter.class, "uuid");
        xStream.aliasField("id", ReportValueFormat.class, "uuid");
        xStream.aliasField("id", ReportScreen.class, "uuid");

        xStream.alias("com.haulmont.cuba.report.DataSet", DataSet.class);
        xStream.alias("com.haulmont.cuba.report.ReportTemplate", ReportTemplate.class);
        xStream.alias("com.haulmont.cuba.report.ReportInputParameter", ReportInputParameter.class);
        xStream.alias("com.haulmont.cuba.report.ReportValueFormat", ReportValueFormat.class);
        xStream.alias("com.haulmont.cuba.report.BandDefinition", BandDefinition.class);
        xStream.alias("com.haulmont.cuba.report.ReportGroup", ReportGroup.class);
        xStream.alias("com.haulmont.cuba.report.ReportScreen", ReportScreen.class);
        xStream.omitField(Report.class, "roles");

        xStream.getConverterRegistry().removeConverter(ExternalizableConverter.class);
        xStream.alias(clazz.getSimpleName(), clazz);
        for (Field field : clazz.getDeclaredFields()) {
            Class cl = field.getType();
            xStream.alias(cl.getSimpleName(), cl);
        }

        return xStream;
    }

    protected void collectBands(BandDefinition band, Set<BandDefinition> bands) {
        bands.add(band);

        if (band.getChildrenBandDefinitions() != null) {
            for (BandDefinition childBand : band.getChildrenBandDefinitions()) {
                collectBands(childBand, bands);
            }
        }
    }
}
