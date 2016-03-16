/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

/**
 *
 */
package com.haulmont.reports;

import com.haulmont.reports.entity.Report;

import java.util.Collection;

/**
 */
public interface ReportImportExportAPI {
    String NAME = "reporting_ReportImportExport";

    byte[] exportReports(Collection<Report> reports);
    Collection<Report> importReports(byte[] zipBytes);
}