/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.entity.wizard;

import com.haulmont.chile.core.datatypes.impl.EnumClass;
import org.apache.commons.lang.ObjectUtils;

/**
 * @author fedorchenko
 * @version $Id$
 */
public enum TemplateFileType implements EnumClass<Integer>{
    HTML(30),
    DOCX(40),
    XLSX(50);

    private Integer id;

    @Override
    public Integer getId() {
        return id;
    }

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

}
