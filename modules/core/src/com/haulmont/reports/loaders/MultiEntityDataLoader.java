/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.loaders;

import com.haulmont.reports.app.EntityMap;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.reports.entity.Band;
import com.haulmont.cuba.core.entity.Entity;

import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class MultiEntityDataLoader extends AbstractDbDataLoader {
    public MultiEntityDataLoader(Map<String, Object> params) {
        super(params);
    }

    @Override
    public List<Map<String, Object>> loadData(DataSet dataSet, Band parentBand) {

        String paramName = dataSet.getListEntitiesParamName();
        Object entities;
        if (params.containsKey(paramName))
            entities = params.get(paramName);
        else
            entities = params.get("entities");

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
