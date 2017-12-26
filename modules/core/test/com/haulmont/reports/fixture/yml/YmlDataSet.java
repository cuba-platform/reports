/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.fixture.yml;


import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.reports.entity.DataSetType;
import com.haulmont.reports.entity.JsonSourceType;

/**
 * Jackson YAML format {@link DataSet} description
 */
public class YmlDataSet extends DataSet {

    @Override
    @JsonIgnore
    public String getScript() {
        return super.getScript();
    }

    @Override
    @JsonIgnore
    public String getLoaderType() {
        return super.getLoaderType();
    }

    @Override
    @JsonProperty("entity-param")
    public String getEntityParamName() {
        return super.getEntityParamName();
    }

    @Override
    @JsonProperty("list-entities-param")
    public String getListEntitiesParamName() {
        return super.getListEntitiesParamName();
    }

    @Override
    @JsonProperty("json-type")
    public JsonSourceType getJsonSourceType() {
        return super.getJsonSourceType();
    }

    @Override
    @JsonProperty("json-path")
    public String getJsonPathQuery() {
        return super.getJsonPathQuery();
    }

    @JsonAnySetter
    public void set(String name, Object value) {
        DataSetType type = DataSetType.fromCode(name);
        if (type != null) {
           setType(type);
           setText(value.toString());
        }
        if (DataSetType.JSON == type) {
            setJsonSourceText(value.toString());
        }
    }
}
