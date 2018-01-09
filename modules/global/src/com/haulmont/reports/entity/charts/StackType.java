/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.entity.charts;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

public enum StackType implements EnumClass<String> {
    NONE("none"), REGULAR("regular"), HUNDRED_PERCENTS("100%"), THREE_D("3d");

    private String id;

    @Override
    public String getId() {
        return id;
    }

    StackType(String id) {
        this.id = id;
    }

    public static StackType fromId(String id) {
        for (StackType type : StackType.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}