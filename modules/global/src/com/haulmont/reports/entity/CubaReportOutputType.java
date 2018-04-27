/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.entity;

import java.io.ObjectStreamException;

public class CubaReportOutputType extends com.haulmont.yarg.structure.ReportOutputType {
    public final static CubaReportOutputType chart = new CubaReportOutputType("chart");
    public final static CubaReportOutputType table = new CubaReportOutputType("table");
    public final static CubaReportOutputType pivot = new CubaReportOutputType("pivot");

    static {
       values.put(chart.getId(), chart);
       values.put(table.getId(), table);
       values.put(pivot.getId(), pivot);
    }

    public CubaReportOutputType(String id) {
        super(id);
    }

    private Object readResolve() throws ObjectStreamException {
        return getOutputTypeById(getId());
    }
}
