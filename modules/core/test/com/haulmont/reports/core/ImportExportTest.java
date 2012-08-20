/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports.core;

import com.haulmont.cuba.core.Locator;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.report.Report;
import com.haulmont.cuba.report.ReportType;
import com.haulmont.cuba.report.app.ReportService;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * @author artamonov
 * @version $Id$
 */
public class ImportExportTest extends ReportsTestCase {

    public void testImport() throws IOException, FileStorageException {
        ReportService reportService = Locator.lookup(ReportService.NAME);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InputStream reportsStream = getClass().getResourceAsStream("/com/haulmont/reports/core/TestReport.zip");
        IOUtils.copy(reportsStream, byteArrayOutputStream);

        Collection<Report> reports = reportService.importReports(byteArrayOutputStream.toByteArray());
        assertNotNull(reports);
        assertEquals(reports.size(), 1);

        Report report = reports.iterator().next();
        assertEquals(report.getName(), "TestReport");
        assertEquals(report.getCode(), "TestReport");
        Assert.assertEquals(report.getReportType(), ReportType.SIMPLE);
        assertNotNull(report.getGroup());

        assertNotNull(report.getTemplates());
        assertEquals(report.getTemplates().size(), 1);
        assertEquals(report.getTemplates().get(0).getCode(), "DEFAULT");

        assertNotNull(report.getBands());
        assertNotNull(report.getRootBandDefinition());
        assertEquals(report.getBands().size(), 2);

        assertNotNull(report.getInputParameters());
        assertEquals(report.getInputParameters().size(), 1);

        assertNotNull(report.getValuesFormats());
        assertEquals(report.getValuesFormats().size(), 1);

        assertNotNull(report.getReportScreens());
        assertEquals(report.getReportScreens().size(), 1);
    }
}