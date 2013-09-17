/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: degtyarjov
 * Created: 17.09.13 12:41
 *
 * $Id$
 */
package com.haulmont.reports.libintegration;

import com.haulmont.yarg.loaders.impl.AbstractDbDataLoader;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportQuery;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JpqlDataLoaderQueryTransformationTest extends JpqlDataDataLoader {

    @Override
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, BandData parentBand, Map<String, Object> params) {
        return null;
    }

    @Test
    public void testFindOutputParams() throws Exception {
        String query = "select \n" +
                "t.num as num, \n" +
                "t.id\n" +
                "as    blabla_id \n" +
                "from tm$Task t";
        List<String> outputFields = parseQueryOutputParametersNames(query);
        System.out.println(outputFields);
        Assert.assertEquals(2, outputFields.size());
        Assert.assertTrue(outputFields.contains("blabla_id"));
        Assert.assertTrue(outputFields.contains("num"));
    }

    @Test
    public void testReplaceParamsConditions() throws Exception {
        String query = "select id as id\n" +
                "from tm$Task t\n" +
                "where t.id  =  ${param1} and t.id  >  ${param2} and t1.id like \n" +
                "${param3}";

        HashMap<String, Object> params = new HashMap<>();
        params.put("param1", null);
        params.put("param2", null);
        params.put("param3", null);
        AbstractDbDataLoader.QueryPack queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertFalse(queryPack.getQuery().contains("${"));
        params.put("param1", "123");
        queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertEquals(1, StringUtils.countMatches(queryPack.getQuery(), "?"));

        params.put("param2", "123");
        queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertEquals(2, StringUtils.countMatches(queryPack.getQuery(), "?"));
    }
}
