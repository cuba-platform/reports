/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.entity.pivottable;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

public enum PivotTablePropertyType implements EnumClass<String> {
    DETACHED("detached"),
    AGGREGATIONS("aggregations"),
    COLUMNS("columns"),
    ROWS("rows");

    String id;

    PivotTablePropertyType(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public static PivotTablePropertyType fromId(String id) {
        for (PivotTablePropertyType type : values()) {
            if (type.getId().equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}
