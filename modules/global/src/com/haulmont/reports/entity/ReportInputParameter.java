/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.entity;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.yarg.structure.ReportParameterWithDefaultValue;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Transient;

/**
 * @author degtyarjov
 * @version $Id$
 */
@MetaClass(name = "report$ReportInputParameter")
@SystemLevel
@NamePattern("%s|locName")
@SuppressWarnings("unused")
public class ReportInputParameter extends AbstractNotPersistentEntity implements ReportParameterWithDefaultValue {
    private static final long serialVersionUID = 6231014880104406246L;

    @MetaProperty
    protected Report report;

    @MetaProperty
    protected Integer type;

    @MetaProperty
    protected String name;

    @MetaProperty
    protected String localeNames;

    @MetaProperty
    protected String alias;

    @MetaProperty
    protected Integer position;

    @MetaProperty
    protected String entityMetaClass;

    @MetaProperty
    protected String enumerationClass;

    @MetaProperty
    protected String screen;

    @MetaProperty
    protected Boolean required = false;

    @MetaProperty
    protected String defaultValue;

    @MetaProperty
    protected String parameterClassName;

    @Transient
    protected String localeName;

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public ParameterType getType() {
        return type != null ? ParameterType.fromId(type) : null;
    }

    public void setType(ParameterType type) {
        this.type = type != null ? type.getId() : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (ObjectUtils.notEqual(name, this.name)) {
            localeName = null;
        }
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getEntityMetaClass() {
        return entityMetaClass;
    }

    public void setEntityMetaClass(String entityMetaClass) {
        this.entityMetaClass = entityMetaClass;
    }

    public String getEnumerationClass() {
        return enumerationClass;
    }

    public void setEnumerationClass(String enumerationClass) {
        this.enumerationClass = enumerationClass;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getLocaleNames() {
        return localeNames;
    }

    public void setLocaleNames(String localeNames) {
        if (ObjectUtils.notEqual(localeNames, this.localeNames)) {
            localeName = null;
        }
        this.localeNames = localeNames;
    }

    @MetaProperty
    public String getLocName() {
        if (localeName == null) {
            localeName = LocaleHelper.getLocalizedName(localeNames);
            if (localeName == null)
                localeName = name;
        }
        return localeName;
    }

    @Override
    public Class getParameterClass() {
        try {
            if (StringUtils.isNotBlank(parameterClassName)) {
                return Class.forName(parameterClassName);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("An error occurred while resolving class name " + parameterClassName, e);
        }

        return null;
    }

    public void setParameterClass(Class parameterClass) {
        if (parameterClass != null) {
            parameterClassName = parameterClass.getName();
        }
    }

    public String getParameterClassName() {
        return parameterClassName;
    }

    public void setParameterClassName(String parameterClassName) {
        this.parameterClassName = parameterClassName;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
