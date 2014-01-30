/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.template.edit;

import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author artamonov
 * @version $Id$
 */
public class TemplateEditor extends AbstractEditor<ReportTemplate> {
    protected static final String DEFAULT_TEMPLATE_CODE = "DEFAULT";

    @Inject
    protected Button templatePath;

    @Inject
    protected TextField customClass;

    @Inject
    protected CheckBox customFlag;

    @Inject
    protected FileUploadField uploadTemplate;

    @Inject
    protected Messages messages;

    @Inject
    protected FileUploadingAPI fileUploading;

    public TemplateEditor() {
        showSaveNotification = false;
    }

    @Override
    protected void initNewItem(ReportTemplate template) {
        if (StringUtils.isEmpty(template.getCode())) {
            Report report = template.getReport();
            if (report != null) {
                if ((report.getTemplates() == null) || (report.getTemplates().size() == 0)) {
                    template.setCode(DEFAULT_TEMPLATE_CODE);
                } else
                    template.setCode("Template_" + Integer.toString(report.getTemplates().size()));
            }
        }
    }

    @Override
    protected void postInit() {
        super.postInit();

        enableCustomProps(getItem().getCustomFlag());

        templatePath.setCaption(getItem().getName());
        updateTemplatePathVisibility();

    }

    private void updateTemplatePathVisibility() {
        templatePath.setVisible(StringUtils.isNotEmpty(getItem().getName()));
    }

    private void enableCustomProps(boolean customEnabled) {
        templatePath.setEnabled(!customEnabled);
        uploadTemplate.setEnabled(!customEnabled);
        customClass.setEnabled(customEnabled);
        customClass.setRequired(customEnabled);
        customClass.setRequiredMessage(messages.getMessage(TemplateEditor.class,
                "templateEditor.classRequired"));
    }

    @Override
    @SuppressWarnings({"serial", "unchecked"})
    public void init(Map<String, Object> params) {
        super.init(params);

        getDialogParams().setWidth(490);

        customFlag.addListener(new ValueListener() {
            @Override
            public void valueChanged(Object source, String property, Object prevValue, Object value) {
                Boolean isCustom = Boolean.TRUE.equals(value);
                enableCustomProps(isCustom);
            }
        });

        FileUploadField.Listener uploadListener = new FileUploadField.ListenerAdapter() {

            @Override
            public void uploadStarted(Event event) {
                uploadTemplate.setEnabled(false);
            }

            @Override
            public void uploadFinished(Event event) {
                uploadTemplate.setEnabled(true);
            }

            @Override
            public void uploadSucceeded(Event event) {
                getItem().setName(uploadTemplate.getFileName());
                File file = fileUploading.getFile(uploadTemplate.getFileId());
                try {
                    byte[] data = FileUtils.readFileToByteArray(file);
                    getItem().setContent(data);
                } catch (IOException e) {
                    throw new RuntimeException(
                            String.format("An error occurred while uploading file for template [%s]", getItem().getCode()));
                }
                templatePath.setCaption(uploadTemplate.getFileName());
                updateTemplatePathVisibility();
                showNotification(messages.getMessage(TemplateEditor.class,
                        "templateEditor.uploadSuccess"), IFrame.NotificationType.HUMANIZED);
            }

            @Override
            public void uploadFailed(Event event) {
                showNotification(messages.getMessage(TemplateEditor.class,
                        "templateEditor.uploadUnsuccess"), IFrame.NotificationType.WARNING);
            }
        };
        uploadTemplate.addListener(uploadListener);

        templatePath.setAction(new AbstractAction("report.template") {
            @Override
            public void actionPerform(Component component) {
                if (getItem().getContent() != null) {
                    ExportDisplay display = AppConfig.createExportDisplay(TemplateEditor.this);
                    display.show(new ByteArrayDataProvider(getItem().getContent()), getItem().getName(),
                            ExportFormat.getByExtension(getItem().getExt()));
                }
            }
        });
    }

    @Override
    public boolean commit(boolean validate) {
        if (!validateTemplateFile()) return false;

        return super.commit(validate);
    }

    protected boolean validateTemplateFile() {
        ReportTemplate template = getItem();
        if (!BooleanUtils.isTrue(template.getCustomFlag()) && template.getContent() == null) {
            StringBuilder notification = new StringBuilder(getMessage("template.uploadTemplate"));

            if (StringUtils.isEmpty(template.getCode())) {
                notification.append("\n").append(getMessage("template.codeMsg"));
            }

            if (template.getOutputType() == null) {
                notification.append("\n").append(getMessage("template.outputTypeMsg"));
            }

            showNotification(getMessage("validationFail.caption"),
                    notification.toString(), NotificationType.TRAY);

            return false;
        }
        return true;
    }

    @Override
    public void commitAndClose() {
        if (!validateTemplateFile())
            return;

        if (!getItem().getCustomFlag()) {
            getItem().setCustomClass("");
        }
        if (commit(true))
            close(COMMIT_ACTION_ID);
    }
}