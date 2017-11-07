/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui;

import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.ParameterType;
import com.haulmont.reports.entity.ReportInputParameter;
import junit.framework.Assert;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ReportGuiManagerTest {

    @Mocked ReportService reportService;

    @Test
    public void testParameterConversion() throws Exception {
        String data1 = "data1";
        String data2 = "data2";
        String data3 = "data3";
        final List<String> collection = Arrays.asList(data1, data2, data3);
        final List<String> collection1 = Arrays.asList(data1);

        new NonStrictExpectations() {
            {
                reportService.loadDataForParameterPrototype((ParameterPrototype) any); result = collection;
            }
        };

        ReportGuiManager reportGuiManager = new ReportGuiManager();
        reportGuiManager.reportService = reportService;

        ReportInputParameter entityParameter = new ReportInputParameter();
        ParameterPrototype parameterPrototype = new ParameterPrototype("param");

        entityParameter.setType(ParameterType.ENTITY);

        Object converted = reportGuiManager.convertParameterIfNecessary(entityParameter, collection, false);
        Assert.assertTrue(converted == collection);
        converted = reportGuiManager.convertParameterIfNecessary(entityParameter, Collections.emptyList(), false);
        Assert.assertTrue(converted == null);
        converted = reportGuiManager.convertParameterIfNecessary(entityParameter, null, false);
        Assert.assertTrue(converted == null);
        converted = reportGuiManager.convertParameterIfNecessary(entityParameter, parameterPrototype, false);
        Assert.assertTrue(converted == collection);

        converted = reportGuiManager.convertParameterIfNecessary(entityParameter, collection, true);
        Assert.assertTrue(converted == collection);
        converted = reportGuiManager.convertParameterIfNecessary(entityParameter, Collections.emptyList(), true);
        Assert.assertTrue(converted == null);
        converted = reportGuiManager.convertParameterIfNecessary(entityParameter, parameterPrototype, true);
        Assert.assertTrue(converted == collection);

        entityParameter.setType(ParameterType.ENTITY_LIST);

        converted = reportGuiManager.convertParameterIfNecessary(entityParameter, collection, false);
        Assert.assertTrue(converted == collection);
        converted = reportGuiManager.convertParameterIfNecessary(entityParameter, collection, true);
        Assert.assertTrue(converted == collection);
        converted = reportGuiManager.convertParameterIfNecessary(entityParameter, parameterPrototype, false);
        Assert.assertTrue(converted == parameterPrototype);
        converted = reportGuiManager.convertParameterIfNecessary(entityParameter, parameterPrototype, true);
        Assert.assertTrue(converted == parameterPrototype);
        converted = reportGuiManager.convertParameterIfNecessary(entityParameter, "simpleString", true);
        Assert.assertTrue(converted instanceof Collection);
        Assert.assertTrue(((Collection)converted).iterator().next().equals("simpleString"));
    }
}