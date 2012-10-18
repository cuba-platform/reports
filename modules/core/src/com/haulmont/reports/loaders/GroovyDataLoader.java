/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.loaders;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.reports.entity.Band;
import com.haulmont.reports.entity.DataSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs groovy script from data set. Script must return List<Map<String, Object>>
 *
 * @author degtyarjov
 * @version $Id$
 */
public class GroovyDataLoader implements DataLoader {
    private Map<String, Object> params = new HashMap<>();

    public GroovyDataLoader(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public List<Map<String, Object>> loadData(DataSet dataSet, Band parentBand) {
        String script = dataSet.getText();
        Map<String, Object> params = new HashMap<>();
        params.put("dataSet", dataSet);
        params.put("parentBand", parentBand);
        params.put("params", this.params);
        return AppBeans.get(Scripting.class).evaluateGroovy(script, params);
    }
}
