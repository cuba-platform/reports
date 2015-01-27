/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportOutputType;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.yarg.structure.BandData;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class CubaReportingTest {
    @Test
    public void testName() throws Exception {
        CubaReporting cubaReporting = new CubaReporting();

        BandData rootBand = new BandData(null);
        String overridenName = "overriddenFileName.txt";
        String basicName = "basicFileName.txt";
        rootBand.addData(CubaReporting.REPORT_FILE_NAME_KEY, overridenName);

        ReportTemplate reportTemplate = new ReportTemplate();
        reportTemplate.setReportOutputType(ReportOutputType.CUSTOM);
        reportTemplate.setName(basicName);

        String outputName = cubaReporting.resolveOutputFileName(new Report(), reportTemplate, rootBand);
        Assert.assertEquals(overridenName, outputName);

        rootBand.addData(CubaReporting.REPORT_FILE_NAME_KEY, "");
        outputName = cubaReporting.resolveOutputFileName(new Report(), reportTemplate, rootBand);
        Assert.assertEquals(basicName, outputName);
    }
}
