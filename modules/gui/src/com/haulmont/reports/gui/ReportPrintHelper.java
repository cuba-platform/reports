/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports.gui;

import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.yarg.structure.ReportOutputType;

import java.util.HashMap;

/**
 * @author artamonov
 * @version $Id$
 */
public class ReportPrintHelper {

    private static HashMap<ReportOutputType, ExportFormat> exportFormats = new HashMap<>();

    static {
        exportFormats.put(ReportOutputType.xls, ExportFormat.XLS);
        exportFormats.put(ReportOutputType.xlsx, ExportFormat.XLSX);
        exportFormats.put(ReportOutputType.doc, ExportFormat.DOC);
        exportFormats.put(ReportOutputType.docx, ExportFormat.DOCX);
        exportFormats.put(ReportOutputType.pdf, ExportFormat.PDF);
        exportFormats.put(ReportOutputType.html, ExportFormat.HTML);
    }

    public static ExportFormat getExportFormat(ReportOutputType outputType) {
        return exportFormats.get(outputType);
    }
}