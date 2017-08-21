/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.entity.tables.dto;

import com.haulmont.bali.datastruct.Pair;
import com.haulmont.cuba.core.entity.KeyValueEntity;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CubaTableDTO implements Serializable{

    /**
     * Data represents band/group/table name and list of rows as key(column)-value maps.
     */
    protected Map<String, List<KeyValueEntity>> data = Collections.emptyMap();

    /**
     * Headers contain band/group/table name and set of pairs 'column name - column type as Class'.
     */
    protected Map<String, Set<Pair<String, Class>>> headers = Collections.emptyMap();

    public CubaTableDTO(Map<String, List<KeyValueEntity>> data, Map<String, Set<Pair<String, Class>>> headers) {
        this.data = data;
        this.headers = headers;
    }

    public Map<String, List<KeyValueEntity>> getData() {
        return data;
    }

    public void setData(Map<String, List<KeyValueEntity>> data) {
        this.data = data;
    }

    public Map<String, Set<Pair<String, Class>>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Set<Pair<String, Class>>> headers) {
        this.headers = headers;
    }
}
