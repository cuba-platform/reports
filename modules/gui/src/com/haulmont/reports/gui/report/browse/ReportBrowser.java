/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.gui.report.browse;

import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.app.core.file.FileUploadDialog;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.gui.ReportGuiManager;
import org.apache.commons.io.FileUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

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
    protected Table reportsTable;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);


        copyReport.setAction(new AbstractAction("copy") {
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
        });

        runReport.setAction(new ItemTrackingAction("runReport") {
            @Override
            public void actionPerform(Component component) {
                Report report = reportsTable.getSingleSelected();
                if (report != null) {
                    report = getDsContext().getDataSupplier().reload(report, "report.edit");
                    if (report.getInputParameters() != null && report.getInputParameters().size() > 0) {
                        openWindow("report$inputParameters", WindowManager.OpenType.DIALOG, Collections.<String, Object>singletonMap("report", report));
                    } else {
                        reportGuiManager.printReport(report, Collections.<String, Object>emptyMap());
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
                            try {
                                byte[] report = FileUtils.readFileToByteArray(fileUpload.getFile(dialog.getFileId()));
                                fileUpload.deleteFile(dialog.getFileId());
                                reportService.importReports(report);
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                            reportsTable.getDatasource().refresh();
                        }
                    }
                });
            }
        });

        exportReport.setAction(new ItemTrackingAction("export") {
            @Override
            public void actionPerform(Component component) {
                Set<Report> reports = reportsTable.getSelected();
                if ((reports != null) && (!reports.isEmpty())) {
                    try {
                        ExportDisplay exportDisplay = AppConfig.createExportDisplay(ReportBrowser.this);
                        exportDisplay.show(
                                new ByteArrayDataProvider(reportService.exportReports(reports)), "Reports", ExportFormat.ZIP);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        reportsTable.addAction(copyReport.getAction());
        reportsTable.addAction(exportReport.getAction());
        reportsTable.addAction(runReport.getAction());
    }
}