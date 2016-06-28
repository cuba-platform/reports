/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.core;/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

import com.haulmont.cuba.core.global.View;
import com.haulmont.reports.converter.GsonConverter;
import com.haulmont.reports.converter.XStreamConverter;
import com.haulmont.reports.entity.*;
import com.haulmont.reports.testsupport.ReportsTestContainer;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ReportSerializationTest {

    @ClassRule
    public static ReportsTestContainer cont = ReportsTestContainer.Common.INSTANCE;

    @Test
    public void testGsonConverter() throws Exception {
        GsonConverter gsonConverter = new GsonConverter();
        Report sourceReport = createData();
        String json = gsonConverter.convertToString(sourceReport);
        Report resultReport = gsonConverter.convertToReport(json);

        assertEquals(sourceReport, resultReport);
        assertEquals(sourceReport.getName(), resultReport.getName());
        assertEquals(sourceReport.getCode(), resultReport.getCode());
        BandDefinition sourceRoot = sourceReport.getRootBandDefinition();
        BandDefinition resultRoot = resultReport.getRootBandDefinition();
        assertEquals(sourceRoot, resultRoot);
        assertEquals(sourceRoot.getName(), resultRoot.getName());
        assertEquals(sourceReport.getDefaultTemplate(), resultReport.getDefaultTemplate());
        List<BandDefinition> sourceRootChildren = sourceRoot.getChildrenBandDefinitions();
        List<BandDefinition> resultRootChildren = resultRoot.getChildrenBandDefinitions();
        assertEquals(sourceRootChildren.get(0), resultRootChildren.get(0));
        assertEquals(sourceRootChildren.get(1), resultRootChildren.get(1));
        assertEquals(sourceRootChildren.get(2), resultRootChildren.get(2));
        assertEquals(sourceRootChildren.get(3), resultRootChildren.get(3));
        assertEquals(sourceRootChildren.get(0).getName(), resultRootChildren.get(0).getName());
        assertEquals(sourceRootChildren.get(1).getName(), resultRootChildren.get(1).getName());
        assertEquals(sourceRootChildren.get(2).getName(), resultRootChildren.get(2).getName());
        assertEquals(sourceRootChildren.get(3).getName(), resultRootChildren.get(3).getName());
        assertNotNull(resultReport.getRootBandDefinition().getDataSets().get(0).getView());
    }

    @Test
    public void testXstreamDeserialization() throws Exception {
        String xml = "<report>\n" +
                "  <detached>false</detached>\n" +
                "  <id>568d76a6-c984-c5ae-e410-8acd62c316dd</id>\n" +
                "  <name>testHtml</name>\n" +
                "  <group>\n" +
                "    <detached>true</detached>\n" +
                "    <createTs>2012-04-26 14:16:21.199 UTC</createTs>\n" +
                "    <id>4e083530-0b9c-11e1-9b41-6bdaa41bff94</id>\n" +
                "    <version>42</version>\n" +
                "    <title>General</title>\n" +
                "    <code>ReportGroup.default</code>\n" +
                "    <localeNames>en=General \n" +
                "ru=Общие</localeNames>\n" +
                "    <localeName>Общие</localeName>\n" +
                "  </group>\n" +
                "  <defaultTemplate>\n" +
                "    <detached>false</detached>\n" +
                "    <id>49524c15-4a12-7c5f-61de-8bb3c5bfca24</id>\n" +
                "    <report reference=\"../..\"/>\n" +
                "    <reportOutputType>40</reportOutputType>\n" +
                "    <code>DEFAULT</code>\n" +
                "    <customFlag>false</customFlag>\n" +
                "    <customClass></customClass>\n" +
                "    <definedBy>100</definedBy>\n" +
                "    <name>testhtml.docx</name>\n" +
                "  </defaultTemplate>\n" +
                "  <reportType>10</reportType>\n" +
                "  <templates>\n" +
                "    <template reference=\"../../defaultTemplate\"/>\n" +
                "  </templates>\n" +
                "  <rootBandDefinition>\n" +
                "    <uuid>3e0fbfd4-e969-5006-8b35-504d213c05be</uuid>\n" +
                "    <name>Root</name>\n" +
                "    <report reference=\"../..\"/>\n" +
                "    <childrenBandDefinitions>\n" +
                "      <band>\n" +
                "        <uuid>3ccc1e4f-3621-8d1e-698c-77b051db048c</uuid>\n" +
                "        <name>Description</name>\n" +
                "        <parentBandDefinition reference=\"../../..\"/>\n" +
                "        <report reference=\"../../../..\"/>\n" +
                "        <childrenBandDefinitions/>\n" +
                "        <dataSets>\n" +
                "          <dataSet>\n" +
                "            <uuid>117f787e-b619-6955-c6e8-c1a90fef5232</uuid>\n" +
                "            <name>Description</name>\n" +
                "            <useExistingView>false</useExistingView>\n" +
                "            <text>return [[&quot;test&quot;:&quot;&lt;html&gt;&lt;a href=\\&quot;http://localhost:8080/app\\&quot;&gt;localhost&lt;/a&gt;&lt;/html&gt;&quot;]]</text>\n" +
                "            <type>30</type>\n" +
                "            <entityParamName>entity</entityParamName>\n" +
                "            <listEntitiesParamName>entities</listEntitiesParamName>\n" +
                "            <bandDefinition reference=\"../../..\"/>\n" +
                "          </dataSet>\n" +
                "        </dataSets>\n" +
                "        <orientation>0</orientation>\n" +
                "        <position>0</position>\n" +
                "      </band>\n" +
                "    </childrenBandDefinitions>\n" +
                "    <dataSets/>\n" +
                "    <position>0</position>\n" +
                "  </rootBandDefinition>\n" +
                "  <bands>\n" +
                "    <band reference=\"../../rootBandDefinition/childrenBandDefinitions/band\"/>\n" +
                "    <band reference=\"../../rootBandDefinition\"/>\n" +
                "  </bands>\n" +
                "  <inputParameters/>\n" +
                "  <valuesFormats>\n" +
                "    <format>\n" +
                "      <uuid>d39bff48-08d8-1e8e-b6f5-4429cd0f6501</uuid>\n" +
                "      <valueName>Description.test</valueName>\n" +
                "      <formatString>${html}</formatString>\n" +
                "      <report reference=\"../../..\"/>\n" +
                "    </format>\n" +
                "  </valuesFormats>\n" +
                "  <reportScreens/>\n" +
                "  <roles/>\n" +
                "  <isTmp>true</isTmp>\n" +
                "</report>";

        XStreamConverter xStreamConverter = new XStreamConverter();
        Report report = xStreamConverter.convertToReport(xml);
        System.out.println();
    }

    protected Report createData() {
        Report report = new Report();
        report.setName("Report");
        report.setCode("ReportCode");

        ReportTemplate reportTemplate = new ReportTemplate();
        reportTemplate.setName("Template");
        reportTemplate.setCode("TemplateCode");
        reportTemplate.setReport(report);
        reportTemplate.setReportOutputType(ReportOutputType.CUSTOM);

        report.setDefaultTemplate(reportTemplate);
        report.setTemplates(Collections.singletonList(reportTemplate));

        BandDefinition root = new BandDefinition();
        root.setName("Root");
        DataSet rootDataSet = new DataSet();
        rootDataSet.setView(new View(Report.class));
        root.setDataSets(Collections.singletonList(rootDataSet));
        BandDefinition child1 = new BandDefinition();
        child1.setName("1");
        BandDefinition child2 = new BandDefinition();
        child2.setName("2");
        BandDefinition child3 = new BandDefinition();
        child3.setName("3");
        BandDefinition child4 = new BandDefinition();
        child4.setName("4");
        root.getChildren().add(child1);
        root.getChildren().add(child2);
        root.getChildren().add(child3);
        root.getChildren().add(child4);
        for (BandDefinition bandDefinition : root.getChildrenBandDefinitions()) {
            bandDefinition.setParentBandDefinition(root);
        }

        report.getBands().add(root);
        report.getBands().add(child1);
        report.getBands().add(child2);
        report.getBands().add(child3);
        report.getBands().add(child4);

        return report;
    }


}
