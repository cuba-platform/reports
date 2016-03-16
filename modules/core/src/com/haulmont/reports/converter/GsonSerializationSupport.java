/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.converter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.haulmont.bali.util.ReflectionHelper;
import com.haulmont.chile.core.datatypes.Datatype;
import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.chile.core.model.Range;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.PersistenceHelper;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static java.lang.String.format;

/**
 */
@NotThreadSafe
//todo add dynamic attributes support
//todo add not meta property objects support
public class GsonSerializationSupport {
    protected GsonBuilder gsonBuilder;
    protected Map<Object, Entity> processedObjects = new HashMap<>();
    protected ExclusionPolicy exclusionPolicy;

    public interface ExclusionPolicy {
        boolean exclude(Class objectClass, String propertyName);
    }

    public GsonSerializationSupport() {
        gsonBuilder = new GsonBuilder()
                .registerTypeHierarchyAdapter(Entity.class, new TypeAdapter<Entity>() {
                    @Override
                    public void write(JsonWriter out, Entity entity) throws IOException {
                        writeEntity(out, entity);
                    }

                    @Override
                    public Entity read(JsonReader in) throws IOException {
                        return readEntity(in);
                    }
                })
                .registerTypeAdapterFactory(new TypeAdapterFactory() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                        if (Class.class.isAssignableFrom(type.getRawType())) {
                            return (TypeAdapter<T>) new TypeAdapter<Class>() {
                                @Override
                                public void write(JsonWriter out, Class value) throws IOException {
                                    out.value(value.getCanonicalName());
                                }

                                @Override
                                public Class read(JsonReader in) throws IOException {
                                    String className = in.nextString();
                                    return ReflectionHelper.getClass(className);
                                }
                            };
                        } else {
                            return null;
                        }
                    }
                });

    }

    private Entity readEntity(JsonReader in) throws IOException {
        in.beginObject();
        in.nextName();
        String metaClassName = in.nextString();
        Metadata metadata = AppBeans.get(Metadata.NAME);
        MetaClass metaClass = metadata.getSession().getClassNN(metaClassName);
        Entity entity = metadata.create(metaClass);
        in.nextName();
        String id = in.nextString();
        MetaProperty idProperty = metaClass.getPropertyNN("id");
        try {
            entity.setValue("id", Datatypes.getNN(idProperty.getJavaType()).parse(id));
        } catch (ParseException e) {
            throw new RuntimeException(
                    format("An error occurred while parsing id. Class [%s]. Value [%s].", idProperty.getJavaType(), id), e);
        }

        Entity processedObject = processedObjects.get(entity.getId());
        if (processedObject != null) {
            entity = processedObject;
        } else {
            processedObjects.put(entity.getId(), entity);
            readFields(in, metaClass, entity);
        }
        in.endObject();
        return entity;
    }

    protected void readFields(JsonReader in, MetaClass metaClass, Entity entity) throws IOException {
        while (in.hasNext()) {
            String propertyName = in.nextName();
            MetaProperty property = metaClass.getProperty(propertyName);
            if (property != null && !property.isReadOnly() && !exclude(entity.getClass(), propertyName)) {
                Class<?> propertyType = property.getJavaType();
                Range propertyRange = property.getRange();
                if (propertyRange.isDatatype()) {
                    Object value = readSimpleProperty(in, propertyType);
                    entity.setValue(propertyName, value);
                } else if (propertyRange.isClass()) {
                    if (Entity.class.isAssignableFrom(propertyType)) {
                        entity.setValue(propertyName, readEntity(in));
                    } else if (Collection.class.isAssignableFrom(propertyType)) {
                        Collection entities = readCollection(in, propertyType);
                        entity.setValue(propertyName, entities);
                    } else {
                        in.skipValue();
                    }
                } else if (propertyRange.isEnum()) {
                    String stringValue = in.nextString();
                    try {
                        Object value = propertyRange.asEnumeration().parse(stringValue);
                        entity.setValue(propertyName, value);
                    } catch (ParseException e) {
                        throw new RuntimeException(
                                format("An error occurred while parsing enum. Class [%s]. Value [%s].", propertyType, stringValue), e);
                    }
                }
            } else {
                readUnresolvedProperty(entity, propertyName, in);
            }
        }
    }

    protected void readUnresolvedProperty(Entity entity, String propertyName, JsonReader in) throws IOException {
        in.skipValue();
    }

    protected Object readSimpleProperty(JsonReader in, Class<?> propertyType) throws IOException {
        String value = in.nextString();
        Object parsedValue = null;
        try {
            Datatype<?> datatype = Datatypes.get(propertyType);
            if (datatype != null) {
                parsedValue = datatype.parse(value);
            }
            return parsedValue;
        } catch (ParseException e) {
            throw new RuntimeException(
                    format("An error occurred while parsing property. Class [%s]. Value [%s].", propertyType, value), e);
        }
    }

    @SuppressWarnings("unchecked")
    protected Collection readCollection(JsonReader in, Class<?> propertyType) throws IOException {
        Collection entities;
        if (List.class.isAssignableFrom(propertyType)) {
            entities = new ArrayList<>();
        } else if (Set.class.isAssignableFrom(propertyType)) {
            entities = new LinkedHashSet<>();
        } else {
            throw new RuntimeException(format("Could not instantiate collection with class [%s].", propertyType));
        }
        in.beginArray();
        while (in.hasNext()) {
            Entity entityForList = readEntity(in);
            entities.add(entityForList);
        }
        in.endArray();
        return entities;
    }

    @SuppressWarnings("unchecked")
    protected void writeEntity(JsonWriter out, Entity entity) throws IOException {
        out.beginObject();
        Datatype id = Datatypes.getNN(entity.getMetaClass().getPropertyNN("id").getJavaType());
        if (processedObjects.containsKey(entity.getId())) {
            out.name("metaClass");
            out.value(entity.getMetaClass().getName());
            out.name("id");
            out.value(id.format(entity.getId()));
        } else {
            processedObjects.put(entity.getId(), entity);
            out.name("metaClass");
            out.value(entity.getMetaClass().getName());
            out.name("id");
            out.value(id.format(entity.getId()));
            writeFields(out, entity);
        }

        out.endObject();
    }

    @SuppressWarnings("unchecked")
    protected void writeFields(JsonWriter out, Entity entity) throws IOException {
        for (MetaProperty property : entity.getMetaClass().getProperties()) {
            if (!"id".equalsIgnoreCase(property.getName())
                    && !property.isReadOnly()
                    && !exclude(entity.getClass(), property.getName())
                    && PersistenceHelper.isLoaded(entity, property.getName())) {
                Range propertyRange = property.getRange();
                if (propertyRange.isDatatype()) {
                    writeSimpleProperty(out, entity, property);
                } else if (propertyRange.isClass()) {
                    Object value = entity.getValue(property.getName());
                    if (value instanceof Entity) {
                        out.name(property.getName());
                        writeEntity(out, (Entity) value);
                    } else if (value instanceof Collection) {
                        out.name(property.getName());
                        writeCollection(out, (Collection) value);
                    }
                } else if (propertyRange.isEnum()) {
                    out.name(property.getName());
                    out.value(propertyRange.asEnumeration().format(entity.getValue(property.getName())));
                }
            }
        }
    }

    protected void writeCollection(JsonWriter out, Collection value) throws IOException {
        out.beginArray();
        for (Object o : value) {
            if (o instanceof Entity) {
                writeEntity(out, (Entity) o);
            }
        }
        out.endArray();
    }

    protected void writeSimpleProperty(JsonWriter out, Entity entity, MetaProperty property) throws IOException {
        Object value = entity.getValue(property.getName());
        if (value != null) {
            out.name(property.getName());
            Datatype datatype = Datatypes.get(property.getJavaType());
            if (datatype != null) {
                out.value(datatype.format(value));
            } else {
                out.value(String.valueOf(value));
            }
        }
    }

    protected boolean exclude(Class objectClass, String propertyName) {
        return exclusionPolicy != null && exclusionPolicy.exclude(objectClass, propertyName);
    }

    public String convertToString(Entity entity) {
        return gsonBuilder.create().toJson(entity);
    }

    public <T> T convertToReport(String json, Class<T> aClass) {
        return gsonBuilder.create().fromJson(json, aClass);
    }
}
