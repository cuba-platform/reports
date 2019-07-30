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

package com.haulmont.reports.entity.table;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;

import java.util.LinkedList;
import java.util.List;


@MetaClass(name = "report$TemplateTableBand")
public class TemplateTableBand extends BaseUuidEntity {

    @MetaProperty
    protected String bandName;

    @MetaProperty
    protected Integer position;

    @MetaProperty
    protected List<TemplateTableColumn> columns = new LinkedList<>();

    public List<TemplateTableColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<TemplateTableColumn> columns) {
        this.columns = columns;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getBandName() {
        return bandName;
    }

    public void setBandName(String bandName) {
        this.bandName = bandName;
    }
}
