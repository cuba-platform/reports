/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.reports.app.EntityMap;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.structure.ReportQuery;
import com.haulmont.yarg.structure.BandData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class MultiEntityDataLoader implements ReportDataLoader {
    public static final String DEFAULT_LIST_ENTITIES_PARAM_NAME = "entities";

    @Override
    public List<Map<String, Object>> loadData(ReportQuery dataSet, BandData parentBand, Map<String, Object> params) {
        Map<String, Object> additionalParams = dataSet.getAdditionalParams();
        String paramName = (String) additionalParams.get(DataSet.LIST_ENTITIES_PARAM_NAME);
        Object entities;

        if (params.containsKey(paramName)) {
            entities = params.get(paramName);
        } else {
            entities = params.get(DEFAULT_LIST_ENTITIES_PARAM_NAME);
        }

        if (entities == null || !(entities instanceof Collection)) {
            throw new IllegalStateException(
                    "Input parameters don't contain 'entities' param or it isn't a collection");
        }
        Collection<Entity> entitiesList = (Collection) entities;
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Entity entity : entitiesList) {
            resultList.add(new EntityMap(entity));
        }
        return resultList;
    }
}
