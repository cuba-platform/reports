/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

public enum CustomTemplateDefinedBy implements EnumClass<Integer> {
    CLASS(100),
    SCRIPT(200),
    URL(300);

    private Integer id;

    @Override
    public Integer getId() {
        return id;
    }

    CustomTemplateDefinedBy(Integer id) {
        this.id = id;
    }

    public static Integer getId(CustomTemplateDefinedBy definedBy) {
        return definedBy != null ? definedBy.getId() : null;
    }

    public static CustomTemplateDefinedBy fromId(Integer id) {
        for (CustomTemplateDefinedBy type : CustomTemplateDefinedBy.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}