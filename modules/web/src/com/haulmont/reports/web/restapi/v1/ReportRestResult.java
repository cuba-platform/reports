/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.web.restapi.v1;

import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.structure.ReportOutputType;

public class ReportRestResult {
    protected byte[] content;
    protected String documentName;
    protected ReportOutputType reportOutputType;
    protected boolean attachment;

    public ReportRestResult(ReportOutputDocument document, boolean attachment) {
        this.content = document.getContent();
        this.documentName = document.getDocumentName();
        this.reportOutputType = document.getReportOutputType();
        this.attachment = attachment;
    }

    public byte[] getContent() {
        return content;
    }

    public String getDocumentName() {
        return documentName;
    }

    public ReportOutputType getReportOutputType() {
        return reportOutputType;
    }

    public boolean isAttachment() {
        return attachment;
    }
}
