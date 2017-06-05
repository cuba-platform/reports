/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.View;
import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.entity.*;
import com.haulmont.yarg.reporting.ReportOutputDocument;

import java.util.*;

/**
 * API for reporting
 *
 */
public interface ReportingApi {
    String NAME = "report_ReportingApi";

    Report storeReportEntity(Report report);

    ReportOutputDocument createReport(Report report, Map<String, Object> params);

    ReportOutputDocument createReport(Report report, String templateCode, Map<String, Object> params);

    ReportOutputDocument createReport(Report report, ReportTemplate template, Map<String, Object> params);

    FileDescriptor createAndSaveReport(Report report,
                                              Map<String, Object> params, String fileName);

    FileDescriptor createAndSaveReport(Report report, String templateCode,
                                              Map<String, Object> params, String fileName);

    FileDescriptor createAndSaveReport(Report report, ReportTemplate template,
                                              Map<String, Object> params, String fileName);

    byte[] exportReports(Collection<Report> reports);

    Collection<Report> importReports(byte[] zipBytes);

    Collection<Report> importReports(byte[] zipBytes, EnumSet<ReportImportOption> importOptions);

    String convertToString(Report report);

    Report convertToReport(String xml);

    Report copyReport(Report source);

    ReportOutputDocument bulkPrint(Report report, List<Map<String, Object>> paramsList);

    <T extends Entity> T reloadEntity(T entity, View view);

    MetaClass findMetaClassByDataSetEntityAlias(String alias, DataSetType dataSetType, List<ReportInputParameter> reportInputParameters);

    String generateReportName(String sourceName);

    List loadDataForParameterPrototype(ParameterPrototype prototype);

    /**
     * Cancel report execution
     * @param userSessionId - user session that started report execution
     * @param reportId - identifier of executed report
     */
    void cancelReportExecution(UUID userSessionId, UUID reportId);
}