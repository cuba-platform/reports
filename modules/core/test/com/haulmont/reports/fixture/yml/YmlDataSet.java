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
