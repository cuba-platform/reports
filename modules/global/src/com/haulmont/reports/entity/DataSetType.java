/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;
import org.apache.commons.lang.ObjectUtils;

/**
 * @author degtyarjov
 * @version $Id$
 */
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
    MULTI(50, "multi");

    private Integer id;

    private String code;

    @Override
    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    private DataSetType(Integer id, String code) {
        this.id = id;
        this.code = code;
    }

    public static DataSetType fromId(Integer id) {
        for (DataSetType type : DataSetType.values()) {
            if (ObjectUtils.equals(type.getId(), id)) {
                return type;
            }
        }
        return null;
    }
}