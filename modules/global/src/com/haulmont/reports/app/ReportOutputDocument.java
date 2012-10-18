/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports.app;

import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportOutputType;

import java.io.Serializable;

/**
 * Output from ReportService
 *
 * @author artamonov
 * @version $Id$
 */
public class ReportOutputDocument implements Serializable {

    private static final long serialVersionUID = 9168523006847042457L;

    private Report report;

    private ReportOutputType outputType;

    private String documentName;

    private byte[] content;

    public ReportOutputDocument(Report report, ReportOutputType outputType, byte[] content) {
        this.report = report;
        this.outputType = outputType;
        this.content = content;
    }

    public ReportOutputDocument(Report report, ReportOutputType outputType, String documentName, byte[] content) {
        this.report = report;
        this.outputType = outputType;
        this.documentName = documentName;
        this.content = content;
    }

    public Report getReport() {
        return report;
    }

    public ReportOutputType getOutputType() {
        return outputType;
    }

    public byte[] getContent() {
        return content;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }
}