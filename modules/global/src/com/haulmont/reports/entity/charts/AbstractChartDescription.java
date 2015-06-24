/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.entity.charts;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.haulmont.bali.util.Preconditions;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;
import com.haulmont.reports.entity.ReportOutputType;

import javax.annotation.Nullable;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class AbstractChartDescription extends AbstractNotPersistentEntity {
    @MetaProperty
    protected final String type;

    @Nullable
    public static AbstractChartDescription fromJsonString(String jsonString) {
        Preconditions.checkNotNullArgument(jsonString);
        Gson gson = new Gson();
        JsonObject jsonElement = gson.fromJson(jsonString, JsonObject.class);
        JsonPrimitive type = jsonElement.getAsJsonPrimitive("type");
        if (ChartType.PIE.getId().equals(type.getAsString())) {
            return gson.fromJson(jsonString, PieChartDescription.class);
        } else if (ChartType.SERIAL.getId().equals(type.getAsString())) {
            return gson.fromJson(jsonString, SerialChartDescription.class);
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
}
