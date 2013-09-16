/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;
import org.apache.commons.lang.ObjectUtils;

/**
 * @author degtyarjov
 * @version $Id$
 */
public enum ReportType implements EnumClass<Integer> {
    SIMPLE(10),
    PRINT_FORM(20),
    LIST_PRINT_FORM(30);

    private Integer id;

    @Override
    public Integer getId() {
        return id;
    }

    ReportType(Integer id) {
        this.id = id;
    }

    public static ReportType fromId(Integer id) {
        for (ReportType type : ReportType.values()) {
            if (ObjectUtils.equals(type.getId(), id)) {
                return type;
            }
        }
        return null;
    }
}