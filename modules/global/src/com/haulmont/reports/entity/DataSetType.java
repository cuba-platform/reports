/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import java.util.Objects;

public enum DataSetType implements EnumClass<Integer> {

    /**
     * SQL query
     */
    SQL(10, "sql"),

    /**
     * JPQL query
     */
    JPQL(20, "jpql"),

    /**
     * Groovy script
     */
    GROOVY(30, "groovy"),

    /**
     * Entity
     */
    SINGLE(40, "single"),

    /**
     * Entities list
     */
    MULTI(50, "multi"),

    /**
     * json
     */
    JSON(60, "json");

    private Integer id;

    private String code;

    @Override
    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    DataSetType(Integer id, String code) {
        this.id = id;
        this.code = code;
    }

    public static DataSetType fromId(Integer id) {
        for (DataSetType type : DataSetType.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }

    public static DataSetType fromCode(String code) {
        for (DataSetType type : DataSetType.values()) {
            if (Objects.equals(type.getCode(), code)) {
                return type;
            }
        }
        return null;
    }
}