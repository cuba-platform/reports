/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports.app.service;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.entity.*;
import com.haulmont.yarg.reporting.ReportOutputDocument;

import java.util.*;

public interface ReportService {
    String NAME = "report_ReportService";

    String MAIN_VIEW_NAME = "report.edit";

    String DEFAULT_TEMPLATE_CODE = "DEFAULT";

    Report storeReportEntity(Report report);

    ReportOutputDocument createReport(Report report, Map<String, Object> params);

    ReportOutputDocument createReport(Report report, Map<String, Object> params, ReportOutputType outputType);

    ReportOutputDocument createReport(Report report, String templateCode, Map<String, Object> params);

    ReportOutputDocument createReport(Report report, String templateCode, Map<String, Object> params, ReportOutputType outputType);

    ReportOutputDocument createReport(Report report, ReportTemplate template, Map<String, Object> params);

    FileDescriptor createAndSaveReport(Report report, Map<String, Object> params, String fileName);

    FileDescriptor createAndSaveReport(Report report, String templateCode, Map<String, Object> params, String fileName);

    FileDescriptor createAndSaveReport(Report report, ReportTemplate template, Map<String, Object> params, String fileName);

    /**
     * Exports all reports and their templates into one zip archive. Each report is exported into a separate zip
     * archive with 2 files (report.xml and a template file (for example MyReport.doc)).
     * For example:
     * return byte[] (bytes of zip arhive)
     * -- MegaReport.zip
     * ---- report.xml
     * ---- Mega report.xls
     * -- Other report.zip
     * ---- report.xml
     * ---- other report.odt
     *
     * @param reports Collection of Report objects to be exported.
     * @return ZIP byte array with zip archives inside.
     */
    byte[] exportReports(Collection<Report> reports);

    /**
     * Imports reports from ZIP archive. Archive file format is described in exportReports method.
     *
     * @param zipBytes ZIP archive as a byte array.
     * @return Collection of imported reports.
     */
    Collection<Report> importReports(byte[] zipBytes);

    /**
     * Imports reports from ZIP archive. Archive file format is described in exportReports method.
     *
     * @param zipBytes ZIP archive as a byte array.
     * @param importOptions - report import options
     * @return Collection of imported reports.
     */
    Collection<Report> importReports(byte[] zipBytes, EnumSet<ReportImportOption> importOptions);

    /**
     * Imports reports from ZIP archive. Archive file format is described in exportReports method.
     *
     * @param zipBytes ZIP archive as a byte array.
     * @param importOptions report - import options
     * @return import result - collection of updated, created reports
     */
    ReportImportResult importReportsWithResult(byte[] zipBytes, EnumSet<ReportImportOption> importOptions);

    String convertToString(Report report);

    Report convertToReport(String xml);

    Report copyReport(Report source);

    /**
     * Prints the report several times for each parameter map in the paramsList. Put the result files to zip archive.
     */
    ReportOutputDocument bulkPrint(Report report, List<Map<String, Object>> paramsList);

    /**
     * Prints the report several times for each parameter map in the paramsList. Put the result files to zip archive.
     */
    ReportOutputDocument bulkPrint(Report report, String templateCode, ReportOutputType outputType, List<Map<String, Object>> paramsList);

    MetaClass findMetaClassByDataSetEntityAlias(String alias, DataSetType dataSetType, List<ReportInputParameter> reportInputParameters);

    List loadDataForParameterPrototype(ParameterPrototype prototype);

    String convertToString(Class parameterClass, Object paramValue);

    Object convertFromString(Class parameterClass, String paramValueStr);

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