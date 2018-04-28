/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.entity.pivottable;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Lob;

@MetaClass(name = "report$PivotTableProperty")
public class PivotTableProperty extends BaseUuidEntity {

    @MetaProperty(mandatory = true)
    protected String name;

    @MetaProperty
    protected String caption;

    @Lob
    @MetaProperty
    protected String function;

    @MetaProperty
    protected Boolean hidden = false;

    @MetaProperty
    protected PivotTablePropertyType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (caption == null && StringUtils.isNotEmpty(name)) {
            setCaption(name.replace('_', ' '));
        }
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public PivotTablePropertyType getType() {
        return type;
    }

    public void setType(PivotTablePropertyType type) {
        this.type = type;
    }
}
