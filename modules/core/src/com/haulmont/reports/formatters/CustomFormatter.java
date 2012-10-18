/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.formatters;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.reports.app.CustomReport;
import com.haulmont.reports.entity.Band;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportTemplate;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class CustomFormatter implements Formatter {
    private Report report;
    private ReportTemplate template;
    private Map<String, Object> params;

    public CustomFormatter(Report report, ReportTemplate template, Map<String, Object> params) {
        this.report = report;
        this.params = params;
        this.template = template;
    }

    @Override
    public byte[] createDocument(@Nullable Band rootBand) {
        Class clazz = AppBeans.get(Scripting.class).loadClass(template.getCustomClass());
        try {
            CustomReport customReport = (CustomReport) clazz.newInstance();
            return customReport.createReport(report, params);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}