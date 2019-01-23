/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportOutputType;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.yarg.structure.BandData;
import junit.framework.Assert;
import org.junit.Test;

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

        String outputName = cubaReporting.resolveOutputFileName(new Report(), reportTemplate, null, rootBand);
        Assert.assertEquals(overridenName, outputName);

        rootBand.addData(CubaReporting.REPORT_FILE_NAME_KEY, "");
        outputName = cubaReporting.resolveOutputFileName(new Report(), reportTemplate, null, rootBand);
        Assert.assertEquals(basicName, outputName);
    }
}
