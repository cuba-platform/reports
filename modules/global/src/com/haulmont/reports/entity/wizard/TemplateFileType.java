/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.entity.wizard;

import com.haulmont.chile.core.datatypes.impl.EnumClass;
import org.apache.commons.lang.ObjectUtils;

public enum TemplateFileType implements EnumClass<Integer> {
    HTML(30),
    DOCX(40),
    XLSX(50),
    CHART(60),
    CSV(70);

    private Integer id;

    private TemplateFileType(Integer id) {
        this.id = id;
    }

    public static TemplateFileType fromId(Integer id) {
        for (TemplateFileType type : TemplateFileType.values()) {
            if (ObjectUtils.equals(type.getId(), id)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public Integer getId() {
        return id;
    }

}
