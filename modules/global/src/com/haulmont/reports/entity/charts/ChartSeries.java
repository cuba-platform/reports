/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.entity.charts;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;

/**
 * @author degtyarjov
 * @version $Id$
 */
@MetaClass(name = "report$ChartSeries")
public class ChartSeries extends AbstractNotPersistentEntity {
    @MetaProperty
    protected String name;
    @MetaProperty(mandatory = true)
    protected String type;
    @MetaProperty(mandatory = true)
    protected String valueField;
    @MetaProperty
    protected String colorField;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SeriesType getType() {
        return SeriesType.fromId(type);
    }

    public void setType(SeriesType type) {
        this.type = type != null ? type.getId() : null;
    }

    public String getValueField() {
        return valueField;
    }

    public void setValueField(String valueField) {
        this.valueField = valueField;
    }

    public String getColorField() {
        return colorField;
    }

    public void setColorField(String colorField) {
        this.colorField = colorField;
    }
}
