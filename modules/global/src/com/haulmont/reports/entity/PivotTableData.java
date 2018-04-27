/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.entity;

import com.haulmont.cuba.core.entity.KeyValueEntity;

import java.io.Serializable;
import java.util.List;

public class PivotTableData implements Serializable {
    protected String pivotTableJson;
    protected List<KeyValueEntity> values;

    public PivotTableData(String pivotTableJson, List<KeyValueEntity> values) {
        this.values = values;
        this.pivotTableJson = pivotTableJson;
    }

    public List<KeyValueEntity> getValues() {
        return values;
    }

    public void setValues(List<KeyValueEntity> values) {
        this.values = values;
    }

    public String getPivotTableJson() {
        return pivotTableJson;
    }

    public void setPivotTableJson(String pivotTableJson) {
        this.pivotTableJson = pivotTableJson;
    }
}
