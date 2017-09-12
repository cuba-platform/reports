package com.haulmont.reports.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;
import org.apache.commons.lang.ObjectUtils;

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
            if (ObjectUtils.equals(type.getId(), id)) {
                return type;
            }
        }
        return null;
    }
}
