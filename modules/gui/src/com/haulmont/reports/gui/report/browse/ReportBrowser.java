/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports.gui.report.browse;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.ClientType;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.app.core.file.FileUploadDialog;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.gui.ReportGuiManager;
import com.haulmont.reports.gui.report.edit.ReportEditor;
import com.haulmont.reports.gui.report.wizard.ReportWizardCreator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;
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
    @Inject
    protected Security security;
    @Inject
    protected Metadata metadata;
    @Inject
    protected PopupButton popupCreateBtn;
    @Inject
    protected Button createBtn;
    @Inject
    protected CollectionDatasource<Report, UUID> reportDs;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        boolean hasPermissionsToCreateReports = security.isEntityOpPermitted(
                metadata.getClassNN(Report.class), EntityOp.CREATE);

        ItemTrackingAction copyAction = new ItemTrackingAction("copy") {
            @Override
            public void actionPerform(Component component) {
                Report report = (Report) target.getSingleSelected();
                if (report != null) {
                    reportService.copyReport(report);
                    target.refresh();
                } else {
                    showNotification(getMessage("notification.selectReport"), NotificationType.HUMANIZED);
                }
            }
        };
        copyAction.setEnabled(hasPermissionsToCreateReports);
        copyReport.setAction(copyAction);

        runReport.setAction(new ItemTrackingAction("runReport") {
            @Override
            public void actionPerform(Component component) {
                Report report = (Report) target.getSingleSelected();
                if (report != null) {
                    report = getDsContext().getDataSupplier().reload(report, "report.edit");
                    if (report.getInputParameters() != null && report.getInputParameters().size() > 0) {
                        Window paramsWindow = openWindow("report$inputParameters", OpenType.DIALOG,
                                ParamsMap.of("report", report));
                        paramsWindow.addCloseListener(actionId ->
                                target.requestFocus()
                        );
                    } else {
                        reportGuiManager.printReport(report, Collections.emptyMap(), ReportBrowser.this);
                    }
                }
            }
        });

        BaseAction importAction = new BaseAction("import") {
            @Override
            public void actionPerform(Component component) {
                openWindow("report$Report.importDialog", OpenType.DIALOG)
                        .addCloseListener(actionId -> {
                            if (COMMIT_ACTION_ID.equals(actionId)) {
                                reportsTable.getDatasource().refresh();
                            }
                            reportsTable.requestFocus();
                        });
            }
        };
        importAction.setEnabled(hasPermissionsToCreateReports);
        importAction.setCaption(StringUtils.EMPTY);
        importReport.setAction(importAction);

        ItemTrackingAction exportAction = new ItemTrackingAction("export") {
            @Override
            public void actionPerform(Component component) {
                Set<Report> reports = target.getSelected();
                if ((reports != null) && (!reports.isEmpty())) {
                    ExportDisplay exportDisplay = AppConfig.createExportDisplay(ReportBrowser.this);
                    ByteArrayDataProvider provider = new ByteArrayDataProvider(reportService.exportReports(reports));
                    if (reports.size() > 1) {
                        exportDisplay.show(provider, "Reports", ExportFormat.ZIP);
                    } else if (reports.size() == 1) {
                        exportDisplay.show(provider, reports.iterator().next().getName(), ExportFormat.ZIP);
                    }
                }
            }
        };
        exportAction.setCaption(StringUtils.EMPTY);
        exportReport.setAction(exportAction);
        reportsTable.addAction(copyReport.getAction());
        reportsTable.addAction(exportReport.getAction());
        reportsTable.addAction(runReport.getAction());

        reportsTable.addAction(new CreateAction(reportsTable) {
            @Override
            protected void afterCommit(Entity entity) {
                reportsTable.expandPath(entity);
            }
        });

        if (AppConfig.getClientType() == ClientType.WEB) {
            reportsTable.getButtonsPanel().remove(createBtn);
            popupCreateBtn.addAction(new CreateAction(reportsTable) {
                @Override
                public String getCaption() {
                    return getMessage("report.new");
                }

                @Override
                protected void afterCommit(Entity entity) {
                    reportsTable.expandPath(entity);
                }
            });

            popupCreateBtn.addAction(new AbstractAction("wizard") {
                @Override
                public void actionPerform(Component component) {
                    ReportWizardCreator wizard = (ReportWizardCreator) openWindow("report$Report.wizard",
                            OpenType.DIALOG);
                    wizard.addCloseListener(actionId -> {
                        if (COMMIT_ACTION_ID.equals(actionId)) {
                            if (wizard.getItem() != null && wizard.getItem().getGeneratedReport() != null) {
                                Report item = wizard.getItem().getGeneratedReport();
                                CollectionDatasource datasource = reportsTable.getDatasource();
                                boolean modified = datasource.isModified();
                                datasource.addItem(item);
                                ((DatasourceImplementation) datasource).setModified(modified);
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
    }
}