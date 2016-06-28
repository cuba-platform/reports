/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.converter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.View;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportTemplate;

import java.io.IOException;

public class ReportGsonSerializationSupport extends GsonSerializationSupport {
    public ReportGsonSerializationSupport() {
        exclusionPolicy = (objectClass, propertyName) ->
                Report.class.isAssignableFrom(objectClass) && "xml".equalsIgnoreCase(propertyName)
                        || ReportTemplate.class.isAssignableFrom(objectClass) && "content".equals(propertyName);
    }

    @Override
    protected void writeFields(JsonWriter out, Entity entity) throws IOException {
        super.writeFields(out, entity);
        if (entity instanceof DataSet) {
            out.name("view");
            out.value(gsonBuilder.create().toJson(((DataSet) entity).getView()));
        }
    }

    @Override
    protected void readUnresolvedProperty(Entity entity, String propertyName, JsonReader in) throws IOException {
        if (entity instanceof DataSet && "view".equals(propertyName)) {
            String viewDefinition = in.nextString();
            View view = gsonBuilder.create().fromJson(viewDefinition, View.class);
            ((DataSet) entity).setView(view);
        } else {
            super.readUnresolvedProperty(entity, propertyName, in);
        }
    }
}
