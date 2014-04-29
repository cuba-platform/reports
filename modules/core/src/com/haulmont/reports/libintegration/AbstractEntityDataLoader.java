/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.reports.ReportingApi;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.structure.ReportQuery;

/**
 * @author fedorchenko
 * @version $Id$
 */
public abstract class AbstractEntityDataLoader implements ReportDataLoader {
    protected Entity reloadEntityByDataSetView(ReportQuery dataSet, Object entity) {
        if (entity instanceof Entity &&
                dataSet instanceof DataSet &&
                ((DataSet) dataSet).getView() != null) {
            ReportingApi reportingApi = AppBeans.get(ReportingApi.NAME);
            if (reportingApi != null) {
                entity = reportingApi.reloadEntity((Entity) entity, ((DataSet) dataSet).getView());
            }
        }
        return (Entity) entity;
    }
}
