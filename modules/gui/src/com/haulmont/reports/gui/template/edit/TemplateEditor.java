/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.template.edit;

import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.theme.ThemeConstants;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportOutputType;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.reports.entity.charts.AbstractChartDescription;
import com.haulmont.reports.gui.ReportPrintHelper;
import com.haulmont.reports.gui.datasource.NotPersistenceDatasource;
import com.haulmont.reports.gui.report.run.ShowChartController;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 */
public class TemplateEditor extends AbstractEditor<ReportTemplate> {
    @Inject
    protected Label templateFileLabel;

    @Inject
    protected LinkButton templatePath;

    @Inject
    protected FileUploadField uploadTemplate;

    @Inject
    protected TextField customDefinition;

    @Inject
    protected Label customDefinitionLabel;

    @Inject
    protected LookupField customDefinedBy;

    @Inject
    protected Label customDefinedByLabel;

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
    protected ThemeConstants themeConstants;

    @Inject
    protected WindowConfig windowConfig;

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
        templatePath.setCaption(reportTemplate.getName());
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

    @Override
    public void ready() {
        super.ready();

        ReportTemplate reportTemplate = getItem();
        setupVisibility(reportTemplate.getCustom(), reportTemplate.getReportOutputType());
    }

    protected void setupVisibility(boolean customEnabled, ReportOutputType reportOutputType) {
        uploadTemplate.setVisible(!customEnabled);
        customDefinedBy.setVisible(customEnabled);
        customDefinition.setVisible(customEnabled);
        customDefinedByLabel.setVisible(customEnabled);
        customDefinitionLabel.setVisible(customEnabled);

        customDefinedBy.setRequired(customEnabled);
        customDefinedBy.setRequiredMessage(getMessage("templateEditor.customDefinedBy"));
        customDefinition.setRequired(customEnabled);
        customDefinition.setRequiredMessage(getMessage("templateEditor.classRequired"));

        boolean chartOutputType = reportOutputType == ReportOutputType.CHART;
        chartEditBox.setVisible(chartOutputType && !customEnabled);
        chartPreviewBox.setVisible(chartOutputType && !customEnabled);
        if (chartOutputType && !customEnabled) {
            chartEdit.showChartPreviewBox();
        } else {
            chartEdit.hideChartPreviewBox();
        }

        uploadTemplate.setVisible(!chartOutputType);
        templatePath.setVisible(!chartOutputType);
        templateFileLabel.setVisible(!chartOutputType);
        outputNamePattern.setVisible(!chartOutputType);
        outputNamePatternLabel.setVisible(!chartOutputType);

        templatePath.setVisible(!customEnabled && !chartOutputType && StringUtils.isNotEmpty(getItem().getName()));
    }

    @Override
    @SuppressWarnings({"serial", "unchecked"})
    public void init(Map<String, Object> params) {
        super.init(params);

        getDialogOptions()
                .setWidthAuto()
                .setResizable(true);

        uploadTemplate.addFileUploadErrorListener(e ->
                showNotification(getMessage("templateEditor.uploadUnsuccess"), NotificationType.WARNING));

        uploadTemplate.addFileUploadSucceedListener(e -> {
            getItem().setName(uploadTemplate.getFileName());
            File file = fileUploading.getFile(uploadTemplate.getFileId());
            try {
                byte[] data = FileUtils.readFileToByteArray(file);
                getItem().setContent(data);
            } catch (IOException ex) {
                throw new RuntimeException(
                        String.format("An error occurred while uploading file for template [%s]", getItem().getCode()), ex);
            }
            templatePath.setCaption(uploadTemplate.getFileName());
            setupVisibility(getItem().isCustom(), getItem().getReportOutputType());
            showNotification(getMessage("templateEditor.uploadSuccess"), NotificationType.TRAY);
        });

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
        if (!validateTemplateFile() || !validateInputOutputFormats()) return false;

        ReportTemplate reportTemplate = getItem();
        if (reportTemplate.getReportOutputType() == ReportOutputType.CHART) {
            AbstractChartDescription chartDescription = chartEdit.getChartDescription();
            reportTemplate.setChartDescription(chartDescription);
        }

        return super.commit(validate);
    }

    protected boolean validateInputOutputFormats() {
        ReportTemplate reportTemplate = getItem();
        String name = reportTemplate.getName();
        if (!Boolean.TRUE.equals(reportTemplate.getCustom())
                && reportTemplate.getReportOutputType() != ReportOutputType.CHART
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