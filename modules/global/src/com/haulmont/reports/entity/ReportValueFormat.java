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
package com.haulmont.reports.entity;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.yarg.structure.ReportFieldFormat;

@MetaClass(name = "report$ReportValueFormat")
@SystemLevel
public class ReportValueFormat extends BaseUuidEntity implements ReportFieldFormat {

    private static final long serialVersionUID = 680180375698449946L;

    @MetaProperty
    protected String valueName;

    @MetaProperty
    protected String formatString;

    @MetaProperty
    protected Report report;

    @MetaProperty
    protected Boolean groovyScript = false;

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

    public String getFormatString() {
        return formatString;
    }

    public void setFormatString(String formatString) {
        this.formatString = formatString;
    }

    public Boolean getGroovyScript() {
        return groovyScript;
    }

    public void setGroovyScript(Boolean groovyScript) {
        this.groovyScript = groovyScript;
    }

    @Override
    public String getName() {
        return valueName;
    }

    @Override
    public String getFormat() {
        return formatString;
    }

    @Override
    public Boolean isGroovyScript() {
        return groovyScript;
    }
}
