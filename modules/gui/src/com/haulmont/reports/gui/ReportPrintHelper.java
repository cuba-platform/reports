/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui;

import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.yarg.structure.ReportOutputType;

import java.util.*;

import static com.haulmont.reports.entity.ReportOutputType.*;

/**
 * @author artamonov
 * @version $Id$
 */
public class ReportPrintHelper {

    private static HashMap<ReportOutputType, ExportFormat> exportFormats = new HashMap<>();

    private static Map<String, List<com.haulmont.reports.entity.ReportOutputType>> inputOutputTypesMapping = new HashMap<>();

    static {
        inputOutputTypesMapping.put("docx", Arrays.asList(DOCX, HTML, PDF));
        inputOutputTypesMapping.put("doc", Arrays.asList(DOC, PDF));
        inputOutputTypesMapping.put("odt", Arrays.asList(DOC, PDF));
        inputOutputTypesMapping.put("xlsx", Arrays.asList(XLSX, PDF));
        inputOutputTypesMapping.put("xlsm", Arrays.asList(XLSX, PDF));
        inputOutputTypesMapping.put("xls", Arrays.asList(XLS, PDF));
        inputOutputTypesMapping.put("html", Arrays.asList(HTML, PDF));
        inputOutputTypesMapping.put("ftl", Arrays.asList(HTML, PDF));
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