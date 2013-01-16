/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.gui.report.run;

import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.gui.ReportHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class RunReportDatasource extends CollectionDatasourceImpl<Report, UUID> {

    @Override
    protected void loadData(Map<String, Object> params) {
        User user = (User) params.get("user");
        String screen = (String) params.get("screen");
        super.loadData(params);
        if (user != null || screen != null) {
            applySecurityPolicies(user, screen);
        }
    }

    private void applySecurityPolicies(User user, String screen) {
        final List<Report> reports = new ArrayList<Report>(data.values());
        data.clear();
        List<Report> filter = ReportHelper.applySecurityPolicies(user, screen, reports);
        for (Report report : filter) {
            data.put(report.getId(), report);
            attachListener(report);
        }
    }
}