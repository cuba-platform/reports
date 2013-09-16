/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.entity;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.yarg.structure.ReportFieldFormat;

/**
 * @author artamonov
 * @version $Id$
 */
@MetaClass(name = "report$ReportValueFormat")
@SystemLevel
public class ReportValueFormat extends AbstractNotPersistentEntity implements ReportFieldFormat {
    private static final long serialVersionUID = 680180375698449946L;

    @MetaProperty
    protected String valueName;

    @MetaProperty
    protected String formatString;

    @MetaProperty
    protected Report report;

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

    public String getFormatString() {
        return formatString;
    }

    public void setFormatString(String formatString) {
        this.formatString = formatString;
    }

    @Override
    public String getName() {
        return valueName;
    }

    @Override
    public String getFormat() {
        return formatString;
    }
}
