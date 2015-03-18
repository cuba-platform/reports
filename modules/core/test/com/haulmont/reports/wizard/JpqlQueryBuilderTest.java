/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.wizard;

import com.haulmont.reports.entity.wizard.EntityTreeNode;
import com.haulmont.reports.entity.wizard.RegionProperty;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.ReportRegion;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author degtyarjov
 * @version $Id$
 */
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
                "queryEntity.vin as \"vin\",\n" +
                "queryEntity.createTs as \"createTs\",\n" +
                "queryEntity.updateTs as \"updateTs\",\n" +
                "queryEntity.deleteTs as \"deleteTs\"\n" +
                "from ref$Car queryEntity", result);
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
                "queryEntity.vin as \"vin\",\n" +
                "queryEntity.createTs as \"createTs\",\n" +
                "queryEntity.updateTs as \"updateTs\",\n" +
                "queryEntity.deleteTs as \"deleteTs\",\n" +
                "model.name as \"model.name\",\n" +
                "colour.name as \"colour.name\",\n" +
                "seller.name as \"seller.name\"\n" +
                "from ref$Car queryEntity  \n" +
                "left join queryEntity.model model \n" +
                "left join queryEntity.colour colour \n" +
                "left join queryEntity.seller seller", result);
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
                "queryEntity.vin as \"vin\",\n" +
                "queryEntity.createTs as \"createTs\",\n" +
                "queryEntity.updateTs as \"updateTs\",\n" +
                "queryEntity.deleteTs as \"deleteTs\",\n" +
                "model.name as \"model.name\",\n" +
                "colour.name as \"colour.name\",\n" +
                "seller.name as \"seller.name\"\n" +
                "from ref$Car queryEntity  \n" +
                "left join queryEntity.model model \n" +
                "left join queryEntity.colour colour \n" +
                "left join queryEntity.seller seller \n" +
                ", in(queryEntity.repairs) repairs where queryEntity.model.id = ${param}", result);
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
