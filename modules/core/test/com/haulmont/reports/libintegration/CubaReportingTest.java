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
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.BandData;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Collections;

public class CubaReportingTest {
    @Test
    public void testName() throws Exception {
        CubaReporting cubaReporting = new CubaReporting();

        BandData rootBand = new BandData(null);
        String overridenName = "overriddenFileName.txt";
        String basicName = "basicFileName.txt";

        ReportTemplate reportTemplate = new ReportTemplate();
        reportTemplate.setReportOutputType(ReportOutputType.CUSTOM);
        reportTemplate.setName(basicName);
        reportTemplate.setOutputNamePattern(basicName);
        Report report = new Report();
        report.setTemplates(Collections.singletonList(reportTemplate));

        RunParams runParams = new RunParams(report).template(reportTemplate);

        String outputName = cubaReporting.resolveOutputFileName(runParams, rootBand);
        Assert.assertEquals(basicName, outputName);

        rootBand.addData(CubaReporting.REPORT_FILE_NAME_KEY, overridenName);
        outputName = cubaReporting.resolveOutputFileName(runParams, rootBand);
        Assert.assertEquals(overridenName, outputName);

        rootBand.addData(CubaReporting.REPORT_FILE_NAME_KEY, "");
        outputName = cubaReporting.resolveOutputFileName(runParams.outputNamePattern(overridenName), rootBand);
        Assert.assertEquals(overridenName, outputName);
    }
}
