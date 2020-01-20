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

package com.haulmont.reports.gui.report.history;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.ScreenBuilders;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.DateField;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportGroup;
import com.haulmont.reports.gui.ReportGuiManager;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@UiController("report$ReportExecution.dialog")
@UiDescriptor("report-execution-dialog.xml")
@LookupComponent("reportsTable")
@LoadDataBeforeShow
public class ReportExecutionDialog extends StandardLookup<Report> {

    public static final String META_CLASS_PARAMETER = "metaClass";
    public static final String SCREEN_PARAMETER = "screen";

    @Inject
    protected ScreenBuilders screenBuilders;
    @Inject
    protected ReportGuiManager reportGuiManager;
    @Inject
    protected UserSessionSource userSessionSource;

    @Inject
    protected CollectionContainer<Report> reportsDc;
    @Inject
    protected Table<Report> reportsTable;

    @Inject
    protected Button applyFilterBtn;
    @Inject
    protected TextField<String> filterName;
    @Inject
    protected TextField<String> filterCode;
    @Inject
    protected LookupField<ReportGroup> filterGroup;
    @Inject
    protected DateField<Date> filterUpdatedDate;

    @WindowParam(name = META_CLASS_PARAMETER)
    protected MetaClass metaClassParameter;
    @WindowParam(name = SCREEN_PARAMETER)
    protected String screenParameter;

    @Install(to = "reportsDl", target = Target.DATA_LOADER)
    protected List<Report> reportsDlLoadDelegate(LoadContext<Report> loadContext) {
        User sessionUser = userSessionSource.getUserSession().getUser();
        return reportGuiManager.getAvailableReports(screenParameter, sessionUser, metaClassParameter);
    }

    @Subscribe("clearFilterBtn")
    public void onClearFilterBtnClick(Button.ClickEvent event) {
        filterName.setValue(null);
        filterCode.setValue(null);
        filterUpdatedDate.setValue(null);
        filterGroup.setValue(null);
        filterReports();
    }

    @Subscribe("openExecutionBrowserBtn")
    public void onOpenExecutionBrowserBtnClick(Button.ClickEvent event) {
        Set<Report> selectedReports = reportsTable.getSelected();
        List<Report> reports = selectedReports.isEmpty()
                ? reportsDc.getItems()
                : new ArrayList<>(selectedReports);

        screenBuilders.screen(ReportExecutionDialog.this)
                .withScreenClass(ReportExecutionBrowser.class)
                .withOptions(new MapScreenOptions(ParamsMap.of(ReportExecutionBrowser.REPORTS_PARAMETER, reports)))
                .show();
    }

    @Subscribe("applyFilterBtn")
    public void onApplyFilterBtnClick(Button.ClickEvent event) {
        filterReports();
    }

    protected void filterReports() {
        User sessionUser = userSessionSource.getUserSession().getUser();
        List<Report> reports = reportGuiManager.getAvailableReports(screenParameter, sessionUser, metaClassParameter)
                .stream()
                .filter(this::filterReport)
                .collect(Collectors.toList());

        reportsDc.setItems(reports);

        Table.SortInfo sortInfo = reportsTable.getSortInfo();
        if (sortInfo != null) {
            Table.SortDirection direction = sortInfo.getAscending() ? Table.SortDirection.ASCENDING : Table.SortDirection.DESCENDING;
            reportsTable.sort(sortInfo.getPropertyId().toString(), direction);
        }
    }

    protected boolean filterReport(Report report) {
        String filterNameValue = StringUtils.lowerCase(filterName.getValue());
        String filterCodeValue = StringUtils.lowerCase(filterCode.getValue());
        ReportGroup groupFilterValue = filterGroup.getValue();
        Date dateFilterValue = filterUpdatedDate.getValue();

        if (filterNameValue != null
                && !report.getName().toLowerCase().contains(filterNameValue)) {
            return false;
        }

        if (filterCodeValue != null) {
            if (report.getCode() == null
                    || (report.getCode() != null
                    && !report.getCode().toLowerCase().contains(filterCodeValue))) {
                return false;
            }
        }

        if (groupFilterValue != null && !Objects.equals(report.getGroup(), groupFilterValue)) {
            return false;
        }

        if (dateFilterValue != null
                && report.getUpdateTs() != null
                && !report.getUpdateTs().after(dateFilterValue)) {
            return false;
        }

        return true;
    }
}
