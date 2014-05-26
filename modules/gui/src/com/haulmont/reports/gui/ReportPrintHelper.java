/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui;

import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.yarg.structure.ReportOutputType;

import java.util.*;

/**
 * @author artamonov
 * @version $Id$
 */
public class ReportPrintHelper {

    private static HashMap<ReportOutputType, ExportFormat> exportFormats = new HashMap<>();

    private static Map<String, List<com.haulmont.reports.entity.ReportOutputType>> inputOutputTypesMapping = new HashMap<>();

    static {
        inputOutputTypesMapping.put("docx", new ArrayList<>(Arrays.asList(com.haulmont.reports.entity.ReportOutputType.DOCX, com.haulmont.reports.entity.ReportOutputType.HTML, com.haulmont.reports.entity.ReportOutputType.PDF)));
        inputOutputTypesMapping.put("doc", new ArrayList<>(Arrays.asList(com.haulmont.reports.entity.ReportOutputType.DOC, com.haulmont.reports.entity.ReportOutputType.PDF)));
        inputOutputTypesMapping.put("odt", new ArrayList<>(Arrays.asList(com.haulmont.reports.entity.ReportOutputType.DOC, com.haulmont.reports.entity.ReportOutputType.PDF)));
        inputOutputTypesMapping.put("xlsx", new ArrayList<>(Arrays.asList(com.haulmont.reports.entity.ReportOutputType.XLSX, com.haulmont.reports.entity.ReportOutputType.PDF)));
        inputOutputTypesMapping.put("xls", new ArrayList<>(Arrays.asList(com.haulmont.reports.entity.ReportOutputType.XLS, com.haulmont.reports.entity.ReportOutputType.PDF)));
        inputOutputTypesMapping.put("html", new ArrayList<>(Arrays.asList(com.haulmont.reports.entity.ReportOutputType.HTML, com.haulmont.reports.entity.ReportOutputType.PDF)));
        inputOutputTypesMapping.put("ftl", new ArrayList<>(Arrays.asList(com.haulmont.reports.entity.ReportOutputType.HTML, com.haulmont.reports.entity.ReportOutputType.PDF)));
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

    public static Map<String, List<com.haulmont.reports.entity.ReportOutputType>> getInputOutputTypesMapping() {
        return inputOutputTypesMapping;
    }
}