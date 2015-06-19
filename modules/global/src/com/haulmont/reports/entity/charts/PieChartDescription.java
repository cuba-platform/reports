/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.entity.charts;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;

/**
 * @author degtyarjov
 * @version $Id$
 */
@MetaClass(name = "report$PieChartDescriptor")
public class PieChartDescription extends AbstractChartDescription {
    @MetaProperty(mandatory = true)
    protected String bandName;
    @MetaProperty(mandatory = true)
    protected String titleField;
    @MetaProperty(mandatory = true)
    protected String valueField;
    @MetaProperty
    protected String colorField;
    @MetaProperty
    protected Boolean showLegend;
    @MetaProperty
    protected String units;

    public PieChartDescription() {
        super(ChartType.PIE.getId());
    }

    public String getBandName() {
        return bandName;
    }

    public void setBandName(String bandName) {
        this.bandName = bandName;
    }

    public String getTitleField() {
        return titleField;
    }

    public void setTitleField(String titleField) {
        this.titleField = titleField;
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

    public Boolean getShowLegend() {
        return showLegend;
    }

    public void setShowLegend(Boolean showLegend) {
        this.showLegend = showLegend;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }
}
