/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
