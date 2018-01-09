package com.haulmont.reports.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

public enum JsonSourceType implements EnumClass<Integer> {
    GROOVY_SCRIPT(10),
    URL(20),
    PARAMETER(30);

    private Integer id;

    JsonSourceType(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public static JsonSourceType fromId(Integer id) {
        for (JsonSourceType type : JsonSourceType.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}
