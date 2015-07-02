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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class MultiEntityDataLoader extends AbstractEntityDataLoader {

    public static final String DEFAULT_LIST_ENTITIES_PARAM_NAME = "entities";
    public static final String NESTED_COLLECTION_SEPARATOR = "#";

    @Override
    public List<Map<String, Object>> loadData(ReportQuery dataSet, BandData parentBand, Map<String, Object> params) {
        Map<String, Object> additionalParams = dataSet.getAdditionalParams();
        String paramName = (String) additionalParams.get(DataSet.LIST_ENTITIES_PARAM_NAME);
        if (StringUtils.isBlank(paramName)) {
            paramName = DEFAULT_LIST_ENTITIES_PARAM_NAME;
        }

        boolean hasNestedCollection = paramName.contains(NESTED_COLLECTION_SEPARATOR);
        String entityParameterName = StringUtils.substringBefore(paramName, NESTED_COLLECTION_SEPARATOR);
        String nestedCollectionName = StringUtils.substringAfter(paramName, NESTED_COLLECTION_SEPARATOR);

        Object entities = null;
        if (params.containsKey(paramName)) {
            entities = params.get(paramName);
        } else if (hasNestedCollection && params.containsKey(entityParameterName)) {
            Entity entity = (Entity) params.get(entityParameterName);
            entity = reloadEntityByDataSetView(dataSet, entity);
            if (entity != null) {
                entities = entity.getValueEx(nestedCollectionName);
            }
        }

        if (entities == null || !(entities instanceof Collection)) {
            if (hasNestedCollection) {
                throw new IllegalStateException(
                        String.format("Input parameters do not contain '%s' parameter, " +
                                "or the entity does not contain nested collection '%s'", entityParameterName, nestedCollectionName)
                );
            } else {
                throw new IllegalStateException(
                        String.format("Input parameters do not contain '%s' parameter or it has type other than collection", paramName)
                );
            }
        }

        Collection<Entity> entitiesList = (Collection) entities;
        params.put(paramName, entitiesList);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Entity entity : entitiesList) {
            if (!hasNestedCollection) {
                entity = reloadEntityByDataSetView(dataSet, entity);
            }

            resultList.add(new EntityMap(entity));
        }
        return resultList;
    }
}
