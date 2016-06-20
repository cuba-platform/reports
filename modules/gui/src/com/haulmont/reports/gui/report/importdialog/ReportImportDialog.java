/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.report.importdialog;

import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.ReportImportOption;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.io.File;
import java.util.*;

public class ReportImportDialog extends AbstractWindow {
    @Inject
    protected FileUploadField fileUpload;
    @Inject
    protected LinkButton fileName;
    @Inject
    protected CheckBox importRoles;
    @Inject
    protected FileUploadingAPI fileUploadingApi;
    @Inject
    protected ReportService reportService;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        initWindowActions();
        fileUpload.addFileUploadSucceedListener(e -> {
            fileName.setCaption(fileUpload.getFileName());
        });
        importRoles.setValue(Boolean.TRUE);
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
            reportService.importReports(bytes, getImportOptions());
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
