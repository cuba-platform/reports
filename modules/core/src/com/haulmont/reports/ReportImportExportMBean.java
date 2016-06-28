/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

/**
 *
 */
package com.haulmont.reports;

import com.haulmont.cuba.core.global.FileStorageException;

import java.io.IOException;

public interface ReportImportExportMBean {

    String deployAllReportsFromPath(String path) throws IOException, FileStorageException;
}