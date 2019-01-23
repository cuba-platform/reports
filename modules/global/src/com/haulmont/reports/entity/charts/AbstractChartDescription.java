/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
