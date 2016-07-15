/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;

public enum ReportOutputType implements EnumClass<Integer> {
    XLS(0, CubaReportOutputType.xls),
    DOC(10, CubaReportOutputType.doc),
    PDF(20, CubaReportOutputType.pdf),
    HTML(30, CubaReportOutputType.html),
    DOCX(40, CubaReportOutputType.docx),
    XLSX(50, CubaReportOutputType.xlsx),
    CUSTOM(60, CubaReportOutputType.custom),
    CHART(70, CubaReportOutputType.chart);

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

    @Nullable
    public static ReportOutputType getTypeFromExtension(String extension) {
        for (ReportOutputType outputType : ReportOutputType.values()) {
            if (StringUtils.equals(outputType.toString(), extension)) {
                return outputType;
            }
        }
        return null;
    }
}