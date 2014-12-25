/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.DataSetType;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $Id$
 */
@Service(ReportService.NAME)
public class ReportServiceBean implements ReportService {
    @Inject
    private ReportingApi reportingApi;

    @Override
    public ReportOutputDocument createReport(Report report, Map<String, Object> params) throws IOException {
        return reportingApi.createReport(report, params);
    }

    @Override
    public ReportOutputDocument createReport(Report report, String templateCode, Map<String, Object> params) throws IOException {
        return reportingApi.createReport(report, templateCode, params);
    }

    @Override
    public ReportOutputDocument createReport(Report report, ReportTemplate template, Map<String, Object> params) throws IOException {
        return reportingApi.createReport(report, template, params);
    }

    @Override
    public FileDescriptor createAndSaveReport(Report report,
                                              Map<String, Object> params, String fileName) throws IOException {
        return reportingApi.createAndSaveReport(report, params, fileName);
    }

    @Override
    public FileDescriptor createAndSaveReport(Report report, String templateCode,
                                              Map<String, Object> params, String fileName) throws IOException {
        return reportingApi.createAndSaveReport(report, templateCode, params, fileName);
    }

    @Override
    public FileDescriptor createAndSaveReport(Report report, ReportTemplate template,
                                              Map<String, Object> params, String fileName) throws IOException {
        return reportingApi.createAndSaveReport(report, template, params, fileName);
    }

    @Override
    public byte[] exportReports(Collection<Report> reports) throws IOException, FileStorageException {
        return reportingApi.exportReports(reports);
    }

    @Override
    public Collection<Report> importReports(byte[] zipBytes) throws IOException, FileStorageException {
        return reportingApi.importReports(zipBytes);
    }

    public String convertToXml(Report report) {
        return reportingApi.convertToXml(report);
    }

    public Report convertToReport(String xml) {
        return reportingApi.convertToReport(xml);
    }

    @Override
    public Report copyReport(Report source) {
        return reportingApi.copyReport(source);
    }

    @Override
    public ReportOutputDocument bulkPrint(Report report, List<Map<String, Object>> paramsList) {
        return reportingApi.bulkPrint(report, paramsList);
    }

    @Override
    public MetaClass findMetaClassByDataSetEntityAlias(final String alias, DataSetType dataSetType, List<ReportInputParameter> reportInputParameters) {
        return reportingApi.findMetaClassByDataSetEntityAlias(alias, dataSetType, reportInputParameters);
    }

    @Override
    public List loadDataForParameterPrototype(ParameterPrototype prototype) {
        return reportingApi.loadDataForParameterPrototype(prototype);
    }
}