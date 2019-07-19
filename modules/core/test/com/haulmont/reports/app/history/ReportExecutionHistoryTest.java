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

package com.haulmont.reports.app.history;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.entity.contracts.Id;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.reports.ReportExecutionHistoryRecorder;
import com.haulmont.reports.ReportingConfig;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.*;
import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.reports.fixture.yml.YmlBandUtil;
import com.haulmont.reports.testsupport.ReportsTestContainer;
import com.haulmont.reports.util.ReportHelper;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.*;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class ReportExecutionHistoryTest {
    @ClassRule
    public static ReportsTestContainer cont = ReportsTestContainer.Common.INSTANCE;

    protected ResourceLoader resourceLoader;
    protected ReportService reportService;
    protected ReportHelper reportHelper;
    protected ReportingConfig reportingConfig;
    protected DataManager dataManager;
    protected ReportExecutionHistoryRecorder executionHistoryRecorder;

    @Before
    public void setup() {
        resourceLoader = AppBeans.get(ResourceLoader.class);
        reportService = AppBeans.get(ReportService.NAME);
        reportHelper = AppBeans.get(ReportHelper.class);
        reportingConfig = AppBeans.get(Configuration.class).getConfig(ReportingConfig.class);
        dataManager = AppBeans.get(DataManager.NAME);
        executionHistoryRecorder = AppBeans.get(ReportExecutionHistoryRecorder.NAME);
    }

    @After
    public void cleanup() {
        cont.persistence().runInTransaction(em -> {
            em.createNativeQuery("delete from report_execution").executeUpdate();
            em.createNativeQuery("update report_report set default_template_id = null").executeUpdate();
            em.createNativeQuery("delete from report_template").executeUpdate();
            em.createNativeQuery("delete from report_report").executeUpdate();
        });
    }

    @Test
    public void testDisabled() throws Exception {
        reportingConfig.setHistoryRecordingEnabled(false);

        Report report = createReport("working-report.yml");
        report.setName("TestDisabled");
        reportService.createReport(report, new HashMap<>());

        ReportExecution execution = loadExecution("TestDisabled");
        assertNull(execution);
    }

    @Test
    public void testSuccess() throws Exception {
        reportingConfig.setHistoryRecordingEnabled(true);

        Report report = createReport("working-report.yml");
        report.setName("TestDisabled");
        report.setCode("TD");
        reportService.createReport(report, ParamsMap.of("k1", "v1"));

        ReportExecution execution = loadExecution("TestDisabled");
        assertNotNull(execution);
        assertNotNull(execution.getFinishTime());
        assertEquals("TD", execution.getReportCode());
        assertTrue(execution.getSuccess());
        assertFalse(execution.getCancelled());
        assertTrue(execution.getParams().contains("key: k1, value: v1"));
        assertNull(execution.getErrorMessage());
        assertEquals("localhost:8080/cuba-core", execution.getServerId());
    }

    @Test
    public void testFailure() throws Exception {
        reportingConfig.setHistoryRecordingEnabled(true);

        Report report = createReport("failing-report.yml");
        report.setName("TestFailure");
        try {
            reportService.createReport(report, Collections.emptyMap());
            Assert.fail("Exception expected");
        } catch (Exception e) {
            // ok
        }

        ReportExecution execution = loadExecution("TestFailure");
        assertNotNull(execution);
        assertNotNull(execution.getFinishTime());
        assertFalse(execution.getSuccess());
        assertFalse(execution.getCancelled());
        assertTrue(execution.getErrorMessage().contains("u.names"));
    }

    @Test
    public void testCleanByDays() throws Exception {
        reportingConfig.setHistoryRecordingEnabled(true);
        reportingConfig.setHistoryCleanupMaxDays(2);
        reportingConfig.setHistoryCleanupMaxItemsPerReport(0);

        Report report = createReport("working-report.yml");
        report.setName("TestCleanDays");

        reportService.createReport(report, Collections.emptyMap());
        reportService.createReport(report, Collections.emptyMap());

        List<ReportExecution> executions = loadExecutions("TestCleanDays");
        assertEquals(2, executions.size());

        // make first item too old
        executions.get(0).setStartTime(DateUtils.addDays(executions.get(0).getStartTime(), -2));
        dataManager.commit(executions.get(0));

        String deleted = executionHistoryRecorder.cleanupHistory();
        assertEquals("1", deleted);

        assertNull(reload(executions.get(0)));
        assertNotNull(reload(executions.get(1)));
    }

    @Test
    public void testCleanItemsPerReport() throws Exception {
        reportingConfig.setHistoryRecordingEnabled(true);
        reportingConfig.setHistoryCleanupMaxDays(0);
        reportingConfig.setHistoryCleanupMaxItemsPerReport(2);

        Report report = createReport("working-report.yml");
        report.setName("TestCleanPerItems");

        // need to persist report to DB to cleanup by this condition
        saveReportToDb(report);

        reportService.createReport(report, Collections.emptyMap());
        reportService.createReport(report, Collections.emptyMap());
        reportService.createReport(report, Collections.emptyMap());
        reportService.createReport(report, Collections.emptyMap());

        List<ReportExecution> executions = loadExecutions("TestCleanPerItems");
        assertEquals(4, executions.size());

        String deleted = executionHistoryRecorder.cleanupHistory();
        assertEquals("2", deleted);

        assertNull(reload(executions.get(0)));
        assertNull(reload(executions.get(1)));
        assertNotNull(reload(executions.get(2)));
    }

    private void saveReportToDb(Report report) {
        ReportGroup group = dataManager.load(ReportGroup.class)
                .query("select g from report$ReportGroup g")
                .maxResults(1)
                .one();

        report.setGroup(group);

        ReportTemplate template = report.getDefaultTemplate();
        report.setDefaultTemplate(null);
        dataManager.commit(report, template);
        report.setDefaultTemplate(template);
        dataManager.commit(report);
    }

    private Report createReport(String bandFile) throws IOException, TemplateGenerationException, URISyntaxException {
        String prefix = "/com/haulmont/reports/app/history/";
        BandDefinition root = YmlBandUtil.bandFrom(resourceLoader.getResource(prefix + bandFile).getFile());
        File templateFile = resourceLoader.getResource(prefix + "users_template.xlsx").getFile();
        return reportHelper.create(root, templateFile);
    }

    @Nullable
    private ReportExecution loadExecution(String reportName) {
        return dataManager.load(ReportExecution.class)
                .query("select e from report$ReportExecution e where e.reportName = :name")
                .parameter("name", reportName)
                .optional()
                .orElse(null);
    }

    private List<ReportExecution> loadExecutions(String reportName) {
        return dataManager.load(ReportExecution.class)
                .query("select e from report$ReportExecution e where e.reportName = :name order by e.startTime asc")
                .parameter("name", reportName)
                .list();
    }

    @Nullable
    private ReportExecution reload(ReportExecution execution) {
        return dataManager.load(Id.of(execution))
                .optional()
                .orElse(null);
    }
}
