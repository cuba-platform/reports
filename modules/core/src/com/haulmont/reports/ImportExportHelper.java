/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports;

import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.app.FileStorageAPI;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.*;
import com.haulmont.reports.entity.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.ExternalizableConverter;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.zip.CRC32;

/**
 * TODO Rewrite as API without static methods
 *
 * @author fontanenko
 * @version $Id$
 */
public class ImportExportHelper {
    protected static final String ENCODING = "CP866";

    private static Log log = LogFactory.getLog(ImportExportHelper.class);

    public static byte[] loadAndCompress(Collection<FileDescriptor> files) throws IOException, FileStorageException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream);
        zipOutputStream.setMethod(ZipArchiveOutputStream.DEFLATED);
        zipOutputStream.setEncoding(ENCODING);

        FileStorageAPI fileStorage = AppBeans.get(FileStorageAPI.NAME);
        for (FileDescriptor fileDescriptor : files) {
            byte[] bytes = fileStorage.loadFile(fileDescriptor);
            ArchiveEntry singleReportEntry = newStoredEntry(replaceForbiddenCharacters(fileDescriptor.getName()), bytes);
            zipOutputStream.putArchiveEntry(singleReportEntry);
            zipOutputStream.write(bytes);
            zipOutputStream.closeArchiveEntry();
        }
        zipOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] exportReports(Collection<Report> reports) throws IOException, FileStorageException {
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
     * Exports single report to ZIP archive whith name <report name>.zip.
     * There are 2 files in archive: report.xml and a template file (odt, xls or other..)
     *
     * @param report Report object that must be exported.
     * @return ZIP archive as a byte array.
     * @throws IOException
     * @throws FileStorageException
     */
    private static byte[] exportReport(Report report) throws IOException, FileStorageException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream);
        zipOutputStream.setMethod(ZipArchiveOutputStream.STORED);
        zipOutputStream.setEncoding(ENCODING);
        report = ((ReportingApi) AppBeans.get(ReportingApi.NAME)).reloadReport(report);
        String xml = toXML(report);
        byte[] xmlBytes = xml.getBytes();
        ArchiveEntry zipEntryReportObject = newStoredEntry("report.xml", xmlBytes);
        zipOutputStream.putArchiveEntry(zipEntryReportObject);
        zipOutputStream.write(xmlBytes);

        if (report.getTemplates() != null) {
            for (int i = 0; i < report.getTemplates().size(); i++) {
                ReportTemplate template = report.getTemplates().get(i);

                FileDescriptor fd = template.getTemplateFileDescriptor();
                if (fd != null) {
                    FileStorageAPI mbean = AppBeans.get(FileStorageAPI.NAME);
                    byte[] fileBytes = mbean.loadFile(fd);
                    ArchiveEntry zipEntryTemplate = newStoredEntry(
                            "templates/" + Integer.toString(i) + "/" + fd.getName(),
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

    private static ArchiveEntry newStoredEntry(String name, byte[] data) {
        ZipArchiveEntry zipEntry = new ZipArchiveEntry(name);
        zipEntry.setSize(data.length);
        zipEntry.setCompressedSize(zipEntry.getSize());
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        zipEntry.setCrc(crc32.getValue());
        return zipEntry;
    }

    private static String replaceForbiddenCharacters(String fileName) {
        return fileName.replaceAll("[\\,/,:,\\*,\",<,>,\\|]", "");
    }

    private static String toXML(Object o) {
        XStream xStream = createXStream(o.getClass());
        return xStream.toXML(o);
    }

    private static <T> T fromXML(Class clazz, String xml) {
        XStream xStream = createXStream(clazz);
        Object o = xStream.fromXML(xml);
        return (T) o;
    }

    private static XStream createXStream(Class clazz) {
        XStream xStream = new XStream();
        xStream.getConverterRegistry().removeConverter(ExternalizableConverter.class);
        xStream.alias(clazz.getSimpleName(), clazz);
        // todo: reimplement - use recursion
        for (Field field : clazz.getDeclaredFields()) {
            Class cl = field.getType();
            xStream.alias(cl.getSimpleName(), cl);
        }
        return xStream;
    }

    private static void addAlias(XStream xStream, Class clazz, HashSet<Class> knownClasses) {
        if (!knownClasses.contains(clazz) && Entity.class.isAssignableFrom(clazz)) {
            xStream.alias(clazz.getSimpleName(), clazz);
            knownClasses.add(clazz);
            for (Field field : clazz.getDeclaredFields()) {
                addAlias(xStream, field.getType(), knownClasses);
            }
            for (Field field : clazz.getFields()) {
                addAlias(xStream, field.getType(), knownClasses);
            }
        }
    }

    public static Collection<Report> importReports(byte[] zipBytes) throws IOException, FileStorageException {
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

    private static byte[] readBytesFromEntry(ZipArchiveInputStream archiveReader) throws IOException {
        return IOUtils.toByteArray(archiveReader);
    }

    private static Report importReport(byte[] zipBytes) throws IOException, FileStorageException {
        Report report = null;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);
        ZipArchiveInputStream archiveReader;
        archiveReader = new ZipArchiveInputStream(byteArrayInputStream);
        ZipArchiveEntry archiveEntry;
        // importing report.xml to report object
        while (((archiveEntry = archiveReader.getNextZipEntry()) != null) && (report == null)) {
            if (archiveEntry.getName().equals("report.xml")) {
                String xml = new String(readBytesFromEntry(archiveReader));
                report = fromXML(Report.class, xml);
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
                        FileDescriptor fd = template.getTemplateFileDescriptor();
                        FileStorageAPI mbean = AppBeans.get(FileStorageAPI.NAME);
                        try {
                            mbean.removeFile(fd);
                        } catch (FileStorageException ex) {
                            if (ex.getType() != FileStorageException.Type.FILE_NOT_FOUND)
                                log.warn("Could not remove old template file", ex);
                        }
                        // use new Id for new FileDescriptor
                        fd.setId(AppBeans.get(UuidSource.class).createUuid());
                        mbean.saveFile(fd, readBytesFromEntry(archiveReader));
                    }
                    i++;
                }
            }
        }
        byteArrayInputStream.close();

        Persistence persistence = AppBeans.get(Persistence.class);

        if (report != null) {
            View exportView = AppBeans.get(Metadata.class).getViewRepository().getView(Report.class, "report.export");

            Report existingReport;
            // remove old report
            Transaction tx = persistence.createTransaction();
            try {
                EntityManager em = persistence.getEntityManager();
                em.setView(exportView);
                existingReport = em.find(Report.class, report.getId());

                if (existingReport != null) {
                    // remove old objects from db
                    removeReportObjects(existingReport, em);

                    em.flush();
                }
                tx.commit();
            } finally {
                tx.end();
            }

            tx = persistence.createTransaction();
            try {
                EntityManager em = persistence.getEntityManager();
                ReportGroup reportGroup;

                if (report.getGroup() != null) {
                    reportGroup = em.find(ReportGroup.class, report.getGroup().getId());
                } else {
                    Query query = em.createQuery("select gr from report$ReportGroup gr where gr.code = 'ReportGroup.default'");
                    reportGroup = (ReportGroup) query.getSingleResult();
                }

                if (reportGroup == null) {
                    reportGroup = new ReportGroup();
                    reportGroup.setId(report.getGroup().getId());
                    reportGroup.setCode(report.getGroup().getCode());
                    reportGroup.setTitle(report.getGroup().getTitle());

                    em.persist(reportGroup);
                }
                report.setGroup(reportGroup);

                persistReportObjects(report, em);

                // persist report
                if (existingReport == null) {
                    em.persist(report);
                } else {
                    Report mergedReport = em.merge(report);
                    // reattach objects
                    reattachObjectsToMergedReport(report, mergedReport);
                }

                tx.commit();
            } finally {
                tx.end();
            }
        }

        return report;
    }

    private static void removeReportObjects(Report report, EntityManager em) {
        // remove templates
        if (report.getTemplates() != null) {
            for (ReportTemplate template : report.getTemplates()) {
                em.remove(template);
                if (template.getTemplateFileDescriptor() != null)
                    em.remove(template.getTemplateFileDescriptor());
            }
        }

        // remove parameters
        if (report.getInputParameters() != null) {
            for (ReportInputParameter parameter : report.getInputParameters())
                em.remove(parameter);
        }

        // remove value formats
        if (report.getValuesFormats() != null) {
            for (ReportValueFormat valueFormat : report.getValuesFormats())
                em.remove(valueFormat);
        }

        // remove report screens
        if (report.getReportScreens() != null) {
            for (ReportScreen screen : report.getReportScreens())
                em.remove(screen);
        }

        // remove band definitions
        if (report.getBands() != null) {
            for (BandDefinition band : report.getBands()) {
                if (band.getDataSets() != null) {
                    for (DataSet ds : band.getDataSets())
                        em.remove(ds);
                }
                em.remove(band);
            }
        }
    }

    private static void reattachObjectsToMergedReport(Report report, Report mergedReport) {
        if (report.getTemplates() != null) {
            for (ReportTemplate template : report.getTemplates())
                template.setReport(mergedReport);
        }
        if (report.getBands() != null) {
            for (BandDefinition band : report.getBands())
                band.setReport(mergedReport);
        }
        if (report.getReportScreens() != null) {
            for (ReportScreen screen : report.getReportScreens())
                screen.setReport(mergedReport);
        }
        if (report.getValuesFormats() != null) {
            for (ReportValueFormat valueFormat : report.getValuesFormats())
                valueFormat.setReport(mergedReport);
        }
        if (report.getInputParameters() != null) {
            for (ReportInputParameter parameter : report.getInputParameters())
                parameter.setReport(mergedReport);
        }
    }

    private static void persistReportObjects(Report report, EntityManager em) {
        // persist templates
        if (report.getTemplates() != null) {
            for (ReportTemplate template : report.getTemplates()) {
                if (template.getTemplateFileDescriptor() != null) {
                    em.persist(template.getTemplateFileDescriptor());
                }
                em.persist(template);
            }
        }

        // persist parameters
        if (report.getInputParameters() != null) {
            for (ReportInputParameter parameter : report.getInputParameters())
                em.persist(parameter);
        }

        // persist value formats
        if (report.getValuesFormats() != null) {
            for (ReportValueFormat valueFormat : report.getValuesFormats())
                em.persist(valueFormat);
        }

        // persist report screens
        if (report.getReportScreens() != null) {
            for (ReportScreen screen : report.getReportScreens())
                em.persist(screen);
        }

        // persist band definitions
        if (report.getBands() != null) {
            for (BandDefinition band : report.getBands()) {
                if (band.getDataSets() != null) {
                    for (DataSet ds : band.getDataSets())
                        em.persist(ds);
                }
                em.persist(band);
            }
        }
    }
}
