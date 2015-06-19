/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.entity.charts;

import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class AbstractChartDescription extends AbstractNotPersistentEntity {
    @MetaProperty
    protected final String type;

    public AbstractChartDescription(String type) {
        this.type = type;
    }

    public ChartType getType() {
        return ChartType.fromId(type);
    }
}
