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

import com.haulmont.bali.datastruct.Pair;
import com.haulmont.cuba.core.entity.KeyValueEntity;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CubaTableData implements Serializable{

    /**
     * Data represents band/group/table name and list of rows as key(column)-value maps.
     */
    protected Map<String, List<KeyValueEntity>> data;

    /**
     * Headers contain band/group/table name and set of pairs 'column name - column type as Class'.
     */
    protected Map<String, Set<Pair<String, Class>>> headers;

    public CubaTableData(Map<String, List<KeyValueEntity>> data, Map<String, Set<Pair<String, Class>>> headers) {
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
