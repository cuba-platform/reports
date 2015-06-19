/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.entity;

import java.io.ObjectStreamException;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class CubaReportOutputType extends com.haulmont.yarg.structure.ReportOutputType {
    public final static CubaReportOutputType chart = new CubaReportOutputType("chart");

    static {
       values.put(chart.getId(), chart);
    }

    public CubaReportOutputType(String id) {
        super(id);
    }

    private Object readResolve() throws ObjectStreamException {
        return getOutputTypeById(getId());
    }
}
