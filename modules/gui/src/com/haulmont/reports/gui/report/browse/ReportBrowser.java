/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.gui.report.browse;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.ClientType;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.app.core.file.FileUploadDialog;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.gui.ReportGuiManager;
import org.apache.commons.io.FileUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class ReportBrowser extends AbstractLookup {

    @Inject
    protected ReportGuiManager reportGuiManager;
    @Inject
    protected FileUploadingAPI fileUpload;
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
    protected GroupTable reportsTable;
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

        final boolean hasPermissionsToCreateReports =
                security.isEntityOpPermitted(metadata.getClassNN(Report.class), EntityOp.CREATE);

        copyReport.setAction(new ItemTrackingAction("copy") {
            @Override
            public void actionPerform(Component component) {
                Report report = reportsTable.getSingleSelected();
                if (report != null) {
                    reportService.copyReport(report);
                    reportsTable.refresh();
                } else {
                    showNotification(getMessage("notification.selectReport"), NotificationType.HUMANIZED);
                }
            }

            @Override
            public boolean isApplicableTo(Datasource.State state, Entity item) {
                return super.isApplicableTo(state, item) && hasPermissionsToCreateReports;
            }
        });

        runReport.setAction(new ItemTrackingAction("runReport") {
            @Override
            public void actionPerform(Component component) {
                Report report = reportsTable.getSingleSelected();
                if (report != null) {
                    report = getDsContext().getDataSupplier().reload(report, "report.edit");
                    if (report.getInputParameters() != null && report.getInputParameters().size() > 0) {
                        Window paramsWindow = openWindow("report$inputParameters", WindowManager.OpenType.DIALOG,
                                Collections.<String, Object>singletonMap("report", report));
                        paramsWindow.addListener(new CloseListener() {
                            @Override
                            public void windowClosed(String actionId) {
                                reportsTable.requestFocus();
                            }
                        });
                    } else {
                        reportGuiManager.printReport(report, Collections.<String, Object>emptyMap(), ReportBrowser.this);
                    }
                }
            }
        });

        importReport.setAction(new AbstractAction("import") {
            @Override
            public void actionPerform(Component component) {
                final FileUploadDialog dialog = openWindow("fileUploadDialog", WindowManager.OpenType.DIALOG);
                dialog.addListener(new CloseListener() {
                    @Override
                    public void windowClosed(String actionId) {
                        if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                            String fileName = dialog.getFileName();
                            int extensionIndex = fileName.lastIndexOf('.');
                            String fileExtension = fileName.substring(extensionIndex + 1).toUpperCase();

                            if (!fileExtension.equals("ZIP")) {
                                String msg = messages.formatMessage(getClass(), "reportException.wrongFileType", fileExtension);
                                showNotification(msg, NotificationType.ERROR);

                            } else {
                                try {
                                    byte[] report = FileUtils.readFileToByteArray(fileUpload.getFile(dialog.getFileId()));
                                    fileUpload.deleteFile(dialog.getFileId());
                                    reportService.importReports(report);
                                } catch (Exception e) {
                                    String msg = getMessage("reportException.unableToImportReport");
                                    showNotification(msg, e.toString(), NotificationType.ERROR);
                                }
                                reportsTable.getDatasource().refresh();
                            }
                        }

                        reportsTable.requestFocus();
                    }
                });
            }
        });

        importReport.setEnabled(hasPermissionsToCreateReports);

        exportReport.setAction(new ItemTrackingAction("export") {
            @Override
            public void actionPerform(Component component) {
                Set<Report> reports = reportsTable.getSelected();
                if ((reports != null) && (!reports.isEmpty())) {
                    ExportDisplay exportDisplay = AppConfig.createExportDisplay(ReportBrowser.this);
                        ByteArrayDataProvider provider = new ByteArrayDataProvider(reportService.exportReports(reports));
                        exportDisplay.show(provider, "Reports", ExportFormat.ZIP);
                }
            }
        });
        reportsTable.addAction(copyReport.getAction());
        reportsTable.addAction(exportReport.getAction());
        reportsTable.addAction(runReport.getAction());

        reportsTable.addAction(new CreateAction(reportsTable) {
            @Override
            protected void afterCommit(Entity entity) {
                reportsTable.expandPath(entity);
            }
        });

        if (hasPermissionsToCreateReports) {
            if (AppConfig.getClientType().equals(ClientType.WEB)) {
                reportsTable.getButtonsPanel().remove(createBtn);
                popupCreateBtn.addAction(new CreateAction(reportsTable) {
                    @Override
                    public String getCaption() {
                        return getMessage("report.new");
                    }
                });
                popupCreateBtn.addAction(new AbstractAction("wizard") {
                    AbstractEditor<ReportData> editor;

                    @Override
                    public void actionPerform(Component component) {
                        editor = openEditor("report$Report.wizard", metadata.create(ReportData.class), WindowManager.OpenType.DIALOG, reportsTable.getDatasource());
                        editor.addListener(new CloseListener() {
                            @Override
                            public void windowClosed(String actionId) {
                                if (actionId.equals(COMMIT_ACTION_ID)) {
                                    reportsTable.refresh();

                                    if (editor.getItem() != null && editor.getItem().getGeneratedReport() != null
                                        /*&& reportDs.getItems() != null && !reportDs.getItems().isEmpty()*/) {

                                        reportsTable.setSelected(editor.getItem().getGeneratedReport());
                                        final AbstractEditor<Report> reportEditor = openEditor("report$Report.edit",
                                                reportDs.getItem(), WindowManager.OpenType.THIS_TAB);
                                        reportEditor.addListener(new CloseListener() {
                                            @Override
                                            public void windowClosed(String actionId) {
                                                if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                                                    Report item = reportEditor.getItem();
                                                    if (item != null) {
                                                        reportDs.updateItem(item);
                                                    }
                                                }
                                                reportsTable.requestFocus();
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public String getCaption() {
                        return getMessage("report.wizard");
                    }
                });
            } else {
                reportsTable.getButtonsPanel().remove(popupCreateBtn);
            }
        }
    }
}