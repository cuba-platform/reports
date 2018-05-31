/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.report;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.EntitySet;
import com.haulmont.cuba.gui.data.impl.GenericDataSupplier;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportTemplate;

import java.util.HashSet;
import java.util.Set;

public class ReportDataSupplier extends GenericDataSupplier {
    @Override
    public EntitySet commit(CommitContext context) {
        Set<Entity> result = new HashSet<>();
        ReportService reportService = AppBeans.get(ReportService.NAME, ReportService.class);
        Report reportToStore = null;
        for (Entity entity : context.getCommitInstances()) {
            if (entity instanceof Report) {
                reportToStore = (Report) entity;
            } else if (entity instanceof ReportTemplate) {
                reportToStore = ((ReportTemplate) entity).getReport();
            }
        }

        if (reportToStore != null) {
            result.add(reportService.storeReportEntity(reportToStore));
        }

        return EntitySet.of(result);
    }
}
