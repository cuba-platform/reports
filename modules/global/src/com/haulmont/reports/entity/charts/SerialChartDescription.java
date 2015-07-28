/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.entity.charts;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author degtyarjov
 * @version $Id$
 */
@MetaClass(name = "report$SerialChartDescription")
public class SerialChartDescription extends AbstractChartDescription {
    @MetaProperty(mandatory = true)
    protected String bandName;
    @MetaProperty(mandatory = true)
    protected String categoryField;
    @MetaProperty
    protected String categoryAxisCaption;
    @MetaProperty
    protected String valueAxisCaption;
    @MetaProperty
    protected String valueAxisUnits;
    @MetaProperty
    protected String valueStackType;
    @MetaProperty
    protected List<ChartSeries> series = new ArrayList<>();
    @MetaProperty
    protected Integer categoryAxisLabelRotation = 0;

    public SerialChartDescription() {
        super(ChartType.SERIAL.getId());
    }

    public String getCategoryAxisCaption() {
        return categoryAxisCaption;
    }

    public void setCategoryAxisCaption(String categoryAxisCaption) {
        this.categoryAxisCaption = categoryAxisCaption;
    }

    public String getValueAxisCaption() {
        return valueAxisCaption;
    }

    public void setValueAxisCaption(String valueAxisCaption) {
        this.valueAxisCaption = valueAxisCaption;
    }

    public String getValueAxisUnits() {
        return valueAxisUnits;
    }

    public void setValueAxisUnits(String valueAxisUnits) {
        this.valueAxisUnits = valueAxisUnits;
    }

    public StackType getValueStackType() {
        return StackType.fromId(valueStackType);
    }

    public void setValueStackType(StackType valueStackType) {
        this.valueStackType = valueStackType != null ? valueStackType.getId() : null;
    }

    public List<ChartSeries> getSeries() {
        return series;
    }

    public void setSeries(List<ChartSeries> series) {
        this.series = series;
    }

    public String getCategoryField() {
        return categoryField;
    }

    public void setCategoryField(String categoryField) {
        this.categoryField = categoryField;
    }

    public String getBandName() {
        return bandName;
    }

    public void setBandName(String bandName) {
        this.bandName = bandName;
    }

    public Integer getCategoryAxisLabelRotation() {
        return categoryAxisLabelRotation;
    }

    public void setCategoryAxisLabelRotation(Integer categoryAxisLabelRotation) {
        this.categoryAxisLabelRotation = categoryAxisLabelRotation;
    }
}
