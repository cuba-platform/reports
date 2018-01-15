/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.template.edit;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportOutputType;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.reports.entity.charts.AbstractChartDescription;
import com.haulmont.reports.entity.charts.ChartSeries;
import com.haulmont.reports.entity.charts.ChartType;
import com.haulmont.reports.entity.charts.SerialChartDescription;
import com.haulmont.reports.gui.ReportPrintHelper;
import com.haulmont.reports.gui.datasource.NotPersistenceDatasource;
import com.haulmont.reports.gui.report.run.ShowChartController;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    protected FileUploadingAPI fileUploading;

    @Inject
    protected LookupField outputType;

    @Inject
    protected TextField outputNamePattern;

    @Inject
    protected Label outputNamePatternLabel;

    @Inject
    protected ChartEditFrameController chartEdit;

    @Inject
    protected NotPersistenceDatasource<ReportTemplate> templateDs;

    @Inject
    protected BoxLayout chartEditBox;

    @Inject
    protected BoxLayout chartPreviewBox;

    @Inject
    protected SourceCodeEditor templateFileEditor;

    @Inject
    protected WindowConfig windowConfig;

    @Inject
    protected Metadata metadata;

    public TemplateEditor() {
        showSaveNotification = false;
    }

    @Override
    protected void initNewItem(ReportTemplate template) {
        if (StringUtils.isEmpty(template.getCode())) {
            Report report = template.getReport();
            if (report != null) {
                if ((report.getTemplates() == null) || (report.getTemplates().size() == 0)) {
                    template.setCode(ReportService.DEFAULT_TEMPLATE_CODE);
                } else
                    template.setCode("Template_" + Integer.toString(report.getTemplates().size()));
            }
        }
    }

    @Override
    protected void postInit() {
        super.postInit();

        ReportTemplate reportTemplate = getItem();

        initUploadField();
        templateDs.addItemPropertyChangeListener(e -> {
            if ("reportOutputType".equals(e.getProperty())) {
                setupVisibility(reportTemplate.getCustom(), (ReportOutputType) e.getValue());
            } else if ("custom".equals(e.getProperty())) {
                setupVisibility(Boolean.TRUE.equals(e.getValue()), reportTemplate.getReportOutputType());
            }
        });

        chartEdit.setBands(reportTemplate.getReport().getBands());
        if (reportTemplate.getReportOutputType() == ReportOutputType.CHART) {
            chartEdit.setChartDescription(reportTemplate.getChartDescription());
        }

        ArrayList<ReportOutputType> outputTypes = new ArrayList<>(Arrays.asList(ReportOutputType.values()));

        if (!windowConfig.hasWindow(ShowChartController.JSON_CHART_SCREEN_ID)) {
            outputTypes.remove(ReportOutputType.CHART);
        }

        outputType.setOptionsList(outputTypes);
    }

    protected void initUploadField() {
        ReportTemplate reportTemplate = getItem();
        byte[] templateFile = reportTemplate.getContent();
        if (templateFile != null) {
            templateUploadField.setContentProvider(() ->
                    new ByteArrayInputStream(templateFile));
            FileDescriptor fileDescriptor = metadata.create(FileDescriptor.class);
            fileDescriptor.setName(reportTemplate.getName());
            templateUploadField.setValue(fileDescriptor);
        }
    }

    @Override
    public void ready() {
        super.ready();
        ReportTemplate reportTemplate = getItem();
        setupVisibility(reportTemplate.getCustom(), reportTemplate.getReportOutputType());
        initTemplateEditor(reportTemplate);
    }

    protected void setupVisibility(boolean customEnabled, ReportOutputType reportOutputType) {
        boolean tableOutputType = reportOutputType == ReportOutputType.TABLE;
        boolean chartOutputType = reportOutputType == ReportOutputType.CHART;
        boolean templateOutputVisibility = !(chartOutputType || tableOutputType);

        templateUploadField.setVisible(!customEnabled);
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

        chartEditBox.setVisible(chartOutputType && !customEnabled);
        chartPreviewBox.setVisible(chartOutputType && !customEnabled);
        if (chartOutputType && !customEnabled) {
            chartEdit.showChartPreviewBox();
        } else {
            chartEdit.hideChartPreviewBox();
        }

       templateUploadField.setVisible(templateOutputVisibility);
        templateFileLabel.setVisible(templateOutputVisibility);
        outputNamePattern.setVisible(templateOutputVisibility);
        outputNamePatternLabel.setVisible(templateOutputVisibility);
    }

    @Override
    @SuppressWarnings({"serial", "unchecked"})
    public void init(Map<String, Object> params) {
        super.init(params);

        getDialogOptions()
                .setWidthAuto()
                .setResizable(true);

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
    }

    protected void updateOutputType() {
        setupVisibility(getItem().isCustom(), getItem().getReportOutputType());

        if (outputType.getValue() == null) {
            String extension = FilenameUtils.getExtension(templateUploadField.getFileDescriptor().getName()).toUpperCase();
            ReportOutputType reportOutputType = ReportOutputType.getTypeFromExtension(extension);
            if (reportOutputType != null) {
                outputType.setValue(reportOutputType);
            }
        }
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
    public boolean commit(boolean validate) {
        if (!validateTemplateFile() || !validateInputOutputFormats()) {
            return false;
        }
        ReportTemplate reportTemplate = getItem();
        if (reportTemplate.getReportOutputType() == ReportOutputType.CHART) {
            if (!validateChart()) {
                return false;
            }
            AbstractChartDescription chartDescription = chartEdit.getChartDescription();
            reportTemplate.setChartDescription(chartDescription);
        }
        if (reportTemplate.getReportOutputType() == ReportOutputType.TABLE) {
            reportTemplate.setTableName();
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

        return super.commit(validate);
    }

    protected boolean validateInputOutputFormats() {
        ReportTemplate reportTemplate = getItem();
        String name = reportTemplate.getName();
        if (!Boolean.TRUE.equals(reportTemplate.getCustom())
                && reportTemplate.getReportOutputType() != ReportOutputType.CHART
                && reportTemplate.getReportOutputType() != ReportOutputType.TABLE
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
                && template.getReportOutputType() != ReportOutputType.CHART
                && template.getReportOutputType() != ReportOutputType.TABLE
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

    protected boolean validateChart() {
        AbstractChartDescription chartDescription = chartEdit.getChartDescription();
        if (chartDescription.getType() == ChartType.SERIAL) {
            List<ChartSeries> series = ((SerialChartDescription) chartDescription).getSeries();
            if (series == null || series.size() == 0) {
                showNotification(getMessage("validationFail.caption"),
                        getMessage("chartEdit.seriesEmptyMsg"), NotificationType.TRAY);
                return false;
            }
            for (ChartSeries it : series) {
                if (it.getType() == null) {
                    showNotification(getMessage("validationFail.caption"),
                            getMessage("chartEdit.seriesTypeNullMsg"), NotificationType.TRAY);
                    return false;
                }
                if (it.getValueField() == null) {
                    showNotification(getMessage("validationFail.caption"),
                            getMessage("chartEdit.seriesValueFieldNullMsg"), NotificationType.TRAY);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void commitAndClose() {
        if (!validateTemplateFile()) {
            return;
        }

        if (!getItem().getCustom()) {
            getItem().setCustomDefinition("");
        }
        if (commit(true)) {
            close(COMMIT_ACTION_ID);
        }
    }
}