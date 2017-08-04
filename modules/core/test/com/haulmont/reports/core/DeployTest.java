/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.core;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.reports.ReportImportExportMBean;
import com.haulmont.reports.testsupport.ReportsTestContainer;
import org.junit.ClassRule;
import org.junit.Test;

public class DeployTest {
    @ClassRule
    public static ReportsTestContainer cont = ReportsTestContainer.Common.INSTANCE;

    @Test
    public void testDeployReports() throws Exception {
        ReportImportExportMBean reportImportExport = AppBeans.get(ReportImportExportMBean.class);
        reportImportExport.deployAllReportsFromPath("./modules/core/test/com/haulmont/reports/core/reports");
    }
}