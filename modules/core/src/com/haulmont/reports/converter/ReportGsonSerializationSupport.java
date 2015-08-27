/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.converter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.View;
import com.haulmont.reports.entity.DataSet;

import java.io.IOException;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class ReportGsonSerializationSupport extends GsonSerializationSupport {
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
