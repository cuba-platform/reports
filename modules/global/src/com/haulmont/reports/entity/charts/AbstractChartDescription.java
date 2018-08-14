/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.entity.charts;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.haulmont.bali.util.Preconditions;
import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;

import javax.annotation.Nullable;

@MetaClass(name = "report$AbstractChartDescription")
@SystemLevel
public abstract class AbstractChartDescription extends BaseUuidEntity {

    private static final long serialVersionUID = 3418759346397067914L;

    @MetaProperty
    protected final String type;
    @MetaProperty
    protected Boolean showLegend;

    @Nullable
    public static AbstractChartDescription fromJsonString(String jsonString) {
        Preconditions.checkNotNullArgument(jsonString);
        Gson gson = new Gson();
        JsonObject jsonElement;

        try {
            jsonElement = gson.fromJson(jsonString, JsonObject.class);
        } catch (JsonSyntaxException e) {
            return null;
        }

        if (jsonElement != null) {
            JsonPrimitive type = jsonElement.getAsJsonPrimitive("type");

            if (type == null) {
                return null;
            }

            if (ChartType.PIE.getId().equals(type.getAsString())) {
                return gson.fromJson(jsonString, PieChartDescription.class);
            } else if (ChartType.SERIAL.getId().equals(type.getAsString())) {
                return gson.fromJson(jsonString, SerialChartDescription.class);
            }
        }

        return null;
    }

    public static String toJsonString(AbstractChartDescription chartDescription) {
        Preconditions.checkNotNullArgument(chartDescription);
        Gson gson = new Gson();
        String jsonString = gson.toJson(chartDescription);
        return jsonString;
    }

    public AbstractChartDescription(String type) {
        this.type = type;
    }

    public ChartType getType() {
        return ChartType.fromId(type);
    }

    public Boolean getShowLegend() {
        return showLegend;
    }

    public void setShowLegend(Boolean showLegend) {
        this.showLegend = showLegend;
    }
}
