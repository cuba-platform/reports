/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.loaders;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.reports.app.EntityMap;
import com.haulmont.reports.entity.Band;
import com.haulmont.reports.entity.DataSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class SingleEntityDataLoader extends AbstractDbDataLoader {
    public SingleEntityDataLoader(Map<String, Object> params) {
        super(params);
    }

    @Override
    public List<Map<String, Object>> loadData(DataSet dataSet, Band parentBand) {

        String paramName = dataSet.getEntityParamName();
        Object entity;
        if (params.containsKey(paramName))
            entity = params.get(paramName);
        else
            entity = params.get("entity");

        if (entity == null)
            throw new IllegalStateException("Input parameters don't contain 'entity' param");

        return Collections.singletonList((Map<String, Object>) new EntityMap((Entity) entity));
    }
}