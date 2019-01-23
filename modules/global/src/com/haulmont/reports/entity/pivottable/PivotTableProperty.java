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

package com.haulmont.reports.entity.pivottable;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Lob;

@MetaClass(name = "report$PivotTableProperty")
public class PivotTableProperty extends BaseUuidEntity {

    @MetaProperty(mandatory = true)
    protected String name;

    @MetaProperty
    protected String caption;

    @Lob
    @MetaProperty
    protected String function;

    @MetaProperty
    protected Boolean hidden = false;

    @MetaProperty
    protected PivotTablePropertyType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (StringUtils.isNotEmpty(name)) {
            setCaption(StringUtils.capitalize(name.replace('_', ' ')));
        }
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public PivotTablePropertyType getType() {
        return type;
    }

    public void setType(PivotTablePropertyType type) {
        this.type = type;
    }
}
