/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.app.service;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.entity.DataSetType;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.yarg.reporting.ReportOutputDocument;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $id$
 */
public interface ReportService {
    String NAME = "report_ReportService";

    String MAIN_VIEW_NAME = "report.edit";

    String DEFAULT_TEMPLATE_CODE = "DEFAULT";

    ReportOutputDocument createReport(Report report, Map<String, Object> params);

    ReportOutputDocument createReport(Report report, String templateCode, Map<String, Object> params);

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

    String convertToXml(Report report);

    Report convertToReport(String xml);

    Report copyReport(Report source);

    /**
     * Prints the report several times for each parameter map in the paramsList. Put the result files to zip archive.
     */
    ReportOutputDocument bulkPrint(Report report, List<Map<String, Object>> paramsList);

    MetaClass findMetaClassByDataSetEntityAlias(String alias, DataSetType dataSetType, List<ReportInputParameter> reportInputParameters);

    List loadDataForParameterPrototype(ParameterPrototype prototype);
}
