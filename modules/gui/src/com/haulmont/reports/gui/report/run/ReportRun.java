/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports.gui.report.run;

import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.AbstractLookup;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.gui.ReportGuiManager;
import org.apache.commons.collections.CollectionUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author artamonov
 * @version $Id$
 */
public class ReportRun extends AbstractLookup {
    private static final String RUN_ACTION_ID = "runReport";

    @Inject
    protected Table reportsTable;

    @Inject
    protected ReportGuiManager reportGuiManager;

    @Inject
    protected CollectionDatasource<Report, UUID> reportDs;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        List<Report> reports = (List<Report>) params.get("reports");

        if (CollectionUtils.isNotEmpty(reports)) {
            for (Report report : reports) {
                reportDs.includeItem(report);
            }
        }

        AbstractAction runAction = new ItemTrackingAction(RUN_ACTION_ID) {
            @Override
            public void actionPerform(Component component) {
                Report report = reportsTable.getSingleSelected();
                if (report != null) {
                    report = getDsContext().getDataSupplier().reload(report, ReportService.MAIN_VIEW_NAME);
                    reportGuiManager.runReport(report, ReportRun.this);
                }
            }
        };
        reportsTable.addAction(runAction);
        reportsTable.setItemClickAction(runAction);
    }
}
