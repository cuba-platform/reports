/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.reports.core;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.reports.ReportImportExportMBean;

public class DeployTest extends ReportsTestCase {
    public void testDeployReports() throws Exception {
        ReportImportExportMBean reportImportExport = AppBeans.get(ReportImportExportMBean.class);
        reportImportExport.deployAllReportsFromPath("./modules/core/test/com/haulmont/reports/core/reports");
    }
}
