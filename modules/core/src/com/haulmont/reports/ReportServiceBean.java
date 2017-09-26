/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.*;
import com.haulmont.reports.exception.ReportingException;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.util.converter.ObjectToStringConverter;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

@Service(ReportService.NAME)
public class ReportServiceBean implements ReportService {

    @Inject
    protected ReportingApi reportingApi;

    @Inject
    protected ObjectToStringConverter objectToStringConverter;

    @Inject
    protected TimeSource timeSource;

    @Override
    public Report storeReportEntity(Report report) {
        return reportingApi.storeReportEntity(report);
    }

    @Override
    public ReportOutputDocument createReport(Report report, Map<String, Object> params) {
        return reportingApi.createReport(report, params);
    }

    @Override
    public ReportOutputDocument createReport(Report report, String templateCode, Map<String, Object> params) {
        return reportingApi.createReport(report, templateCode, params);
    }

    @Override
    public ReportOutputDocument createReport(Report report, ReportTemplate template, Map<String, Object> params) {
        return reportingApi.createReport(report, template, params);
    }

    @Override
    public FileDescriptor createAndSaveReport(Report report,
                                              Map<String, Object> params, String fileName) {
        return reportingApi.createAndSaveReport(report, params, fileName);
    }

    @Override
    public FileDescriptor createAndSaveReport(Report report, String templateCode,
                                              Map<String, Object> params, String fileName) {
        return reportingApi.createAndSaveReport(report, templateCode, params, fileName);
    }

    @Override
    public FileDescriptor createAndSaveReport(Report report, ReportTemplate template,
                                              Map<String, Object> params, String fileName) {
        return reportingApi.createAndSaveReport(report, template, params, fileName);
    }

    @Override
    public byte[] exportReports(Collection<Report> reports) {
        return reportingApi.exportReports(reports);
    }

    @Override
    public Collection<Report> importReports(byte[] zipBytes) {
        return reportingApi.importReports(zipBytes);
    }

    @Override
    public Collection<Report> importReports(byte[] zipBytes, EnumSet<ReportImportOption> importOptions) {
        return reportingApi.importReports(zipBytes, importOptions);
    }

    @Override
    public String convertToString(Report report) {
        return reportingApi.convertToString(report);
    }

    @Override
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

    @Override
    public String convertToString(Class parameterClass, Object paramValue) {
        return objectToStringConverter.convertToString(parameterClass, paramValue);
    }

    @Override
    public Object convertFromString(Class parameterClass, String paramValueStr) {
        return objectToStringConverter.convertFromString(parameterClass, paramValueStr);
    }

    @Override
    public void cancelReportExecution(UUID userSessionId, UUID reportId) {
        reportingApi.cancelReportExecution(userSessionId, reportId);
    }

    @Override
    public Date adjustDate(ParameterType parameterType) {
        Date now = timeSource.currentTimestamp();
        switch (parameterType) {
            case TIME:
                now = getTime(now);
                break;
            case DATETIME:
                break;
            case DATE:
                now = getDate(now);
                break;
            default:
                throw new ReportingException("Not Date/Time related parameter types are not supported.");
        }

        return now;
    }

    protected Date getDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    protected Date getTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.YEAR, 1970);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }
}