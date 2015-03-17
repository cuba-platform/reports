/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.report.run;

import com.google.common.collect.ImmutableMap;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.reports.entity.ParameterType;
import com.haulmont.reports.entity.ReportInputParameter;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class ParameterClassResolver {
    protected Map<ParameterType, Class> primitiveParameterTypeMapping = new ImmutableMap.Builder<ParameterType, Class>()
            .put(ParameterType.BOOLEAN, Boolean.class)
            .put(ParameterType.DATE, Date.class)
            .put(ParameterType.DATETIME, Date.class)
            .put(ParameterType.TEXT, String.class)
            .put(ParameterType.NUMERIC, Double.class)
            .put(ParameterType.TIME, Date.class)
            .build();

    protected Map<Class, ParameterType> primitiveParameterClassMapping = new ImmutableMap.Builder<Class, ParameterType>()
            .put(Boolean.class, ParameterType.BOOLEAN)
            .put(Date.class, ParameterType.DATE)
            .put(String.class, ParameterType.TEXT)
            .put(Double.class, ParameterType.NUMERIC)
            .put(Integer.class, ParameterType.NUMERIC)
            .put(BigDecimal.class, ParameterType.NUMERIC)
            .put(Float.class, ParameterType.NUMERIC)
            .put(Long.class, ParameterType.NUMERIC)
            .build();

    private Scripting scripting = AppBeans.get(Scripting.class);

    private Metadata metadata = AppBeans.get(Metadata.class);

    @Nullable
    public Class resolveClass(ReportInputParameter parameter) {
        Class aClass = primitiveParameterTypeMapping.get(parameter.getType());
        if (aClass == null) {
            if (parameter.getType() == ParameterType.ENTITY) {
                MetaClass metaClass = metadata.getSession().getClass(parameter.getEntityMetaClass());
                if (metaClass != null) {
                    return metaClass.getJavaClass();
                } else {
                    return null;
                }
            } else if (parameter.getType() == ParameterType.ENUMERATION) {
                if (StringUtils.isNotBlank(parameter.getEnumerationClass())) {
                    return scripting.loadClass(parameter.getEnumerationClass());
                }
            }
        }

        return aClass;
    }

    @Nullable
    public ParameterType resolveParameterType(@Nullable Class parameterClass) {
        if (parameterClass == null) {
            return null;
        }

        ParameterType parameterType = primitiveParameterClassMapping.get(parameterClass);
        if (parameterType != null) {
            return parameterType;
        } else {
            if (Entity.class.isAssignableFrom(parameterClass)) {
                return ParameterType.ENTITY;
            } else if (Collection.class.isAssignableFrom(parameterClass)) {
                return ParameterType.ENTITY_LIST;
            } else if (Enum.class.isAssignableFrom(parameterClass)) {
                return ParameterType.ENUMERATION;
            }
        }

        return null;
    }
}
