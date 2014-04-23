/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.entity;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.cuba.core.global.View;
import com.haulmont.yarg.structure.ReportQuery;

import java.util.HashMap;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $id$
 */
@MetaClass(name = "report$DataSet")
@SystemLevel
public class DataSet extends AbstractNotPersistentEntity implements ReportQuery {
    public static final String ENTITY_PARAM_NAME = "entityParamName";
    public static final String LIST_ENTITIES_PARAM_NAME = "listEntitiesParamName";
    private static final long serialVersionUID = -3706206933129963303L;
    //@MetaProperty
    protected View view;
    @MetaProperty
    protected String name;
    @MetaProperty
    protected String text;
    @MetaProperty
    protected Integer type;
    @MetaProperty
    protected String entityParamName;
    @MetaProperty
    protected String listEntitiesParamName;
    @MetaProperty
    protected BandDefinition bandDefinition;
    @MetaProperty
    protected String linkParameterName;

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public DataSetType getType() {
        return type != null ? DataSetType.fromId(type) : null;
    }

    public void setType(DataSetType type) {
        this.type = type != null ? type.getId() : null;
    }

    public String getEntityParamName() {
        return entityParamName;
    }

    public void setEntityParamName(String entityParamName) {
        this.entityParamName = entityParamName;
    }

    public String getListEntitiesParamName() {
        return listEntitiesParamName;
    }

    public void setListEntitiesParamName(String listEntitiesParamName) {
        this.listEntitiesParamName = listEntitiesParamName;
    }

    public BandDefinition getBandDefinition() {
        return bandDefinition;
    }

    public void setBandDefinition(BandDefinition bandDefinition) {
        this.bandDefinition = bandDefinition;
    }

    @Override
    public String getLinkParameterName() {
        return linkParameterName;
    }

    public void setLinkParameterName(String linkParameterName) {
        this.linkParameterName = linkParameterName;
    }

    @Override
    public String getScript() {
        return text;
    }

    @Override
    public String getLoaderType() {
        return getType().getCode();
    }

    @Override
    public Map<String, Object> getAdditionalParams() {
        Map<String, Object> params = new HashMap<>();
        params.put(ENTITY_PARAM_NAME, entityParamName);
        params.put(LIST_ENTITIES_PARAM_NAME, listEntitiesParamName);
        return params;
    }
}