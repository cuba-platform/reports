/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.converter;

import com.haulmont.reports.entity.Report;

/**
 * @author degtyarjov
 * @version $Id$
 */
//todo support versions
public class GsonConverter {
    public String convertToString(Report report) {
        return new ReportGsonSerializationSupport().convertToString(report);
    }

    public Report convertToReport(String json) {
        return new ReportGsonSerializationSupport().convertToReport(json, Report.class);
    }
}
