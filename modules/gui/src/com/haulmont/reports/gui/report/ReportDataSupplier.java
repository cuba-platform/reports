/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.report;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.gui.data.impl.GenericDataSupplier;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.Report;

import java.util.HashSet;
import java.util.Set;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class ReportDataSupplier extends GenericDataSupplier {
    @Override
    public Set<Entity> commit(CommitContext context) {
        Set<Entity> result = new HashSet<>();
        ReportService reportService = AppBeans.get(ReportService.NAME, ReportService.class);
        for (Entity entity : context.getCommitInstances()) {
            if (entity instanceof Report) {
                result.add(reportService.storeReportEntity((Report) entity));
            }
        }

        return result;
    }
}
