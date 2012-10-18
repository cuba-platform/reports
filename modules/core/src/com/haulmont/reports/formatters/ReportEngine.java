/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.formatters;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.reports.entity.Band;
import com.haulmont.reports.entity.ReportOutputType;

import java.io.OutputStream;

/**
 * Interface for main Report Formatters
 *
 * @see DocFormatter
 * @see XLSFormatter
 * @see HtmlFormatter
 *
 * @author artamonov
 * @version $Id$
 */
public interface ReportEngine {
    void setTemplateFile(FileDescriptor templateFile);

    ReportOutputType getDefaultOutputType();

    boolean hasSupportReport(String reportExtension, ReportOutputType outputType);

    void createDocument(Band rootBand, ReportOutputType outputType, OutputStream outputStream);
}