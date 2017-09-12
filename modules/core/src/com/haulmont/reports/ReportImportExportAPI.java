/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports;

import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportImportOption;
import com.haulmont.reports.entity.ReportImportResult;

import java.util.Collection;
import java.util.EnumSet;

public interface ReportImportExportAPI {
    String NAME = "reporting_ReportImportExport";

    byte[] exportReports(Collection<Report> reports);
    Collection<Report> importReports(byte[] zipBytes);
    Collection<Report> importReports(byte[] zipBytes, EnumSet<ReportImportOption> importOptions);
    ReportImportResult importReportsWithResult(byte[] zipBytes, EnumSet<ReportImportOption> importOptions);
}