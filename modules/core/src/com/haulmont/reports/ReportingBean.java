/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.app.FileStorageAPI;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.*;
import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.entity.*;
import com.haulmont.reports.exception.ReportingException;
import com.haulmont.yarg.formatters.CustomReport;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.reporting.ReportingAPI;
import com.haulmont.yarg.reporting.RunParams;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.reflection.ExternalizableConverter;
import org.apache.commons.lang.StringUtils;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

/**
 * @author artamonov
 * @version $Id$
 */
@ManagedBean(ReportingApi.NAME)
public class ReportingBean implements ReportingApi {
    public static final String REPORT_EDIT_VIEW_NAME = "report.edit";

    private PrototypesLoader prototypesLoader = new PrototypesLoader();

    @Inject
    private Persistence persistence;

    @Inject
    private Metadata metadata;

    @Inject
    private FileStorageAPI fileStorageAPI;

    @Inject
    private TimeSource timeSource;

    @Inject
    private ReportingAPI reportingApi;

    @Inject
    private Scripting scripting;

    @Inject
    private ReportImportExport reportImportExport;

    @Override
    public ReportOutputDocument createReport(Report report, Map<String, Object> params) throws IOException {
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
        fromXml(report);

        ReportTemplate reportTemplate = report.getDefaultTemplate();
        return createReportDocument(report, reportTemplate, params);
    }

    private void fromXml(Report report) {
        if (StringUtils.isNotBlank(report.getXml())) {
            Report reportFromXml = convertToReport(report.getXml());
            report.setBands(reportFromXml.getBands());
            report.setInputParameters(reportFromXml.getInputParameters());
            report.setReportScreens(reportFromXml.getReportScreens());
            report.setRoles(reportFromXml.getRoles());
            report.setValuesFormats(reportFromXml.getValuesFormats());
        }
    }

    @Override
    public ReportOutputDocument createReport(Report report, String templateCode, Map<String, Object> params)
            throws IOException {
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
        fromXml(report);
        ReportTemplate template = report.getTemplateByCode(templateCode);
        return createReportDocument(report, template, params);
    }

    @Override
    public ReportOutputDocument createReport(Report report, ReportTemplate template, Map<String, Object> params)
            throws IOException {
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
        fromXml(report);
        return createReportDocument(report, template, params);
    }

    private ReportOutputDocument createReportDocument(Report report, ReportTemplate template, Map<String, Object> params)
            throws IOException {
        try {
            List<String> prototypes = new LinkedList<>();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (param.getValue() instanceof ParameterPrototype)
                    prototypes.add(param.getKey());
            }
            Map<String, Object> paramsMap = new HashMap<>(params);

            for (String paramName : prototypes) {
                ParameterPrototype prototype = (ParameterPrototype) params.get(paramName);
                List data = prototypesLoader.loadData(prototype);
                paramsMap.put(paramName, data);
            }

            if (template.isCustom()) {
                Class<Object> reportClass = scripting.loadClass(template.getCustomClass());
                template.setCustomReport((CustomReport) reportClass.newInstance());
            }

            return reportingApi.runReport(new RunParams(report).template(template).params(params));
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ReportingException(String.format("Could not instantiate class for custom template [%s]", template.getCustomClass()), e);
        }
    }

    @Override
    public byte[] exportReports(Collection<Report> reports) throws IOException, FileStorageException {
        return reportImportExport.exportReports(reports);
    }

    @Override
    public FileDescriptor createAndSaveReport(Report report,
                                              Map<String, Object> params, String fileName) throws IOException {
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
        ReportTemplate template = report.getDefaultTemplate();
        return createAndSaveReport(report, template, params, fileName);
    }

    @Override
    public FileDescriptor createAndSaveReport(Report report, String templateCode,
                                              Map<String, Object> params, String fileName) throws IOException {
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
        ReportTemplate template = report.getTemplateByCode(templateCode);
        return createAndSaveReport(report, template, params, fileName);
    }

    @Override
    public FileDescriptor createAndSaveReport(Report report, ReportTemplate template,
                                              Map<String, Object> params, String fileName) throws IOException {
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
        return createAndSaveReportDocument(report, template, params, fileName);
    }

    private FileDescriptor createAndSaveReportDocument(Report report, ReportTemplate template, Map<String, Object> params, String fileName) throws IOException {
        byte[] reportData = createReportDocument(report, template, params).getContent();
        String ext = template.getReportOutputType().toString().toLowerCase();

        return saveReport(reportData, fileName, ext);
    }

    private FileDescriptor saveReport(byte[] reportData, String fileName, String ext) throws IOException {
        FileDescriptor file = new FileDescriptor();
        file.setCreateDate(timeSource.currentTimestamp());
        file.setName(fileName + "." + ext);
        file.setExtension(ext);
        file.setSize(reportData.length);

        try {
            fileStorageAPI.saveFile(file, reportData);
        } catch (FileStorageException e) {
            throw new IOException(e);
        }

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            em.persist(file);
            tx.commit();
        } finally {
            tx.end();
        }
        return file;
    }

    @Override
    public Collection<Report> importReports(byte[] zipBytes) throws IOException, FileStorageException {
        return reportImportExport.importReports(zipBytes);
    }

    public String convertToXml(Report report) {
        XStream xStream = createXStream();
        String xml = xStream.toXML(report);
        return xml;
    }

    public Report convertToReport(String xml) {
        XStream xStream = createXStream();
        return (Report) xStream.fromXML(xml);
    }

    private static XStream createXStream() {
        XStream xStream = new XStream();
        xStream.getConverterRegistry().removeConverter(ExternalizableConverter.class);
        xStream.registerConverter(new CollectionConverter(xStream.getMapper()) {
            @Override
            public boolean canConvert(Class type) {
                return ArrayList.class.isAssignableFrom(type) ||
                        HashSet.class.isAssignableFrom(type) ||
                        LinkedList.class.isAssignableFrom(type) ||
                        LinkedHashSet.class.isAssignableFrom(type);

            }
        }, XStream.PRIORITY_VERY_HIGH);

        xStream.registerConverter(new DateConverter() {
            @Override
            public boolean canConvert(Class type) {
                return Date.class.isAssignableFrom(type);
            }
        });

        xStream.alias("report", Report.class);
        xStream.alias("band", BandDefinition.class);
        xStream.alias("dataSet", DataSet.class);
        xStream.alias("parameter", ReportInputParameter.class);
        xStream.alias("template", ReportTemplate.class);
        xStream.alias("screen", ReportScreen.class);
        xStream.alias("format", ReportValueFormat.class);
        xStream.aliasSystemAttribute(null, "class");
        xStream.omitField(ReportTemplate.class, "content");
        xStream.omitField(ReportInputParameter.class, "localeName");
        xStream.omitField(Report.class, "xml");

        return xStream;
    }


    private <T extends Entity> T reloadEntity(T entity, String viewName) {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            View targetView = metadata.getViewRepository().getView(entity.getClass(), viewName);
            em.setView(targetView);
            entity = (T) em.find(entity.getClass(), entity.getId());
            if (entity != null) {
                em.setView(null);
                em.fetch(entity, targetView);
            }
            tx.commit();
            return entity;
        } finally {
            tx.end();
        }
    }
}