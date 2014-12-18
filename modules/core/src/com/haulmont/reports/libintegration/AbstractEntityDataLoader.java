/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
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
import org.apache.commons.lang.StringUtils;

/**
 * @author fedorchenko
 * @version $Id$
 */
public abstract class AbstractEntityDataLoader implements ReportDataLoader {
    protected Entity reloadEntityByDataSetView(ReportQuery reportQuery, Object inputObject) {
        Entity entity = null;
        if (inputObject instanceof Entity && reportQuery instanceof DataSet) {
            entity = (Entity) inputObject;
            DataSet dataSet = (DataSet) reportQuery;

            View view = null;
            if (Boolean.TRUE.equals(dataSet.getUseExistingView())) {
                ViewRepository viewRepository = AppBeans.get(ViewRepository.NAME);
                view = viewRepository.getView(entity.getClass(), dataSet.getViewName());
            } else {
                view = dataSet.getView();
            }

            if (view != null) {
                ReportingApi reportingApi = AppBeans.get(ReportingApi.NAME);
                entity = reportingApi.reloadEntity(entity, view);
            }
        }

        return entity;
    }
}
