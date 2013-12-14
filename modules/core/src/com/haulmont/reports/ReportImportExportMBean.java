/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.reports;

import com.haulmont.cuba.core.global.FileStorageException;

import java.io.IOException;

/**
 * @author degtyarjov
 * @version $Id$
 */
public interface ReportImportExportMBean {

    String deployAllReportsFromPath(String path) throws IOException, FileStorageException;
}