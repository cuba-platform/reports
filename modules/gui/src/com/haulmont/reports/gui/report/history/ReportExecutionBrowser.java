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

import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.icons.CubaIcon;
import com.haulmont.cuba.gui.model.CollectionLoader;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportExecution;

import javax.inject.Inject;
import java.util.function.Function;

@UiController("report$ReportExecution.browse")
@UiDescriptor("report-execution-browse.xml")
public class ReportExecutionBrowser extends StandardLookup {

    @Inject
    protected CollectionLoader<ReportExecution> executionsDl;
    @Inject
    protected Table<ReportExecution> executionsTable;
    @Inject
    protected MessageBundle messageBundle;
    @Inject
    protected ExportDisplay exportDisplay;

    protected Function<Long, String> durationFormatter = new SecondsToTextFormatter();
    protected Report filterByReport;

    public ReportExecutionBrowser setFilterByReport(Report filterByReport) {
        this.filterByReport = filterByReport;
        return this;
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        executionsTable.addAction(new DownloadDocumentAction());
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        initDataLoader();

        if (filterByReport != null) {
            String caption = messageBundle.formatMessage("report.executionHistory.byReport", filterByReport.getName());
            getWindow().setCaption(caption);
        }
    }

    protected void initDataLoader() {
        String queryString = "select e from report$ReportExecution e"
                + (filterByReport != null ? " where e.report.id = :reportId" : "")
                + " order by e.startTime desc";
        executionsDl.setQuery(queryString);

        if (filterByReport != null) {
            executionsDl.setParameter("reportId", filterByReport.getId());
        }
        executionsDl.load();
    }

    @Install(to = "executionsTable.executionTimeSec", subject = "formatter")
    protected String formatExecutionTimeSec(Long value) {
        String text = durationFormatter.apply(value);
        return text;
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
