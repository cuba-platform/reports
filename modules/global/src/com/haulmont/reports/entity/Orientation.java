/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;
import org.apache.commons.lang.ObjectUtils;

public enum Orientation implements EnumClass<Integer> {
    HORIZONTAL(0),
    VERTICAL(1);

    private Integer id;

    Orientation(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public static Orientation fromId(Integer id) {
        for (Orientation type : Orientation.values()) {
            if (ObjectUtils.equals(type.getId(), id)) {
                return type;
            }
        }
        return null;
    }
}
