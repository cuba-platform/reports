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
import java.util.*;

public class TemplateEditor extends AbstractEditor<ReportTemplate> {

    @Inject
    protected Label isCustomLabel;

    @Inject
    protected CheckBox custom;

    @Inject
    protected Label templateFileLabel;

    @Inject
    protected FileUploadField templateUploadField;

    @Inject
    protected RadioButtonGroup<Boolean> isGroovyRadioButtonGroup;

    @Inject
    protected Label<String> isGroovyLabel;

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
    protected TableEditFrame tableEdit;

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

    @Inject
    private LinkButton namePatternTextHelp;

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
        namePatternTextHelp.setAction(new AbstractAction("") {
            @Override
            public void actionPerform(Component component) {
                showMessageDialog(getMessage("template.namePatternText"), getMessage("template.namePatternTextHelp"),
                        MessageType.CONFIRMATION_HTML
                                .modal(false)
                                .width(560f));
            }
        });

        Map<String, Boolean> groovyOptions = new HashMap<>();
        groovyOptions.put(getMessage("template.freemarkerType"), Boolean.FALSE);
        groovyOptions.put(getMessage("template.groovyType"), Boolean.TRUE);
        isGroovyRadioButtonGroup.setOptionsMap(groovyOptions);
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
                ReportOutputType prevOutputType = (ReportOutputType) e.getPrevValue();
                ReportOutputType newOutputType = (ReportOutputType) e.getValue();
                setupVisibility(reportTemplate.getCustom(), newOutputType);
                if (hasHtmlCsvTemplateOutput(prevOutputType) && !hasTemplateOutput(newOutputType)) {
                    showMessageDialog(getMessage("templateEditor.warning"), getMessage("templateEditor.clearTemplateMessage"), MessageType.CONFIRMATION);
                }
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
        return Arrays.asList(chartEdit, pivotTableEdit, tableEdit);
    }

    protected boolean hasTemplateOutput(ReportOutputType reportOutputType) {
        return reportOutputType != ReportOutputType.CHART
                && reportOutputType != ReportOutputType.TABLE
                && reportOutputType != ReportOutputType.PIVOT_TABLE;
    }

    protected boolean hasTableTemplateOutput(ReportOutputType reportOutputType) {
        return reportOutputType == ReportOutputType.TABLE;
    }

    protected boolean hasChartTemplateOutput(ReportOutputType reportOutputType) {
        return reportOutputType == ReportOutputType.CHART;
    }

    protected boolean hasHtmlCsvTemplateOutput(ReportOutputType reportOutputType) {
        return reportOutputType == ReportOutputType.CSV || reportOutputType == ReportOutputType.HTML;
    }

    protected void setupVisibility(boolean customEnabled, ReportOutputType reportOutputType) {
        boolean templateOutputVisibility = hasTemplateOutput(reportOutputType);

        boolean chartTemplateOutput = hasChartTemplateOutput(reportOutputType);
        boolean enabled = !chartTemplateOutput && customEnabled;
        custom.setVisible(!chartTemplateOutput);
        isCustomLabel.setVisible(!chartTemplateOutput);

        customDefinedBy.setVisible(enabled);
        customDefinition.setVisible(enabled);
        customDefinedByLabel.setVisible(enabled);
        customDefinitionLabel.setVisible(enabled);

        customDefinedBy.setRequired(enabled);
        customDefinedBy.setRequiredMessage(getMessage("templateEditor.customDefinedBy"));
        customDefinition.setRequired(enabled);
        customDefinition.setRequiredMessage(getMessage("templateEditor.classRequired"));

        boolean supportAlterableForTemplate = templateOutputVisibility && !enabled;
        alterable.setVisible(supportAlterableForTemplate);
        alterableLabel.setVisible(supportAlterableForTemplate);

        templateUploadField.setVisible(templateOutputVisibility);
        templateFileLabel.setVisible(templateOutputVisibility);
        outputNamePattern.setVisible(templateOutputVisibility);
        outputNamePatternLabel.setVisible(templateOutputVisibility);
        namePatternTextHelp.setVisible(templateOutputVisibility);

        setupTemplateTypeVisibility(templateOutputVisibility);

        visibleTemplateEditor(reportOutputType);

        setupVisibilityDescriptionEdit(enabled, reportOutputType);
        setupVisibilityTableEdit(reportOutputType);
    }

    protected void setupVisibilityTableEdit(ReportOutputType reportOutputType) {
        boolean visible = hasTableTemplateOutput(reportOutputType);

        custom.setVisible(!visible);
        isCustomLabel.setVisible(!visible);
        tableEdit.setVisible(visible);
    }

    protected void setupTemplateTypeVisibility(boolean visibility) {
        String extension = "";
        if (getItem().getDocumentName() != null) {
            extension = FilenameUtils.getExtension(getItem().getDocumentName()).toUpperCase();
        }
        isGroovyRadioButtonGroup.setVisible(visibility
                && ReportOutputType.HTML.equals(ReportOutputType.getTypeFromExtension(extension)));
        isGroovyLabel.setVisible(visibility
                && ReportOutputType.HTML.equals(ReportOutputType.getTypeFromExtension(extension)));
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

        if(applicableFrame != null && applicableFrame.getReportTemplate().getReportOutputType() == ReportOutputType.TABLE){
            applicableFrame.hidePreview();
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
            setupTemplateTypeVisibility(hasTemplateOutput(reportTemplate.getReportOutputType()));
            updateOutputType();

            showNotification(getMessage("templateEditor.uploadSuccess"), NotificationType.TRAY);
        });

        ReportTemplate reportTemplate = getItem();
        byte[] templateFile = reportTemplate.getContent();
        if (templateFile != null && !hasChartTemplateOutput(reportTemplate.getReportOutputType())) {
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
            visibleTemplateEditor(null);
            return;
        }
        ReportOutputType outputType = ReportOutputType.getTypeFromExtension(extension.toUpperCase());
        visibleTemplateEditor(outputType);
        if (hasHtmlCsvTemplateOutput(outputType)) {
            String templateContent = new String(reportTemplate.getContent(), StandardCharsets.UTF_8);
            templateFileEditor.setValue(templateContent);
        }
        templateFileEditor.setEditable(security.isEntityOpPermitted(reportTemplate.getMetaClass(), EntityOp.UPDATE));
    }

    protected void visibleTemplateEditor(ReportOutputType outputType) {
        String extension = FilenameUtils.getExtension(templateUploadField.getFileName());
        if (extension == null) {
            templateFileEditor.setVisible(false);
            return;
        }
        templateFileEditor.setVisible(hasHtmlCsvTemplateOutput(outputType));
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

        if (hasTableTemplateOutput(reportTemplate.getReportOutputType())) {
            reportTemplate.setName(".table");
        }

        String extension = FilenameUtils.getExtension(templateUploadField.getFileName());
        if (extension != null) {
            ReportOutputType outputType = ReportOutputType.getTypeFromExtension(extension.toUpperCase());
            if (hasHtmlCsvTemplateOutput(outputType)) {
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