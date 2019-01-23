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

package com.haulmont.reports.wizard;

import com.haulmont.reports.entity.wizard.*;
import org.junit.Assert;
import org.junit.Test;

public class JpqlQueryBuilderTest {
    @Test
    public void testSimpleQuery() throws Exception {
        String query = "select queryEntity from ref$Car queryEntity";

        ReportData reportData = new ReportData();
        reportData.setQuery(query);
        ReportRegion reportRegion = new ReportRegion();
        reportData.addRegion(reportRegion);
        EntityTreeNode root = new EntityTreeNode();

        createProperty(reportRegion, root, "vin");
        createProperty(reportRegion, root, "createTs");
        createProperty(reportRegion, root, "updateTs");
        createProperty(reportRegion, root, "deleteTs");

        JpqlQueryBuilder jpqlQueryBuilder = new JpqlQueryBuilder(reportData, reportRegion);
        String result = jpqlQueryBuilder.buildQuery();
        Assert.assertEquals("select\n" +
                "e.vin as \"vin\",\n" +
                "e.createTs as \"createTs\",\n" +
                "e.updateTs as \"updateTs\",\n" +
                "e.deleteTs as \"deleteTs\"\n" +
                "from ref$Car e", result);
    }

    @Test
    public void testQueryWithJoins() throws Exception {
        String query = "select queryEntity from ref$Car queryEntity";

        ReportData reportData = new ReportData();
        reportData.setQuery(query);
        ReportRegion reportRegion = new ReportRegion();
        reportData.addRegion(reportRegion);
        EntityTreeNode root = new EntityTreeNode();

        createProperty(reportRegion, root, "vin");
        createProperty(reportRegion, root, "createTs");
        createProperty(reportRegion, root, "updateTs");
        createProperty(reportRegion, root, "deleteTs");
        createProperty(reportRegion, root, "model.name");
        createProperty(reportRegion, root, "colour.name");
        createProperty(reportRegion, root, "seller.name");

        JpqlQueryBuilder jpqlQueryBuilder = new JpqlQueryBuilder(reportData, reportRegion);
        String result = jpqlQueryBuilder.buildQuery();
        Assert.assertEquals("select\n" +
                "e.vin as \"vin\",\n" +
                "e.createTs as \"createTs\",\n" +
                "e.updateTs as \"updateTs\",\n" +
                "e.deleteTs as \"deleteTs\",\n" +
                "model.name as \"model.name\",\n" +
                "colour.name as \"colour.name\",\n" +
                "seller.name as \"seller.name\"\n" +
                "from ref$Car e  \n" +
                "left join e.model model \n" +
                "left join e.colour colour \n" +
                "left join e.seller seller", result);
    }

    @Test
    public void testQueryWithJoinsAndWhere() throws Exception {
        String query = "select queryEntity from ref$Car queryEntity, in(queryEntity.repairs) repairs where queryEntity.model.id = ${param}";

        ReportData reportData = new ReportData();
        reportData.setQuery(query);
        ReportRegion reportRegion = new ReportRegion();
        reportData.addRegion(reportRegion);
        EntityTreeNode root = new EntityTreeNode();

        createProperty(reportRegion, root, "vin");
        createProperty(reportRegion, root, "createTs");
        createProperty(reportRegion, root, "updateTs");
        createProperty(reportRegion, root, "deleteTs");
        createProperty(reportRegion, root, "model.name");
        createProperty(reportRegion, root, "colour.name");
        createProperty(reportRegion, root, "seller.name");

        JpqlQueryBuilder jpqlQueryBuilder = new JpqlQueryBuilder(reportData, reportRegion);
        String result = jpqlQueryBuilder.buildQuery();
        Assert.assertEquals("select\n" +
                "e.vin as \"vin\",\n" +
                "e.createTs as \"createTs\",\n" +
                "e.updateTs as \"updateTs\",\n" +
                "e.deleteTs as \"deleteTs\",\n" +
                "model.name as \"model.name\",\n" +
                "colour.name as \"colour.name\",\n" +
                "seller.name as \"seller.name\"\n" +
                "from ref$Car e  \n" +
                "left join e.model model \n" +
                "left join e.colour colour \n" +
                "left join e.seller seller \n" +
                ", in(e.repairs) repairs where e.model.id = ${param}", result);
    }

    @Test
    public void testDefaultOrderByForChartsTemplate() throws Exception {
        String query = "select queryEntity from ref$Car queryEntity";
        ReportData reportData = new ReportData();
        reportData.setQuery(query);
        reportData.setReportType(ReportData.ReportType.LIST_OF_ENTITIES_WITH_QUERY);
        reportData.setTemplateFileType(TemplateFileType.CHART);
        ReportRegion reportRegion = new ReportRegion();
        reportData.addRegion(reportRegion);
        EntityTreeNode root = new EntityTreeNode();

        createProperty(reportRegion, root, "vin");
        JpqlQueryBuilder jpqlQueryBuilder = new JpqlQueryBuilder(reportData, reportRegion);
        String result = jpqlQueryBuilder.buildQuery();
        Assert.assertEquals("select\n" +
                "e.vin as \"vin\"\n" +
                "from ref$Car e order by e.vin", result);
    }

    protected void createProperty(ReportRegion reportRegion, EntityTreeNode root, String name) {
        RegionProperty regionProperty = new RegionProperty();
        EntityTreeNode propertyTreeNode = new EntityTreeNode();
        propertyTreeNode.setParent(root);
        propertyTreeNode.setName(name);
        regionProperty.setEntityTreeNode(propertyTreeNode);
        reportRegion.getRegionProperties().add(regionProperty);
    }
}
