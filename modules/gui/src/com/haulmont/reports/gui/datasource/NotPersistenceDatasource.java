/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
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
