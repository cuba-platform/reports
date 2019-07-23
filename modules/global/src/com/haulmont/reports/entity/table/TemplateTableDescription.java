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

package com.haulmont.reports.entity.table;

import com.google.gson.*;
import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.reports.entity.pivottable.AggregationMode;
import com.haulmont.reports.entity.pivottable.RendererType;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

@MetaClass(name = "report$TemplateTableDescription")
public class TemplateTableDescription extends BaseUuidEntity {

    protected final static Gson gson;

    static {
        gson = new GsonBuilder()
                .registerTypeAdapter(RendererType.class, new TemplateTableDescription.RendererTypeAdapter())
                .registerTypeAdapter(AggregationMode.class, new TemplateTableDescription.AggregationTypeAdapter())
                .create();
    }

    @MetaProperty
    protected List<TemplateTableBand> templateTableBands = new LinkedList<>();

    public List<TemplateTableBand> getTemplateTableBands() {
        return templateTableBands;
    }

    public void setTemplateTableBands(List<TemplateTableBand> templateTableBands) {
        this.templateTableBands = templateTableBands;
    }

    public static String toJsonString(TemplateTableDescription description) {
        return gson.toJson(description);
    }

    public static TemplateTableDescription fromJsonString(String json) {
        try {
            return gson.fromJson(json, TemplateTableDescription.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    protected static class RendererTypeAdapter implements JsonSerializer<RendererType>, JsonDeserializer<RendererType> {
        @Override
        public JsonElement serialize(RendererType src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getId());
        }

        @Override
        public RendererType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return RendererType.fromId(json.getAsString());
        }
    }

    protected static class AggregationTypeAdapter implements JsonSerializer<AggregationMode>, JsonDeserializer<AggregationMode> {
        @Override
        public JsonElement serialize(AggregationMode src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getId());
        }

        @Override
        public AggregationMode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return AggregationMode.fromId(json.getAsString());
        }
    }
}
