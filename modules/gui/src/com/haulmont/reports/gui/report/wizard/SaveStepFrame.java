/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.report.wizard;

import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportOutputType;
import com.haulmont.reports.entity.charts.*;
import com.haulmont.reports.entity.wizard.TemplateFileType;
import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.reports.gui.report.run.ShowChartController;
import com.haulmont.reports.gui.report.wizard.step.StepFrame;
import com.haulmont.reports.gui.template.edit.RandomChartDataGenerator;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $Id$
 */
class SaveStepFrame extends StepFrame {
    public SaveStepFrame(ReportWizardCreator wizard) {
        super(wizard, wizard.getMessage("saveReport"), "saveStep");
        isLast = true;
        beforeShowFrameHandler = new BeforeShowSaveStepFrameHandler();

        beforeHideFrameHandler = new BeforeHideSaveStepFrameHandler();
    }

    protected class BeforeShowSaveStepFrameHandler implements BeforeShowStepFrameHandler {
        @Override
        public void beforeShowFrame() {
            initSaveAction();
            initDownloadAction();

            if (StringUtils.isEmpty(wizard.outputFileName.<String>getValue())) {
                wizard.outputFileName.setValue(wizard.generateOutputFileName(wizard.templateFileFormat.getValue().toString().toLowerCase()));
            }
            wizard.setCorrectReportOutputType();

            initChartPreview();
        }

        protected void initChartPreview() {
            if (wizard.outputFileFormat.getValue() == ReportOutputType.CHART) {
                wizard.chartPreviewBox.setVisible(true);
                wizard.diagramTypeLabel.setVisible(true);
                wizard.diagramType.setVisible(true);

                showChart();

                ReportWizardCreator.Companion companion = wizard.getCompanion();
                if (companion != null) {
                    companion.setWindowHeight(wizard, wizard.wizardHeight + 400);
                    companion.center(wizard);
                }

                wizard.diagramType.setRequired(true);
                wizard.diagramType.setOptionsList(Arrays.asList(ChartType.values()));
                wizard.diagramType.setValue(ChartType.SERIAL);
                wizard.diagramType.addListener(new ValueListener() {
                    @Override
                    public void valueChanged(Object source, String property, @Nullable Object prevValue, @Nullable Object value) {
                        wizard.getItem().setChartType((ChartType) value);
                        wizard.chartPreviewBox.removeAll();
                        showChart();
                    }
                });
            } else {
                wizard.chartPreviewBox.setVisible(false);
                wizard.diagramTypeLabel.setVisible(false);
                wizard.diagramType.setVisible(false);
            }
        }

        protected void initDownloadAction() {
            wizard.downloadTemplateFile.setCaption(wizard.generateTemplateFileName(wizard.templateFileFormat.getValue().toString().toLowerCase()));
            wizard.downloadTemplateFile.setAction(new AbstractAction("generateNewTemplateAndGet") {
                @Override
                public void actionPerform(Component component) {
                    byte[] newTemplate = null;
                    try {
                        wizard.getItem().setName(wizard.reportName.getValue().toString());
                        newTemplate = wizard.reportWizardService.generateTemplate(wizard.getItem(), (TemplateFileType) wizard.templateFileFormat.getValue());
                        ExportDisplay exportDisplay = AppConfig.createExportDisplay((IFrame) wizard.getComponent("saveStep"));
                        exportDisplay.show(new ByteArrayDataProvider(newTemplate),
                                wizard.downloadTemplateFile.getCaption(), ExportFormat.getByExtension(wizard.templateFileFormat.getValue().toString().toLowerCase()));
                    } catch (TemplateGenerationException e) {
                        wizard.showNotification(wizard.getMessage("templateGenerationException"), IFrame.NotificationType.WARNING);
                    }
                    if (newTemplate != null) {
                        wizard.lastGeneratedTemplate = newTemplate;
                    }
                }
            });
        }

        protected void initSaveAction() {
            wizard.saveBtn.setVisible(true);
            wizard.saveBtn.setAction(new AbstractAction("saveReport") {
                @Override
                public void actionPerform(Component component) {
                    try {
                        wizard.outputFileName.validate();
                    } catch (ValidationException e) {
                        wizard.showNotification(wizard.getMessage("validationFail.caption"), e.getMessage(), IFrame.NotificationType.TRAY);
                        return;
                    }
                    if (wizard.getItem().getReportRegions().isEmpty()) {
                        wizard.showOptionDialog(
                                wizard.getMessage("dialogs.Confirmation"),
                                wizard.getMessage("confirmSaveWithoutRegions"),
                                IFrame.MessageType.CONFIRMATION,
                                new Action[]{
                                        new DialogAction(DialogAction.Type.OK) {
                                            @Override
                                            public void actionPerform(Component component) {
                                                convertToReportAndForceCloseWizard();
                                            }
                                        },
                                        new DialogAction(DialogAction.Type.NO)
                                });

                    } else {
                        convertToReportAndForceCloseWizard();
                    }
                }

                private void convertToReportAndForceCloseWizard() {
                    Report r = wizard.buildReport(false);
                    if (r != null) {
                        wizard.close(Window.COMMIT_ACTION_ID, true); //true is ok cause it is a save btn
                    }
                }
            });
        }

        protected void showChart() {
            String chartDescriptionJson = new String(wizard.buildReport(true).getDefaultTemplate().getContent());
            AbstractChartDescription chartDescription = AbstractChartDescription.fromJsonString(chartDescriptionJson);
            RandomChartDataGenerator randomChartDataGenerator = new RandomChartDataGenerator();
            List<Map<String, Object>> randomChartData = randomChartDataGenerator.generateRandomChartData(chartDescription);
            ChartToJsonConverter chartToJsonConverter = new ChartToJsonConverter();
            String chartJson = null;
            if (chartDescription instanceof PieChartDescription) {
                chartJson = chartToJsonConverter.convertPieChart((PieChartDescription) chartDescription, randomChartData);
            } else if (chartDescription instanceof SerialChartDescription) {
                chartJson = chartToJsonConverter.convertSerialChart((SerialChartDescription) chartDescription, randomChartData);
            }

            wizard.openFrame(wizard.chartPreviewBox, ShowChartController.JSON_CHART_SCREEN_ID,
                    Collections.<String, Object>singletonMap(ShowChartController.CHART_JSON_PARAMETER, chartJson));
        }
    }

    protected class BeforeHideSaveStepFrameHandler implements BeforeHideStepFrameHandler {
        @Override
        public void beforeHideFrame() {
            wizard.saveBtn.setVisible(false);
        }
    }
}
