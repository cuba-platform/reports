/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.template.edit;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportOutputType;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.reports.gui.ReportPrintHelper;
import com.haulmont.reports.gui.datasource.NotPersistenceDatasource;
import com.haulmont.reports.gui.report.run.ShowChartController;
import com.haulmont.reports.gui.report.run.ShowPivotTableController;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class TemplateEditor extends AbstractEditor<ReportTemplate> {

    @Inject
    protected Label templateFileLabel;

    @Inject
    protected FileUploadField templateUploadField;

    @Inject
    protected TextField customDefinition;

    @Inject
    protected Label customDefinitionLabel;

    @Inject
    protected LookupField customDefinedBy;

    @Inject
    protected Label customDefinedByLabel;

    @Inject
    protected CheckBox alterable;

    @Inject
    protected Label alterableLabel;

    @Inject
    protected LookupField<ReportOutputType> outputType;

    @Inject
    protected TextField outputNamePattern;

    @Inject
    protected Label outputNamePatternLabel;

    @Inject
    protected ChartEditFrame chartEdit;

    @Inject
    protected PivotTableEditFrame pivotTableEdit;

    @Inject
    protected NotPersistenceDatasource<ReportTemplate> templateDs;

    @Inject
    protected BoxLayout descriptionEditBox;

    @Inject
    protected BoxLayout previewBox;

    @Inject
    protected SourceCodeEditor templateFileEditor;

    @Inject
    protected WindowConfig windowConfig;

    @Inject
    protected Metadata metadata;

    @Inject
    protected Security security;

    @Inject
    protected FileUploadingAPI fileUploading;

    public TemplateEditor() {
        showSaveNotification = false;
    }

    @Override
    @SuppressWarnings({"serial", "unchecked"})
    public void init(Map<String, Object> params) {
        super.init(params);
        getDialogOptions()
                .setWidthAuto()
                .setResizable(true);
    }

    @Override
    protected void initNewItem(ReportTemplate template) {
        if (StringUtils.isEmpty(template.getCode())) {
            Report report = template.getReport();
            if (report != null) {
                if (report.getTemplates() == null || report.getTemplates().isEmpty())
                    template.setCode(ReportService.DEFAULT_TEMPLATE_CODE);
                else
                    template.setCode("Template_" + Integer.toString(report.getTemplates().size()));
            }
        }
    }

    @Override
    protected void postInit() {
        super.postInit();
        initUploadField();
        templateDs.addItemPropertyChangeListener(e -> {
            ReportTemplate reportTemplate = getItem();
            if ("reportOutputType".equals(e.getProperty())) {
                setupVisibility(reportTemplate.getCustom(), (ReportOutputType) e.getValue());
            } else if ("custom".equals(e.getProperty())) {
                setupVisibility(Boolean.TRUE.equals(e.getValue()), reportTemplate.getReportOutputType());
            }
        });
        initOutputTypeList();
    }

    @Override
    public void ready() {
        super.ready();
        ReportTemplate reportTemplate = getItem();
        initTemplateEditor(reportTemplate);
        getDescriptionEditFrames().forEach(controller -> controller.setItem(reportTemplate));
        setupVisibility(reportTemplate.getCustom(), reportTemplate.getReportOutputType());
    }

    protected Collection<DescriptionEditFrame> getDescriptionEditFrames() {
        return Arrays.asList(chartEdit, pivotTableEdit);
    }

    protected boolean hasTemplateOutput(ReportOutputType reportOutputType) {
        return reportOutputType != ReportOutputType.CHART
                && reportOutputType != ReportOutputType.TABLE
                && reportOutputType != ReportOutputType.PIVOT_TABLE;
    }

    protected void setupVisibility(boolean customEnabled, ReportOutputType reportOutputType) {
        boolean templateOutputVisibility = hasTemplateOutput(reportOutputType);

        customDefinedBy.setVisible(customEnabled);
        customDefinition.setVisible(customEnabled);
        customDefinedByLabel.setVisible(customEnabled);
        customDefinitionLabel.setVisible(customEnabled);

        customDefinedBy.setRequired(customEnabled);
        customDefinedBy.setRequiredMessage(getMessage("templateEditor.customDefinedBy"));
        customDefinition.setRequired(customEnabled);
        customDefinition.setRequiredMessage(getMessage("templateEditor.classRequired"));

        boolean supportAlterableForTemplate = templateOutputVisibility && !customEnabled;
        alterable.setVisible(supportAlterableForTemplate);
        alterableLabel.setVisible(supportAlterableForTemplate);

        templateUploadField.setVisible(templateOutputVisibility);
        templateFileLabel.setVisible(templateOutputVisibility);
        outputNamePattern.setVisible(templateOutputVisibility);
        outputNamePatternLabel.setVisible(templateOutputVisibility);

        setupVisibilityDescriptionEdit(customEnabled, reportOutputType);
    }

    protected void setupVisibilityDescriptionEdit(boolean customEnabled, ReportOutputType reportOutputType) {
        DescriptionEditFrame applicableFrame =
                getDescriptionEditFrames().stream()
                        .filter(c -> c.isApplicable(reportOutputType))
                        .findFirst().orElse(null);
        if (applicableFrame != null) {
            descriptionEditBox.setVisible(!customEnabled);
            applicableFrame.setVisible(!customEnabled);
            applicableFrame.setItem(getItem());
            if (!customEnabled) {
                applicableFrame.showPreview();
            } else {
                applicableFrame.hidePreview();
            }
        }

        for (DescriptionEditFrame frame : getDescriptionEditFrames()) {
            if (applicableFrame != frame) {
                frame.setVisible(false);
            }
            if (applicableFrame == null) {
                frame.hidePreview();
                descriptionEditBox.setVisible(false);
            }
        }
    }

    protected void updateOutputType() {
        if (outputType.getValue() == null) {
            String extension = FilenameUtils.getExtension(templateUploadField.getFileDescriptor().getName()).toUpperCase();
            ReportOutputType reportOutputType = ReportOutputType.getTypeFromExtension(extension);
            if (reportOutputType != null) {
                outputType.setValue(reportOutputType);
            }
        }
    }

    protected void initOutputTypeList() {
        ArrayList<ReportOutputType> outputTypes = new ArrayList<>(Arrays.asList(ReportOutputType.values()));

        if (!windowConfig.hasWindow(ShowChartController.JSON_CHART_SCREEN_ID)) {
            outputTypes.remove(ReportOutputType.CHART);
        }
        if (!windowConfig.hasWindow(ShowPivotTableController.PIVOT_TABLE_SCREEN_ID)) {
            outputTypes.remove(ReportOutputType.PIVOT_TABLE);
        }

        outputType.setOptionsList(outputTypes);
    }

    protected void initUploadField() {
        templateUploadField.addFileUploadErrorListener(e ->
                showNotification(getMessage("templateEditor.uploadUnsuccess"), NotificationType.WARNING));
        templateUploadField.addFileUploadSucceedListener(e -> {
            String fileName = templateUploadField.getFileName();
            ReportTemplate reportTemplate = getItem();
            reportTemplate.setName(fileName);

            File file = fileUploading.getFile(templateUploadField.getFileId());
            try {
                byte[] data = FileUtils.readFileToByteArray(file);
                reportTemplate.setContent(data);
            } catch (IOException ex) {
                throw new RuntimeException(
                        String.format("An error occurred while uploading file for template [%s]", getItem().getCode()), ex);
            }
            initTemplateEditor(reportTemplate);
            updateOutputType();

            showNotification(getMessage("templateEditor.uploadSuccess"), NotificationType.TRAY);
        });

        ReportTemplate reportTemplate = getItem();
        byte[] templateFile = reportTemplate.getContent();
        if (templateFile != null) {
            templateUploadField.setContentProvider(() -> new ByteArrayInputStream(templateFile));
            FileDescriptor fileDescriptor = metadata.create(FileDescriptor.class);
            fileDescriptor.setName(reportTemplate.getName());
            templateUploadField.setValue(fileDescriptor);
        }

        boolean updatePermitted = security.isEntityOpPermitted(reportTemplate.getMetaClass(), EntityOp.UPDATE)
                && security.isEntityAttrUpdatePermitted(reportTemplate.getMetaClass(), "content");

        templateUploadField.setEditable(updatePermitted);
    }

    protected void initTemplateEditor(ReportTemplate reportTemplate) {
        templateFileEditor.setMode(SourceCodeEditor.Mode.HTML);
        String extension = FilenameUtils.getExtension(templateUploadField.getFileName());
        if (extension == null) {
            templateFileEditor.setVisible(false);
            return;
        }
        ReportOutputType outputType = ReportOutputType.getTypeFromExtension(extension.toUpperCase());
        if (outputType == ReportOutputType.CSV || outputType == ReportOutputType.HTML) {
            templateFileEditor.setVisible(true);
            String templateContent = new String(reportTemplate.getContent(), StandardCharsets.UTF_8);
            templateFileEditor.setValue(templateContent);
        } else {
            templateFileEditor.setVisible(false);
        }
    }

    @Override
    public boolean preCommit() {
        if (!validateTemplateFile() || !validateInputOutputFormats()) {
            return false;
        }
        ReportTemplate reportTemplate = getItem();
        for (DescriptionEditFrame frame : getDescriptionEditFrames()) {
            if (frame.isApplicable(reportTemplate.getReportOutputType())) {
                if (!frame.applyChanges()) {
                    return false;
                }
            }
        }

        if (!Boolean.TRUE.equals(reportTemplate.getCustom())) {
            reportTemplate.setCustomDefinition("");
        }

        if (reportTemplate.getReportOutputType() == ReportOutputType.TABLE) {
            reportTemplate.setName(".table");
        }

        String extension = FilenameUtils.getExtension(templateUploadField.getFileName());
        if (extension != null) {
            ReportOutputType outputType = ReportOutputType.getTypeFromExtension(extension.toUpperCase());
            if (outputType == ReportOutputType.CSV || outputType == ReportOutputType.HTML) {
                byte[] bytes = templateFileEditor.getValue() == null ?
                        new byte[0] :
                        templateFileEditor.getValue().getBytes(StandardCharsets.UTF_8);
                reportTemplate.setContent(bytes);
            }
        }

        return super.preCommit();
    }

    protected boolean validateInputOutputFormats() {
        ReportTemplate reportTemplate = getItem();
        String name = reportTemplate.getName();
        if (!Boolean.TRUE.equals(reportTemplate.getCustom())
                && hasTemplateOutput(reportTemplate.getReportOutputType())
                && name != null) {
            String inputType = name.contains(".") ? name.substring(name.lastIndexOf(".") + 1) : "";

            ReportOutputType outputTypeValue = outputType.getValue();
            if (!ReportPrintHelper.getInputOutputTypesMapping().containsKey(inputType) ||
                    !ReportPrintHelper.getInputOutputTypesMapping().get(inputType).contains(outputTypeValue)) {
                showNotification(getMessage("inputOutputTypesError"), NotificationType.TRAY);
                return false;
            }
        }
        return true;
    }

    protected boolean validateTemplateFile() {
        ReportTemplate template = getItem();
        if (!Boolean.TRUE.equals(template.getCustom())
                && hasTemplateOutput(template.getReportOutputType())
                && template.getContent() == null) {
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
}