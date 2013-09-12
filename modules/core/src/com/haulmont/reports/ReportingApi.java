/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportTemplate;
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
}
