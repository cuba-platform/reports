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
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.reports.exception.ReportingException;
import com.haulmont.yarg.formatters.CustomReport;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.reporting.ReportingAPI;
import com.haulmont.yarg.reporting.RunParams;

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

    @Override
    public ReportOutputDocument createReport(Report report, Map<String, Object> params) throws IOException {
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
        ReportTemplate reportTemplate = report.getDefaultTemplate();
        return createReportDocument(report, reportTemplate, params);
    }

    @Override
    public ReportOutputDocument createReport(Report report, String templateCode, Map<String, Object> params)
            throws IOException {
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
        ReportTemplate template = report.getTemplateByCode(templateCode);
        return createReportDocument(report, template, params);
    }

    @Override
    public ReportOutputDocument createReport(Report report, ReportTemplate template, Map<String, Object> params)
            throws IOException {
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
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

            if (!template.isCustom()) {
                byte[] bytes = fileStorageAPI.loadFile(template.getTemplateFileDescriptor());
                template.setContent(bytes);
            } else {
                Class<Object> reportClass = scripting.loadClass(template.getCustomClass());
                template.setCustomReport((CustomReport) reportClass.newInstance());
            }

            return reportingApi.runReport(new RunParams(report).template(template).params(params));
        } catch (FileStorageException e) {
            throw new ReportingException(e);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ReportingException(String.format("Could not instantiate class for custom template [%s]", template.getCustomClass()), e);
        }
    }

    @Override
    public Report reloadReport(Report report) {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            View exportView = metadata.getViewRepository().getView(report.getClass(), "report.export");
            em.setView(exportView);
            report = em.find(Report.class, report.getId());
            if (report != null) {
                em.setView(null);
                em.fetch(report, exportView);
            }
            tx.commit();
            return report;
        } finally {
            tx.end();
        }
    }

    @Override
    public byte[] exportReports(Collection<Report> reports) throws IOException, FileStorageException {
        return ReportImportExportHelper.exportReports(reports);
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
        return ReportImportExportHelper.importReports(zipBytes);
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