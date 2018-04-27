/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.entity.pivottable;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.UuidProvider;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Lob;

@MetaClass(name = "report$PivotTableAggregation")
public class PivotTableAggregation extends BaseUuidEntity {

    @MetaProperty
    protected AggregationMode mode;

    @MetaProperty
    protected String caption;

    @Lob
    @MetaProperty
    protected String function;

    public PivotTableAggregation() {
        id = UuidProvider.createUuid();
    }

    public PivotTableAggregation(AggregationMode mode) {
        id = UuidProvider.createUuid();
        setMode(mode);
    }

    public AggregationMode getMode() {
        return mode;
    }

    public void setMode(AggregationMode mode) {
        this.mode = mode;
        if (StringUtils.isEmpty(caption)) {
            setCaption(AppBeans.get(Messages.class).getMessage(getClass(), "AggregationMode." + mode.toString()));
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
}
