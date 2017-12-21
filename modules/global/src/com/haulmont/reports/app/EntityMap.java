/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports.app;

import com.haulmont.chile.core.model.Instance;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EntityMap implements Map<String, Object> {
    private static final Logger log = LoggerFactory.getLogger(EntityMap.class);

    public static final String INSTANCE_NAME_KEY = "_instanceName";

    protected Instance instance;
    protected View view;
    protected HashMap<String, Object> explicitData;

    protected boolean loaded = false;

    public EntityMap(Entity entity) {
        instance = entity;
        explicitData = new HashMap<>();
    }

    public EntityMap(Entity entity, View loadedAttributes) {
        this(entity);
        view = loadedAttributes;
    }

    @Override
    public int size() {
        return explicitData.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        if (explicitData.containsKey(key)) {
            return true;
        } else {
            MetaClass metaClass = instance.getMetaClass();
            for (MetaProperty property : metaClass.getProperties()) {
                if (ObjectUtils.equals(property.getName(), key))
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        loadAllProperties();
        return explicitData.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        Object value = getValue(instance, key);

        if (value != null) return value;

        return explicitData.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return explicitData.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return explicitData.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        explicitData.putAll(m);
    }

    @Override
    public void clear() {
        explicitData.clear();
    }

    @Override
    public Set<String> keySet() {
        loadAllProperties();
        return explicitData.keySet();
    }

    @Override
    public Collection<Object> values() {
        loadAllProperties();
        return explicitData.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        loadAllProperties();
        return explicitData.entrySet();
    }

    protected void loadAllProperties() {
        if (!loaded) {
            MetaClass metaClass = instance.getMetaClass();
            Metadata metadata = AppBeans.get(Metadata.class);
            String pkName = metadata.getTools().getPrimaryKeyName(metaClass);
            for (MetaProperty property : metaClass.getProperties()) {
                if (view != null && view.getProperty(property.getName()) != null) {
                    explicitData.put(property.getName(), getValue(instance, property.getName()));
                } else if (view != null && Objects.equals(pkName, property.getName())) {
                    explicitData.put(property.getName(), getValue(instance, property.getName()));
                } else if (view == null) {
                    explicitData.put(property.getName(), getValue(instance, property.getName()));
                }
            }

            explicitData.put(INSTANCE_NAME_KEY, instance.getInstanceName());

            loaded = true;
        }
    }

    protected Object getValue(Instance instance, Object key) {
        if (key == null) return null;

        String path = String.valueOf(key);
        if (path.endsWith(INSTANCE_NAME_KEY)) {
            if (StringUtils.isNotBlank(path.replace(INSTANCE_NAME_KEY, ""))) {
                Object value = getValue(instance, path.replace("." + INSTANCE_NAME_KEY, ""));
                if (value instanceof Instance) {
                    return ((Instance) value).getInstanceName();
                }
            } else {
                return instance.getInstanceName();
            }
        }

        Object value = null;
        try {
            value = instance.getValue(path);
        } catch (Exception e) {
            log.trace("Suppressed error from underlying EntityMap instance.getValue", e);
        }

        if (value == null) {
            try {
                value = instance.getValueEx(path);
            } catch (Exception e) {
                log.trace("Suppressed error from underlying EntityMap instance.getValue", e);
            }
        }
        return value;
    }

    public Instance getInstance() {
        return instance;
    }
}