/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    ReportOutputDocument createReport(Report report, Map<String, Object> params, ReportOutputType outputType);

    ReportOutputDocument createReport(Report report, String templateCode, Map<String, Object> params);

    ReportOutputDocument createReport(Report report, String templateCode, Map<String, Object> params, ReportOutputType outputType);

    ReportOutputDocument createReport(Report report, ReportTemplate template, Map<String, Object> params);

    ReportOutputDocument createReport(ReportRunParams reportRunParams);

    FileDescriptor createAndSaveReport(Report report,
                                              Map<String, Object> params, String fileName);

    FileDescriptor createAndSaveReport(Report report, String templateCode,
                                              Map<String, Object> params, String fileName);

    FileDescriptor createAndSaveReport(Report report, ReportTemplate template,
                                              Map<String, Object> params, String fileName);

    FileDescriptor createAndSaveReport(ReportRunParams reportRunParams);

    byte[] exportReports(Collection<Report> reports);

    Collection<Report> importReports(byte[] zipBytes);

    Collection<Report> importReports(byte[] zipBytes, EnumSet<ReportImportOption> importOptions);

    ReportImportResult importReportsWithResult(byte[] zipBytes, EnumSet<ReportImportOption> importOptions);

    String convertToString(Report report);

    Report convertToReport(String xml);

    Report copyReport(Report source);

    ReportOutputDocument bulkPrint(Report report, List<Map<String, Object>> paramsList);

    ReportOutputDocument bulkPrint(Report report, String templateCode, ReportOutputType outputType, List<Map<String, Object>> paramsList);

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

    /**
     * Get current date {@link Date} according to {@link ParameterType} value
     *
     * @param parameterType - ParameterType value.
     * @return adjusted Date
     */
    Date currentDateOrTime(ParameterType parameterType);
}