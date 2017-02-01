/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports;


import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.security.app.Authenticated;
import com.haulmont.reports.entity.Report;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component(ReportingMigrator.NAME)
public class ReportingMigrator implements ReportingMigratorMBean {

    public static final String NAME = "reporting_ReportingMigrator";

    @Inject
    protected DataManager dataManager;
    @Inject
    protected ReportingApi reportingApi;

    @Authenticated
    public String updateSecurityIndex() {
        LoadContext<Report> ctx = new LoadContext<>(Report.class);
        ctx.setLoadDynamicAttributes(true);
        ctx.setView("report.edit");
        ctx.setQueryString("select r from report$Report r");
        List<Report> resultList = dataManager.loadList(ctx);
        for (Report report : resultList) {
            reportingApi.storeReportEntity(report);
        }
        return "Index migrated successfully";
    }
}
