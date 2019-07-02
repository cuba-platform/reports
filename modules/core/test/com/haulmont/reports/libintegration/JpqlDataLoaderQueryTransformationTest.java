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

import com.haulmont.yarg.loaders.impl.AbstractDbDataLoader;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportQuery;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
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
    public void testFindOutputParams() {
        String query = "select t.num as num, t.id as blabla_id from tm$Task t";
        List<OutputValue> outputFields = parseQueryOutputParametersNames(query);
        Assert.assertEquals(2, outputFields.size());
        for (OutputValue outputField : outputFields) {
            Assert.assertTrue(outputField.getValueName().equals("blabla_id") || outputField.getValueName().equals("num"));
        }
    }

    @Test
    public void testReplaceParamsConditions() {
        String query = "select id as id from tm$Task t " +
                "where t.id = ${param1} and t.id > ${param2} and t1.id like ${param3} and t1.id = ${Root.parentBandParam}";

        HashMap<String, Object> params = new HashMap<>();
        params.put("param1", null);
        params.put("param2", null);
        params.put("param3", null);
        AbstractDbDataLoader.QueryPack queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertFalse(queryPack.getQuery().contains("${"));
        Assert.assertEquals(1, StringUtils.countMatches(queryPack.getQuery(), "?"));

        params.put("param1", "123");
        queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertEquals(2, StringUtils.countMatches(queryPack.getQuery(), "?"));

        params.put("param2", "123");
        queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertEquals(3, StringUtils.countMatches(queryPack.getQuery(), "?"));
    }

    @Test
    public void testReplaceIndenticalConditions() {
        String query = "select t from tm$Task t where t.dateStart != ${param} and t.dateEnd != ${param}";

        Map<String, Object> params = new HashMap<>();
        params.put("param", "14.06.2019");

        AbstractDbDataLoader.QueryPack queryPack = prepareQuery(query, new BandData(""), params);
        Assert.assertEquals(2, queryPack.getParams().length);
        Assert.assertEquals(1, StringUtils.countMatches(queryPack.getQuery(), "?1"));
        Assert.assertEquals(1, StringUtils.countMatches(queryPack.getQuery(), "?2"));
    }
}
