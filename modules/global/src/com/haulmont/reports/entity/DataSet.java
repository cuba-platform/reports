/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.entity;

import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.yarg.structure.ReportQuery;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $id$
 */
@Entity(name = "report$DataSet")
@Table(name = "REPORT_DATA_SET")
@SystemLevel
public class DataSet extends BaseReportEntity implements ReportQuery {
    private static final long serialVersionUID = -3706206933129963303L;
    public static final String ENTITY_PARAM_NAME = "entityParamName";
    public static final String LIST_ENTITIES_PARAM_NAME = "listEntitiesParamName";

    @Column(name = "NAME")
    private String name;

    @Column(name = "TEXT", length = 0)
    private String text;

    @Column(name = "DATA_SET_TYPE")
    private Integer type;

    @Column(name = "ENTITY_PARAM_NAME")
    private String entityParamName;

    @Column(name = "LIST_ENTITIES_PARAM_NAME")
    private String listEntitiesParamName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BAND_DEFINITION")
    private BandDefinition bandDefinition;

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
        return null; //todo create persistent field
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