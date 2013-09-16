/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.datasource;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.data.impl.DatasourceImpl;

/**
 * @author devyatkin
 * @version $Id$
 */
public class NotPersistenceDatasource<T extends Entity> extends DatasourceImpl<T>{

    @Override
    public void setItem(T item) {
        super.setItem(item);
        modified = false;
    }

}
