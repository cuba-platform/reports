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

package com.haulmont.reports.libintegration.extraction.controller;

import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.reports.testsupport.ReportsContextBootstrapper;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@BootstrapWith(ReportsContextBootstrapper.class)
public class CrosstabControllerTest extends AbstractControllerTestClass {

    @Before
    public void construct() throws SQLException {
        ScriptUtils.executeSqlScript(
                persistence.getDataSource().getConnection(),
                resourceLoader
                .getResource("/com/haulmont/reports/libintegration/extraction/controller/initial-ddl.sql"));
    }

    @Test
    public void testSqlExtractionForCrosstabBand() throws URISyntaxException, IOException, TemplateGenerationException, InvalidFormatException {
        ReportOutputDocument document = createDocument(
                "/com/haulmont/reports/fixture/cross_sql_report_band.yml",
                "/com/haulmont/reports/fixture/crosstab_template.xlsx");

        Assert.assertNotNull(document);
        Assert.assertNotNull(document.getDocumentName());

        assertCrossDataDocument(document);
    }

    @Test
    public void testJpqlExtractionForCrosstabBand() throws URISyntaxException, IOException, TemplateGenerationException, InvalidFormatException {
        ReportOutputDocument document = createDocument(
                "/com/haulmont/reports/fixture/cross_jpql_report_band.yml",
                "/com/haulmont/reports/fixture/crosstab_template.xlsx");

        Assert.assertNotNull(document);
        Assert.assertNotNull(document.getDocumentName());

        assertCrossDataDocument(document);
    }

    @Test
    public void testGroovyExtractionForCrosstabBand() throws URISyntaxException, IOException, TemplateGenerationException, InvalidFormatException {
        ReportOutputDocument document = createDocument(
                "/com/haulmont/reports/fixture/cross_groovy_report_band.yml",
                "/com/haulmont/reports/fixture/crosstab_template.xlsx");

        Assert.assertNotNull(document);
        Assert.assertNotNull(document.getDocumentName());

        assertCrossDataDocument(document);
    }

    @Test
    public void testSingleEntityExtractionForCrosstabBand() throws URISyntaxException, IOException, TemplateGenerationException, InvalidFormatException {
        ReportOutputDocument document = createDocument(
                "/com/haulmont/reports/fixture/cross_single_entity_report_band.yml",
                "/com/haulmont/reports/fixture/crosstab_template.xlsx");

        Assert.assertNotNull(document);
        Assert.assertNotNull(document.getDocumentName());

        assertCrossDataDocument(persistence.callInTransaction(em ->
                Arrays.asList(em.createQuery("select e.login from test$User e", String.class)
                        .getFirstResult())), document);
    }

    @Test
    public void testMultipleEntityExtractionForCrosstabBand() throws URISyntaxException, IOException, TemplateGenerationException, InvalidFormatException {
        ReportOutputDocument document = createDocument(
                "/com/haulmont/reports/fixture/cross_multi_entity_report_band.yml",
                "/com/haulmont/reports/fixture/crosstab_template.xlsx");

        Assert.assertNotNull(document);
        Assert.assertNotNull(document.getDocumentName());

        assertCrossDataDocument(document);
    }

    @Test
    public void testJsonExtractionForCrosstabBand() throws URISyntaxException, IOException, TemplateGenerationException, InvalidFormatException {
        ReportOutputDocument document = createDocument(
                "/com/haulmont/reports/fixture/cross_json_entity_report_band.yml",
                "/com/haulmont/reports/fixture/crosstab_template.xlsx");

        Assert.assertNotNull(document);
        Assert.assertNotNull(document.getDocumentName());

        assertCrossDataDocument(document);
    }

}
