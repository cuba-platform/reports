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

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.global.ViewRepository;
import com.haulmont.reports.ReportingApi;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.structure.ReportQuery;

public abstract class AbstractEntityDataLoader implements ReportDataLoader {
    protected Entity reloadEntityByDataSetView(ReportQuery reportQuery, Object inputObject) {
        Entity entity = null;
        if (inputObject instanceof Entity && reportQuery instanceof DataSet) {
            entity = (Entity) inputObject;
            DataSet dataSet = (DataSet) reportQuery;
            View view = getView(entity, dataSet);
            if (view != null) {
                ReportingApi reportingApi = AppBeans.get(ReportingApi.NAME);
                entity = reportingApi.reloadEntity(entity, view);
            }
        }

        return entity;
    }

    protected View getView(Entity entity, DataSet dataSet) {
        View view;
        if (Boolean.TRUE.equals(dataSet.getUseExistingView())) {
            ViewRepository viewRepository = AppBeans.get(ViewRepository.NAME);
            view = viewRepository.getView(entity.getClass(), dataSet.getViewName());
        } else {
            view = dataSet.getView();
        }
        return view;
    }
}
