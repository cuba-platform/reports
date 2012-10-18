/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
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
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.gui.ReportHelper;
import org.apache.commons.io.FileUtils;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class ReportBrowser extends AbstractLookup {

    @Inject
    private FileUploadingAPI fileUpload;

    @Inject
    private ReportService reportService;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        final Table reportsTable = getComponent("table");
        Button runReport = getComponent("runReport");
        runReport.setAction(new ItemTrackingAction("runReport") {
            @Override
            public void actionPerform(Component component) {
                Report report = reportsTable.getSingleSelected();
                if (report != null) {
                    report = getDsContext().getDataService().reload(report, "report.edit");
                    if (report.getInputParameters() != null && report.getInputParameters().size() > 0) {
                        openWindow("report$inputParameters", WindowManager.OpenType.DIALOG, Collections.<String, Object>singletonMap("report", report));
                    } else {
                        ReportHelper.printReport(report, Collections.<String, Object>emptyMap());
                    }
                }
            }
        });

        Button importReport = getComponent("import");
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

        Button exportReport = getComponent("export");
        exportReport.setAction(new ItemTrackingAction("export") {
            @Override
            public void actionPerform(Component component) {
                Set<Report> reports = reportsTable.getSelected();
                if ((reports != null) && (!reports.isEmpty())) {
                    try {
                        ExportDisplay exportDisplay = AppConfig.createExportDisplay();
                        exportDisplay.show(
                                new ByteArrayDataProvider(reportService.exportReports(reports)), "Reports", ExportFormat.ZIP);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        reportsTable.addAction(exportReport.getAction());
        reportsTable.addAction(runReport.getAction());
    }
}