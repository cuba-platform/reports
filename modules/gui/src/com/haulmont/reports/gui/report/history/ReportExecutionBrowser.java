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

import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.icons.CubaIcon;
import com.haulmont.cuba.gui.model.CollectionLoader;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportExecution;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@UiController("report$ReportExecution.browse")
@UiDescriptor("report-execution-browse.xml")
public class ReportExecutionBrowser extends StandardLookup {

    public static final String REPORTS_PARAMETER = "reports";

    @Inject
    protected CollectionLoader<ReportExecution> executionsDl;
    @Inject
    protected Table<ReportExecution> executionsTable;
    @Inject
    protected MessageBundle messageBundle;
    @Inject
    protected ExportDisplay exportDisplay;

    @WindowParam(name = REPORTS_PARAMETER)
    protected List<Report> filterByReports;

    protected Function<Long, String> durationFormatter = new SecondsToTextFormatter();

    @Subscribe
    protected void onInit(InitEvent event) {
        executionsTable.addAction(new DownloadDocumentAction());
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        initDataLoader();

        if (filterByReports != null && !filterByReports.isEmpty()) {
            String caption = messageBundle.formatMessage("report.executionHistory.byReport", getReportsNames());
            getWindow().setCaption(caption);
        }
    }

    protected void initDataLoader() {
        StringBuilder query = new StringBuilder("select e from report$ReportExecution e");

        if (!CollectionUtils.isEmpty(filterByReports)) {
            query.append(" where e.report.id in :reportIds");
            executionsDl.setParameter("reportIds", filterByReports);
        }
        query.append(" order by e.startTime desc");

        executionsDl.setQuery(query.toString());
        executionsDl.load();
    }

    @Install(to = "executionsTable.executionTimeSec", subject = "formatter")
    protected String formatExecutionTimeSec(Long value) {
        String text = durationFormatter.apply(value);
        return text;
    }

    protected String getReportsNames() {
        if (CollectionUtils.isEmpty(filterByReports)) {
            return "";
        }

        return filterByReports.stream()
                .map(Report::getName)
                .collect(Collectors.joining(", "));
    }

    public class DownloadDocumentAction extends BaseAction {
        public DownloadDocumentAction() {
            super("download");
        }

        @Override
        protected boolean isApplicable() {
            if (executionsTable.getSelected().size() != 1) {
                return false;
            }
            ReportExecution execution = executionsTable.getSingleSelected();
            return execution != null && execution.getOutputDocument() != null;
        }

        @Override
        public String getCaption() {
            return messageBundle.getMessage("report.executionHistory.download");
        }

        @Override
        public String getIcon() {
            return CubaIcon.DOWNLOAD.source();
        }

        @Override
        public void actionPerform(Component component) {
            ReportExecution execution = executionsTable.getSingleSelected();
            if (execution != null && execution.getOutputDocument() != null) {
                exportDisplay.show(execution.getOutputDocument(), null);
            }
        }
    }
}
