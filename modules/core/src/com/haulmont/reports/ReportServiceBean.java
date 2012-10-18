/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.reports.app.ReportOutputDocument;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $Id$
 */
@Service(ReportService.NAME)
public class ReportServiceBean implements ReportService {

    @Override
    public ReportOutputDocument createReport(Report report, Map<String, Object> params) throws IOException {
        ReportingApi uploadingApi = AppBeans.get(ReportingApi.NAME);
        return uploadingApi.createReport(report, params);
    }

    @Override
    public ReportOutputDocument createReport(Report report, String templateCode, Map<String, Object> params) throws IOException {
        ReportingApi uploadingApi = AppBeans.get(ReportingApi.NAME);
        return uploadingApi.createReport(report, templateCode, params);
    }

    @Override
    public ReportOutputDocument createReport(Report report, ReportTemplate template, Map<String, Object> params) throws IOException {
        ReportingApi uploadingApi = AppBeans.get(ReportingApi.NAME);
        return uploadingApi.createReport(report, template, params);
    }

    @Override
    public Report reloadReport(Report report) {
        ReportingApi uploadingApi = AppBeans.get(ReportingApi.NAME);
        return uploadingApi.reloadReport(report);
    }

    @Override
    public byte[] exportReports(Collection<Report> reports) throws IOException, FileStorageException {
        ReportingApi uploadingApi = AppBeans.get(ReportingApi.NAME);
        return uploadingApi.exportReports(reports);
    }

    @Override
    public FileDescriptor createAndSaveReport(Report report,
                                              Map<String, Object> params, String fileName) throws IOException {
        ReportingApi uploadingApi = AppBeans.get(ReportingApi.NAME);
        return uploadingApi.createAndSaveReport(report, params, fileName);
    }

    @Override
    public FileDescriptor createAndSaveReport(Report report, String templateCode,
                                              Map<String, Object> params, String fileName) throws IOException {
        ReportingApi uploadingApi = AppBeans.get(ReportingApi.NAME);
        return uploadingApi.createAndSaveReport(report, templateCode, params, fileName);
    }

    @Override
    public FileDescriptor createAndSaveReport(Report report, ReportTemplate template,
                                              Map<String, Object> params, String fileName) throws IOException {
        ReportingApi uploadingApi = AppBeans.get(ReportingApi.NAME);
        return uploadingApi.createAndSaveReport(report, template, params, fileName);
    }

    @Override
    public Collection<Report> importReports(byte[] zipBytes) throws IOException, FileStorageException {
        ReportingApi uploadingApi = AppBeans.get(ReportingApi.NAME);
        return uploadingApi.importReports(zipBytes);
    }
}