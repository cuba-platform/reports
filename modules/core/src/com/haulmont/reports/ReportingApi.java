/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.View;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.TemplateFileType;
import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.yarg.reporting.ReportOutputDocument;

import java.io.IOException;
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

    ReportOutputDocument createReport(Report report, Map<String, Object> params) throws IOException;

    ReportOutputDocument createReport(Report report, String templateCode, Map<String, Object> params) throws IOException;

    ReportOutputDocument createReport(Report report, ReportTemplate template, Map<String, Object> params) throws IOException;

    FileDescriptor createAndSaveReport(Report report,
                                              Map<String, Object> params, String fileName) throws IOException;

    FileDescriptor createAndSaveReport(Report report, String templateCode,
                                              Map<String, Object> params, String fileName) throws IOException;

    FileDescriptor createAndSaveReport(Report report, ReportTemplate template,
                                              Map<String, Object> params, String fileName) throws IOException;

    byte[] exportReports(Collection<Report> reports) throws IOException, FileStorageException;

    Collection<Report> importReports(byte[] zipBytes) throws IOException, FileStorageException;

    String convertToXml(Report report);

    Report convertToReport(String xml);

    Report copyReport(Report source);

    ReportOutputDocument bulkPrint(Report report, List<Map<String, Object>> paramsList);

    <T extends Entity> T reloadEntity(T entity, View view);
}
