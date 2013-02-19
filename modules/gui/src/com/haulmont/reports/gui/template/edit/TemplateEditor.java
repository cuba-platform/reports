/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports.gui.template.edit;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportTemplate;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author artamonov
 * @version $Id$
 */
public class TemplateEditor extends AbstractEditor {
    private static final String DEFAULT_TEMPLATE_CODE = "DEFAULT";

    private ReportTemplate template;

    @Inject
    private Button templatePath;

    @Inject
    private TextField customClass;

    @Inject
    private FileUploadField uploadTemplate;

    @Inject
    private Messages messages;

    @Inject
    private FileUploadingAPI fileUploading;

    private Map<ReportTemplate, ArrayList<FileDescriptor>> deletedContainer;
    private List<FileDescriptor> deletedList = new ArrayList<>();

    @Override
    public void setItem(Entity item) {
        template = (ReportTemplate)item;
        if (StringUtils.isEmpty(template.getCode())) {
            Report report = template.getReport();
            if (report != null) {
                if ((report.getTemplates() == null) || (report.getTemplates().size() == 0)) {
                    template.setCode(DEFAULT_TEMPLATE_CODE);
                    template.setDefaultFlag(true);
                } else
                    template.setCode("Template_" + Integer.toString(report.getTemplates().size()));
            }
        }
        super.setItem(template);

        template = (ReportTemplate) getItem();
        enableCustomProps(template.getCustomFlag());

        FileDescriptor templateDescriptor = template.getTemplateFileDescriptor();
        if (templateDescriptor != null)
            templatePath.setCaption(templateDescriptor.getName());
    }

    private void enableCustomProps(boolean customEnabled) {
        templatePath.setEnabled(!customEnabled);
        uploadTemplate.setEnabled(!customEnabled);
        customClass.setEnabled(customEnabled);
    }

    @Override
    @SuppressWarnings({"serial", "unchecked"})
    public void init(Map<String, Object> params) {
        super.init(params);

        getDialogParams().setWidth(490);

        deletedContainer = (java.util.Map) params.get("deletedContainer");

        CheckBox custom = getComponent("customFlag");
        custom.addListener(new ValueListener() {
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
                FileDescriptor templateDescriptor = uploadTemplate.getFileDescriptor();

                try {
                    fileUploading.putFileIntoStorage(uploadTemplate.getFileId(), templateDescriptor);
                } catch (FileStorageException e) {
                    throw new RuntimeException(e);
                }

                templatePath.setCaption(templateDescriptor.getName());

                if (template.getTemplateFileDescriptor() != null)
                    deletedList.add(template.getTemplateFileDescriptor());
                template.setTemplateFileDescriptor(templateDescriptor);

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
                if (template.getTemplateFileDescriptor() != null) {
                    ExportDisplay display = AppConfig.createExportDisplay(TemplateEditor.this);
                    display.show(template.getTemplateFileDescriptor());
                }
            }
        });
    }

    @Override
    public boolean commit() {
        boolean result = super.commit();
        if (result) {
            if (deletedContainer.get(template) == null)
                deletedContainer.put(template, new ArrayList<FileDescriptor>());
            List<FileDescriptor> deletedFilesList = deletedContainer.get(template);
            deletedFilesList.addAll(deletedList);
        }
        return result;
    }

    @Override
    public boolean commit(boolean validate) {
        if (!validateTemplateFile()) return false;

        boolean result = super.commit(validate);
        if (result && (deletedList.size() > 0)) {
            if (deletedContainer.get(template) == null)
                deletedContainer.put(template, new ArrayList<FileDescriptor>());
            List<FileDescriptor> deletedFilesList = deletedContainer.get(template);
            deletedFilesList.addAll(deletedList);
        }
        return result;
    }

    private boolean validateTemplateFile() {
        if (template.getTemplateFileDescriptor() == null)  {
            showNotification(getMessage("validationFail.caption"),
                    getMessage("template.uploadTemplate"), NotificationType.TRAY);
            return false;
        }
        return true;
    }

    @Override
    public void commitAndClose() {
        if (!validateTemplateFile())
            return;

        if (!template.getCustomFlag()) {
            template.setCustomClass("");
        }
        if (commit(true))
            close(COMMIT_ACTION_ID);
    }
}