/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Contains import information about created reports or updated reports
 */
public class ReportImportResult implements Serializable {
    private static final long serialVersionUID = -1796078629837052922L;

    protected Collection<Report> importedReports;
    protected Collection<Report> updatedReports;
    protected Collection<Report> createdReports;

    public Collection<Report> getImportedReports() {
        return importedReports == null ? Collections.emptySet() : importedReports;
    }

    public Collection<Report> getUpdatedReports() {
        return updatedReports == null ? Collections.emptySet() : updatedReports;
    }

    public Collection<Report> getCreatedReports() {
        return createdReports == null ? Collections.emptySet() : createdReports;
    }

    public void addImportedReport(Report importedReport) {
        if (importedReports == null) {
            importedReports = new HashSet<>();
        }
        importedReports.add(importedReport);
    }

    public void addUpdatedReport(Report updatedReport) {
        if (updatedReports == null) {
            updatedReports = new HashSet<>();
        }
        updatedReports.add(updatedReport);
    }

    public void addCreatedReport(Report newReport) {
        if (createdReports == null) {
            createdReports = new HashSet<>();
        }
        createdReports.add(newReport);
    }
}
