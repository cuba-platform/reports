/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.datasource;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.data.impl.HierarchicalPropertyDatasourceImpl;

import java.util.Collection;
import java.util.Set;

public class BandsHierarchicalPropertyDatasourse<T extends Entity<K>, K> extends HierarchicalPropertyDatasourceImpl<T, K> {
    @Override
    public void committed(Set<Entity> entities) {
        super.committed(entities);
        if (State.VALID.equals(masterDs.getState())) {
            Collection<T> collection = __getCollection();
            if (item != null && collection != null) {
                for (T entity : collection) {
                    if (entity.equals(item)) {
                        item = entity;
                    }
                }
            }
        }
    }
}
