/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.View;
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
 * API for reporting
 *
 * @author artamonov
 * @version $Id$
 */
public interface ReportingApi {
    String NAME = "report_ReportingApi";

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

    String convertToXml(Report report);

    Report convertToReport(String xml);

    Report copyReport(Report source);

    ReportOutputDocument bulkPrint(Report report, List<Map<String, Object>> paramsList);

    <T extends Entity> T reloadEntity(T entity, View view);

    MetaClass findMetaClassByDataSetEntityAlias(String alias, DataSetType dataSetType, List<ReportInputParameter> reportInputParameters);

    String generateReportName(String sourceName);

    List loadDataForParameterPrototype(ParameterPrototype prototype);
}