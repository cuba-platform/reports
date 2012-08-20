/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.cuba.report;

import com.haulmont.chile.core.datatypes.impl.EnumClass;
import org.apache.commons.lang.ObjectUtils;

/**
 * @author degtyarjov
 * @version $Id$
 */
public enum ReportOutputType implements EnumClass<Integer> {
    XLS(0),
    DOC(10),
    PDF(20),
    HTML(30);

    private Integer id;

    @Override
    public Integer getId() {
        return id;
    }

    ReportOutputType(Integer id) {
        this.id = id;
    }

    public static ReportOutputType fromId(Integer id) {
        for (ReportOutputType type : ReportOutputType.values()) {
            if (ObjectUtils.equals(type.getId(), id)) {
                return type;
            }
        }
        return null;
    }
}