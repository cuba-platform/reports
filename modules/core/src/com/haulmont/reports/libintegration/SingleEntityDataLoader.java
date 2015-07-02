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
import org.apache.commons.lang.StringUtils;

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
        if (StringUtils.isBlank(paramName)) {
            paramName = DEFAULT_ENTITY_PARAM_NAME;
        }

        Object entity = null;
        if (params.containsKey(paramName)) {
            entity = params.get(paramName);
        }

        if (entity == null) {
            throw new IllegalStateException(
                    String.format("Input parameters do not contain '%s' parameter", paramName)
            );
        }

        entity = reloadEntityByDataSetView(dataSet, entity);
        params.put(paramName, entity);
        return Collections.singletonList((Map<String, Object>) new EntityMap((Entity) entity));
    }


}
