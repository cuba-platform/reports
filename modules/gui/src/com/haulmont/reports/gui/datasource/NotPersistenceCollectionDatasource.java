/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.datasource;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;

import java.util.Map;

/**
 * @author devyatkin
 * @version $Id$
 */
public class NotPersistenceCollectionDatasource<T extends Entity<K>, K> extends CollectionDatasourceImpl<T, K> {
    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void refresh(Map<String, Object> parameters) {
        return;
    }
}
