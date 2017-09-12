/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.report.importdialog;

import com.haulmont.cuba.core.global.ClientType;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.ReportImportOption;
import com.haulmont.reports.entity.ReportImportResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.io.File;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

public class ReportImportDialog extends AbstractWindow {
    @Inject
    protected FileUploadField fileUpload;
    @Inject
    protected Label fileName;
    @Inject
    protected CheckBox importRoles;
    @Inject
    protected FileUploadingAPI fileUploadingApi;
    @Inject
    protected ReportService reportService;
    @Inject
    protected HBoxLayout dropZone;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        initWindowActions();
        fileUpload.addFileUploadSucceedListener(e -> {
            fileName.setValue(fileUpload.getFileName());
        });
        importRoles.setValue(Boolean.TRUE);

        if (AppConfig.getClientType() != ClientType.WEB) {
            dropZone.setVisible(false);
        }
    }

    protected void initWindowActions() {
        addAction(new AbstractAction("windowCommit") {
            @Override
            public void actionPerform(Component component) {
                if (validateAll()) {
                    importReport();
                    close(COMMIT_ACTION_ID);
                }
            }

            @Override
            public String getCaption() {
                return messages.getMainMessage("actions.Ok");
            }
        });
        addAction(new AbstractAction("windowClose") {
            @Override
            public void actionPerform(Component component) {
                close(CLOSE_ACTION_ID);
            }

            @Override
            public String getCaption() {
                return messages.getMainMessage("actions.Cancel");
            }
        });
    }

    protected void importReport() {
        try {
            UUID fileID = fileUpload.getFileId();
            File file = fileUploadingApi.getFile(fileID);
            byte[] bytes = FileUtils.readFileToByteArray(file);
            fileUploadingApi.deleteFile(fileID);
            ReportImportResult result = reportService.importReportsWithResult(bytes, getImportOptions());
            showNotification(formatMessage("importResult",
                    result.getCreatedReports().size(), result.getUpdatedReports().size()),
                    NotificationType.HUMANIZED);
        } catch (Exception e) {
            String msg = getMessage("reportException.unableToImportReport");
            showNotification(msg, e.toString(), NotificationType.ERROR);
        }
    }

    protected EnumSet<ReportImportOption> getImportOptions() {
        if (BooleanUtils.isNotTrue(importRoles.getValue())) {
            return EnumSet.of(ReportImportOption.DO_NOT_IMPORT_ROLES);
        }
        return null;
    }

    @Override
    protected void postValidate(ValidationErrors errors) {
        super.postValidate(errors);
        if (fileUpload.getFileId() == null) {
            errors.add(getMessage("reportException.noFile"));
            return;
        }
        String extension = FilenameUtils.getExtension(fileUpload.getFileName());
        if (!StringUtils.equalsIgnoreCase(extension, ExportFormat.ZIP.getFileExt())) {
            errors.add(formatMessage("reportException.wrongFileType", extension));
        }
    }
}
