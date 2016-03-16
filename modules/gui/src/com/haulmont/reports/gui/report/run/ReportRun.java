/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.report.run;

import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.defaults.DefaultString;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportGroup;
import com.haulmont.reports.gui.ReportGuiManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.*;

/**
 */
public class ReportRun extends AbstractLookup {

    protected static final String RUN_ACTION_ID = "runReport";
    public static final String REPORTS_PARAMETER = "reports";
    public static final String SCREEN_PARAMETER = "screen";

    @Inject
    protected Table reportsTable;

    @Inject
    protected ReportGuiManager reportGuiManager;

    @Inject
    protected CollectionDatasource<Report, UUID> reportDs;

    @Inject
    protected UserSessionSource userSessionSource;

    @Inject
    protected TextField nameFilter;

    @Inject
    protected TextField codeFilter;

    @Inject
    protected LookupField groupFilter;

    @Inject
    protected DateField updatedDateFilter;

    @Inject
    protected GridLayout gridFilter;

    @Inject
    protected ClientConfig clientConfig;

    @WindowParam(name = REPORTS_PARAMETER)
    protected List<Report> reportsParameter;

    @WindowParam(name = SCREEN_PARAMETER)
    protected String screenParameter;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        List<Report> reports = reportsParameter;
        if (reports == null) {
            reports = reportGuiManager.getAvailableReports(screenParameter, userSessionSource.getUserSession().getUser(), null);
        }

        if (reportsParameter != null) {
            gridFilter.setVisible(false);
        }

        for (Report report : reports) {
            reportDs.includeItem(report);
        }

        Action runAction = new ItemTrackingAction(RUN_ACTION_ID) {
            @Override
            public void actionPerform(Component component) {
                Report report = (Report) target.getSingleSelected();
                if (report != null) {
                    report = getDsContext().getDataSupplier().reload(report, ReportService.MAIN_VIEW_NAME);
                    reportGuiManager.runReport(report, ReportRun.this);
                }
            }
        };
        reportsTable.addAction(runAction);
        reportsTable.setItemClickAction(runAction);

        // Dialog mode queryParameters
        getDialogParams().setWidth(640).setHeight(480);

        addAction(new AbstractAction("applyFilter", clientConfig.getFilterApplyShortcut()) {
            @Override
            public void actionPerform(Component component) {
                filterReports();
            }
        });
    }

    public void filterReports() {
        String nameFilterValue = StringUtils.lowerCase(nameFilter.getValue());
        String codeFilterValue = StringUtils.lowerCase(codeFilter.getValue());
        ReportGroup groupFilterValue = groupFilter.getValue();
        Date dateFilterValue = updatedDateFilter.getValue();

        List<Report> reports = new ArrayList<>(
                reportGuiManager.getAvailableReports(screenParameter, userSessionSource.getUserSession().getUser(), null)
        );

        CollectionUtils.filter(reports, new org.apache.commons.collections.Predicate() {
            @Override
            public boolean evaluate(Object object) {
                Report report = (Report) object;

                if (nameFilterValue != null
                        && !report.getName().toLowerCase().contains(nameFilterValue)) {
                    return false;
                }

                if (codeFilterValue != null) {
                    if (report.getCode() == null
                            || (report.getCode() != null
                            && !report.getCode().toLowerCase().contains(codeFilterValue))) {
                        return false;
                    }
                }

                if (groupFilterValue != null && !ObjectUtils.equals(report.getGroup(), groupFilterValue)) {
                    return false;
                }

                if (dateFilterValue != null
                        && report.getUpdateTs() != null
                        && !report.getUpdateTs().after(dateFilterValue)) {
                    return false;
                }

                return true;
            }
        });

        reportDs.clear();
        for (Report report : reports) {
            reportDs.includeItem(report);
        }
    }

    public void clearFilter() {
        nameFilter.setValue(null);
        codeFilter.setValue(null);
        updatedDateFilter.setValue(null);
        groupFilter.setValue(null);
    }
}