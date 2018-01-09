/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

public enum PredefinedTransformation implements EnumClass<Integer> {
    STARTS_WITH(0), CONTAINS(1), ENDS_WITH(2);

    private Integer id;

    PredefinedTransformation(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public static PredefinedTransformation fromId(Integer id) {
        for (PredefinedTransformation type : PredefinedTransformation.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}
