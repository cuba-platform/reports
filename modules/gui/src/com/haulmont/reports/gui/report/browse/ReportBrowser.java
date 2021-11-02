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
package com.haulmont.reports.gui.report.browse;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.app.PersistenceManagerService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.ClientType;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ScreenBuilders;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.screen.MapScreenOptions;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.gui.ReportGuiManager;
import com.haulmont.reports.gui.report.edit.ReportEditor;
import com.haulmont.reports.gui.report.history.ReportExecutionBrowser;
import com.haulmont.reports.gui.report.wizard.ReportWizardCreator;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ReportBrowser extends AbstractLookup {

    @Inject
    protected ReportGuiManager reportGuiManager;
    @Inject
    protected ReportService reportService;
    @Inject
    protected Button runReport;
    @Named("import")
    protected Button importReport;
    @Named("export")
    protected Button exportReport;
    @Named("copy")
    protected Button copyReport;
    @Named("table")
    protected GroupTable<Report> reportsTable;
    @Named("table.edit")
    protected EditAction tableEdit;
    @Inject
    protected Security security;
    @Inject
    protected Metadata metadata;
    @Inject
    protected ScreenBuilders screenBuilders;
    @Inject
    protected PopupButton popupCreateBtn;
    @Inject
    protected Button createBtn;
    @Inject
    protected CollectionDatasource<Report, UUID> reportDs;
    @Inject
    protected PersistenceManagerService persistenceManagerService;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        boolean hasPermissionsToCreateReports = security.isEntityOpPermitted(
                metadata.getClassNN(Report.class), EntityOp.CREATE);

        Action copyAction = new ItemTrackingAction("copy")
                .withCaption(getMessage("copy"))
                .withHandler(event -> {
                    Report report = reportsTable.getSingleSelected();
                    if (report != null) {
                        reportService.copyReport(report);
                        reportsTable.getDatasource().refresh();
                    } else {
                        showNotification(getMessage("notification.selectReport"), NotificationType.HUMANIZED);
                    }
                });
        copyAction.setEnabled(hasPermissionsToCreateReports);
        copyReport.setAction(copyAction);

        runReport.setAction(new ItemTrackingAction("runReport")
                .withCaption(getMessage("runReport"))
                .withHandler(event -> {
                    Report report = reportsTable.getSingleSelected();
                    if (report != null) {
                        report = getDsContext().getDataSupplier().reload(report, "report.edit");
                        if (report.getInputParameters() != null && report.getInputParameters().size() > 0 ||
                                reportGuiManager.inputParametersRequiredByTemplates(report)) {
                            Window paramsWindow = openWindow("report$inputParameters", OpenType.DIALOG,
                                    ParamsMap.of("report", report));
                            paramsWindow.addCloseListener(actionId ->
                                    reportsTable.focus()
                            );
                        } else {
                            reportGuiManager.printReport(report, Collections.emptyMap(), ReportBrowser.this);
                        }
                    }
                }));

        BaseAction importAction = new BaseAction("import")
                .withHandler(event -> {
                    openWindow("report$Report.importDialog", OpenType.DIALOG)
                            .addCloseListener(actionId -> {
                                if (COMMIT_ACTION_ID.equals(actionId)) {
                                    reportsTable.getDatasource().refresh();
                                }
                                reportsTable.requestFocus();
                            });
                });

        importAction.setEnabled(hasPermissionsToCreateReports);
        importReport.setAction(importAction);

        Action exportAction = new ItemTrackingAction("export")
                .withHandler(event -> {
                    Set<Report> reports = reportsTable.getSelected();
                    if ((reports != null) && (!reports.isEmpty())) {
                        ExportDisplay exportDisplay = AppConfig.createExportDisplay(ReportBrowser.this);
                        ByteArrayDataProvider provider = new ByteArrayDataProvider(reportService.exportReports(reports));
                        if (reports.size() > 1) {
                            exportDisplay.show(provider, "Reports", ExportFormat.ZIP);
                        } else if (reports.size() == 1) {
                            exportDisplay.show(provider, reports.iterator().next().getName(), ExportFormat.ZIP);
                        }
                    }
                });

        exportReport.setAction(exportAction);

        reportsTable.addAction(copyReport.getAction());
        reportsTable.addAction(exportReport.getAction());
        reportsTable.addAction(runReport.getAction());
        reportsTable.addAction(new ShowExecutionsAction());

        CreateAction createReportAction = new CreateAction(reportsTable) {
            @Override
            protected void afterCommit(Entity entity) {
                reportsTable.expandPath(entity);
            }
        };

        reportsTable.addAction(createReportAction);
        subscribeCreateActionCloseHandler(createReportAction);

        if (AppConfig.getClientType() == ClientType.WEB) {
            reportsTable.getButtonsPanel().remove(createBtn);

            CreateAction popupCreateReportAction = new CreateAction(reportsTable) {
                @Override
                public String getCaption() {
                    return getMessage("report.new");
                }

                @Override
                protected void afterCommit(Entity entity) {
                    reportsTable.expandPath(entity);
                }
            };
            popupCreateBtn.addAction(popupCreateReportAction);
            subscribeCreateActionCloseHandler(popupCreateReportAction);

            popupCreateBtn.addAction(new AbstractAction("wizard") {
                @Override
                public void actionPerform(Component component) {
                    ReportWizardCreator wizard = (ReportWizardCreator) openWindow("report$Report.wizard",
                            OpenType.DIALOG);
                    wizard.addCloseListener(actionId -> {
                        if (COMMIT_ACTION_ID.equals(actionId)) {
                            if (wizard.getItem() != null && wizard.getItem().getGeneratedReport() != null) {
                                Report item = wizard.getItem().getGeneratedReport();
                                reportDs.includeItem(item);
                                reportsTable.setSelected(item);
                                ReportEditor reportEditor = (ReportEditor) openEditor("report$Report.edit",
                                        reportDs.getItem(), OpenType.THIS_TAB);

                                reportEditor.addCloseListener(reportEditorActionId -> {
                                    if (COMMIT_ACTION_ID.equals(reportEditorActionId)) {
                                        Report item1 = reportEditor.getItem();
                                        if (item1 != null) {
                                            reportDs.updateItem(item1);
                                        }
                                    }
                                    UUID newReportId = reportEditor.getItem().getId();
                                    reportsTable.expandPath(reportDs.getItem(newReportId));
                                    reportsTable.requestFocus();
                                });
                            }
                        }
                    });
                }

                @Override
                public String getCaption() {
                    return getMessage("report.wizard");
                }
            });

            popupCreateBtn.setEnabled(hasPermissionsToCreateReports);
        } else {
            reportsTable.getButtonsPanel().remove(popupCreateBtn);
        }

        tableEdit.setAfterWindowClosedHandler((window, closeActionId) -> {
            if (!COMMIT_ACTION_ID.equals(closeActionId)) {
                Report editedReport = (Report) ((Editor) window).getItem();
                Report currentItem = reportDs.getItem(editedReport.getId());

                if (currentItem != null && !editedReport.getVersion().equals(currentItem.getVersion())) {
                    DataSupplier dataSupplier = getDsContext().getDataSupplier();
                    Report reloadedReport = dataSupplier.reload(currentItem, reportDs.getView());
                    reportDs.updateItem(reloadedReport);
                }
            }
        });

        initReportsTableSorting();
    }

    protected void initReportsTableSorting() {
        //sorting by group causes errors in Oracle database
        if ("oracle".equals(persistenceManagerService.getDbmsType())) {
            reportsTable.getColumn("group").setSortable(false);
        }
    }

    protected void subscribeCreateActionCloseHandler(CreateAction createAction) {
        createAction.setAfterWindowClosedHandler(((window, closeActionId) -> {
            if (!COMMIT_ACTION_ID.equals(closeActionId)) {
                Report newReport = (Report) ((Editor) window).getItem();

                if (!PersistenceHelper.isNew(newReport)) {
                    DataSupplier dataSupplier = getDsContext().getDataSupplier();
                    Report reloadedReport = dataSupplier.reload(newReport, reportDs.getView());

                    boolean modified = reportDs.isModified();
                    reportDs.addItem(reloadedReport);
                    ((DatasourceImplementation) reportDs).setModified(modified);
                }
            }
        }));
    }

    public class ShowExecutionsAction extends BaseAction {

        public ShowExecutionsAction() {
            super("executions");
        }

        @Override
        public String getCaption() {
            return getMessage("report.browser.showExecutions");
        }

        @Override
        public void actionPerform(Component component) {
            Set<Report> selectedReports = reportsTable.getSelected();
            screenBuilders.screen(ReportBrowser.this)
                    .withScreenClass(ReportExecutionBrowser.class)
                    .withOptions(new MapScreenOptions(
                            ParamsMap.of(ReportExecutionBrowser.REPORTS_PARAMETER, new ArrayList<>(selectedReports))
                    ))
                    .show();
        }
    }
}