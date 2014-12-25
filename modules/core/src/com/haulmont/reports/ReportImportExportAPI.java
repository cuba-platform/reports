/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.reports;

import com.haulmont.reports.entity.Report;

import java.util.Collection;

/**
 * @author degtyarjov
 * @version $Id$
 */
public interface ReportImportExportAPI {
    final String NAME = "reporting_ReportImportExport";

    byte[] exportReports(Collection<Report> reports);
    Collection<Report> importReports(byte[] zipBytes);
}
