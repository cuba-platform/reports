/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports.gui.report.run;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.AbstractLookup;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.reports.gui.ReportHelper;
import com.haulmont.reports.entity.Report;

import javax.inject.Inject;
import java.util.Map;

/**
 * @author artamonov
 * @version $Id$
 */
public class ReportRun extends AbstractLookup {
    private static final String RUN_ACTION_ID = "runReport";

    @Inject
    private Table reportsTable;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        AbstractAction runAction = new ItemTrackingAction(RUN_ACTION_ID) {
            @Override
            public void actionPerform(Component component) {
                Report report = reportsTable.getSingleSelected();
                if (report != null) {
                    report = getDsContext().getDataSupplier().reload(report, "report.edit");
                    ReportHelper.runReport(report, ReportRun.this);
                }
            }
        };
        reportsTable.addAction(runAction);
        reportsTable.setItemClickAction(runAction);

        UserSessionSource userSessionSource = AppBeans.get(UserSessionSource.class);
        if (params.get("user") == null) params.put("user", userSessionSource.getUserSession().getUser());

        reportsTable.getDatasource().refresh(params);
    }
}
