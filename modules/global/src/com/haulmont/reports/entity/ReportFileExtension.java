/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;
import org.apache.commons.lang.ObjectUtils;

/**
 * @author artamonov
 * @version $Id$
 */
public enum ReportFileExtension implements EnumClass<String> {
    XLT("xlt"),
    XLS("xls"),
    DOC("doc"),
    ODT("odt"),
    HTML("html"),
    HTM("htm");

    private String id;

    ReportFileExtension(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public static ReportFileExtension fromId(String id) {
        for (ReportFileExtension type : ReportFileExtension.values()) {
            if (ObjectUtils.equals(type.getId(), id)) {
                return type;
            }
        }
        return null;
    }
}