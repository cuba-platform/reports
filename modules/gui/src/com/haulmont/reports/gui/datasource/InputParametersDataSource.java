/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.datasource;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.data.impl.CollectionPropertyDatasourceImpl;
import com.haulmont.reports.entity.ReportInputParameter;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class InputParametersDataSource extends CollectionPropertyDatasourceImpl<ReportInputParameter, UUID> {
    @Override
    public void committed(Set<Entity> entities) {
        super.committed(entities);
        if (State.VALID.equals(masterDs.getState())) {
            Collection<ReportInputParameter> collection = getCollection();
            if (item != null && collection != null) {
                for (ReportInputParameter entity : collection) {
                    if (entity.equals(item)) {
                        item = entity;
                    }
                }
            }
        }
    }
}
