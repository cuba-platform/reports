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
public enum ReportOutputType implements EnumClass<Integer> {
    XLS(0, com.haulmont.yarg.structure.ReportOutputType.xls),
    DOC(10, com.haulmont.yarg.structure.ReportOutputType.doc),
    PDF(20, com.haulmont.yarg.structure.ReportOutputType.pdf),
    HTML(30, com.haulmont.yarg.structure.ReportOutputType.html),
    DOCX(40, com.haulmont.yarg.structure.ReportOutputType.docx),
    XLSX(50, com.haulmont.yarg.structure.ReportOutputType.xlsx);

    private Integer id;

    private com.haulmont.yarg.structure.ReportOutputType outputType;

    @Override
    public Integer getId() {
        return id;
    }

    public com.haulmont.yarg.structure.ReportOutputType getOutputType() {
        return outputType;
    }

    private ReportOutputType(Integer id, com.haulmont.yarg.structure.ReportOutputType outputType) {
        this.id = id;
        this.outputType = outputType;
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