/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.reports.app.EntityMap;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportQuery;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class SingleEntityDataLoader extends AbstractEntityDataLoader {

    public static final String DEFAULT_ENTITY_PARAM_NAME = "entity";

    @Override
    public List<Map<String, Object>> loadData(ReportQuery dataSet, BandData parentBand, Map<String, Object> params) {
        Map<String, Object> additionalParams = dataSet.getAdditionalParams();
        String paramName = (String) additionalParams.get(DataSet.ENTITY_PARAM_NAME);

        Object entity;
        if (params.containsKey(paramName)) {
            entity = params.get(paramName);
        } else {
            entity = params.get(DEFAULT_ENTITY_PARAM_NAME);
        }

        if (entity == null) {
            throw new IllegalStateException("Input parameters don't contain 'entity' param");
        } else {
            entity = reloadEntityByDataSetView(dataSet, entity);
        }

        return Collections.singletonList((Map<String, Object>) new EntityMap((Entity) entity));
    }


}
