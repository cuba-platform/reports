/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.reports.app.EntityMap;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ProxyWrapper;
import com.haulmont.yarg.structure.ReportQuery;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SingleEntityDataLoader extends AbstractEntityDataLoader {

    public static final String DEFAULT_ENTITY_PARAM_NAME = "entity";

    @Override
    public List<Map<String, Object>> loadData(ReportQuery dataSet, BandData parentBand, Map<String, Object> params) {
        Map<String, Object> additionalParams = dataSet.getAdditionalParams();
        String paramName = (String) additionalParams.get(DataSet.ENTITY_PARAM_NAME);
        if (StringUtils.isBlank(paramName)) {
            paramName = DEFAULT_ENTITY_PARAM_NAME;
        }

        Object entity = params.get(paramName);

        if (entity == null) {
            throw new IllegalStateException(
                    String.format("Input parameters do not contain '%s' parameter", paramName)
            );
        }

        dataSet = ProxyWrapper.unwrap(dataSet);
        entity = reloadEntityByDataSetView(dataSet, entity);
        params.put(paramName, entity);

        EntityMap result;
        if (dataSet instanceof DataSet) {
            result = new EntityMap((Entity) entity, getView((Entity)entity, (DataSet) dataSet));
        } else {
            result = new EntityMap((Entity) entity);
        }
        return Collections.singletonList(result);
    }


}
