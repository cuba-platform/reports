/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.datasource;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.data.impl.DatasourceImpl;

/**
 */
public class NotPersistenceDatasource<T extends Entity> extends DatasourceImpl<T>{

    @Override
    public void setItem(T item) {
        super.setItem(item);
        modified = false;
    }

}
