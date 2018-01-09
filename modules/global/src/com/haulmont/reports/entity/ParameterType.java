/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

public enum ParameterType implements EnumClass<Integer> {
    DATE(10),
    TEXT(20),
    ENTITY(30),
    BOOLEAN(40),
    NUMERIC(50),
    ENTITY_LIST(60),
    ENUMERATION(70),
    DATETIME(80),
    TIME(90);

    private Integer id;

    @Override
    public Integer getId() {
        return id;
    }

    ParameterType(Integer id) {
        this.id = id;
    }

    public static ParameterType fromId(Integer id) {
        for (ParameterType type : ParameterType.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}