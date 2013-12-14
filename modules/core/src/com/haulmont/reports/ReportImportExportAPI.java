/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.reports;

import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.reports.entity.Report;

import java.io.IOException;
import java.util.Collection;

/**
 * @author degtyarjov
 * @version $Id$
 */
public interface ReportImportExportAPI {
    final String NAME = "reporting_ReportImportExport";

    byte[] exportReports(Collection<Report> reports) throws IOException, FileStorageException;
    Collection<Report> importReports(byte[] zipBytes) throws IOException, FileStorageException;
}
