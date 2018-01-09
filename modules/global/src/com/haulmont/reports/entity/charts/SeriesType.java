/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.entity.charts;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

public enum SeriesType implements EnumClass<String> {
    LINE("line"), COLUMN("column"), STEP("step"), SMOOTHED_LINE("smoothedLine");

    private String id;

    @Override
    public String getId() {
        return id;
    }

    SeriesType(String id) {
        this.id = id;
    }

    public static SeriesType fromId(String id) {
        for (SeriesType type : SeriesType.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}