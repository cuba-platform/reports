/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports.gui;

import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.report.ReportOutputType;

import java.util.HashMap;

/**
 * @author artamonov
 * @version $Id$
 */
public class ReportPrintHelper {

    private static HashMap<ReportOutputType, ExportFormat> exportFormats = new HashMap<ReportOutputType, ExportFormat>();

    static {
        exportFormats.put(ReportOutputType.XLS, ExportFormat.XLS);
        exportFormats.put(ReportOutputType.DOC, ExportFormat.DOC);
        exportFormats.put(ReportOutputType.PDF, ExportFormat.PDF);
        exportFormats.put(ReportOutputType.HTML, ExportFormat.HTML);
    }

    public static ExportFormat getExportFormat(ReportOutputType outputType) {
        return exportFormats.get(outputType);
    }
}