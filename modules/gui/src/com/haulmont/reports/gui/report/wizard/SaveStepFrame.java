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

package com.haulmont.reports.gui.report.wizard;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.DialogAction.Type;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportOutputType;
import com.haulmont.reports.entity.charts.*;
import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.reports.gui.report.run.ShowChartController;
import com.haulmont.reports.gui.report.wizard.step.StepFrame;
import com.haulmont.reports.gui.template.edit.generator.RandomChartDataGenerator;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SaveStepFrame extends StepFrame {
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

            if (StringUtils.isEmpty(wizard.outputFileName.getValue())) {
                Object value = wizard.templateFileFormat.getValue();
                wizard.outputFileName.setValue(wizard.generateOutputFileName(value.toString().toLowerCase()));
            }
            wizard.setCorrectReportOutputType();

            initChartPreview();
        }

        protected void initChartPreview() {
            if (wizard.outputFileFormat.getValue() == ReportOutputType.CHART) {
                wizard.chartPreviewBox.setVisible(true);
                wizard.diagramType.setVisible(true);

                showChart();

                wizard.getWindow().setHeightAuto();
                wizard.getDialogOptions().center();

                wizard.diagramType.setRequired(true);
                wizard.diagramType.setOptionsList(Arrays.asList(ChartType.values()));
                wizard.diagramType.setValue(ChartType.SERIAL);

                wizard.diagramType.addValueChangeListener(e -> {
                    wizard.getItem().setChartType((ChartType) e.getValue());
                    wizard.chartPreviewBox.removeAll();
                    showChart();
                });
            } else {
                wizard.chartPreviewBox.setVisible(false);
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
                        newTemplate = wizard.reportWizardService.generateTemplate(wizard.getItem(), wizard.templateFileFormat.getValue());
                        ExportDisplay exportDisplay = AppConfig.createExportDisplay((Frame) wizard.getComponent("saveStep"));
                        exportDisplay.show(new ByteArrayDataProvider(newTemplate),
                                wizard.downloadTemplateFile.getCaption(), ExportFormat.getByExtension(wizard.templateFileFormat.getValue().toString().toLowerCase()));
                    } catch (TemplateGenerationException e) {
                        wizard.showNotification(wizard.getMessage("templateGenerationException"), Frame.NotificationType.WARNING);
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
                        wizard.showNotification(wizard.getMessage("validationFail.caption"), e.getMessage(), Frame.NotificationType.TRAY);
                        return;
                    }
                    if (wizard.getItem().getReportRegions().isEmpty()) {
                        wizard.showOptionDialog(
                                wizard.getMessage("dialogs.Confirmation"),
                                wizard.getMessage("confirmSaveWithoutRegions"),
                                Frame.MessageType.CONFIRMATION,
                                new Action[]{
                                        new DialogAction(Type.OK) {
                                            @Override
                                            public void actionPerform(Component component) {
                                                convertToReportAndForceCloseWizard();
                                            }
                                        },
                                        new DialogAction(Type.NO, Status.PRIMARY)
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
            byte[] content = wizard.buildReport(true).getDefaultTemplate().getContent();
            String chartDescriptionJson = new String(content, StandardCharsets.UTF_8);
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
                    ParamsMap.of(ShowChartController.CHART_JSON_PARAMETER, chartJson));
        }
    }

    protected class BeforeHideSaveStepFrameHandler implements BeforeHideStepFrameHandler {
        @Override
        public void beforeHideFrame() {
            wizard.saveBtn.setVisible(false);
        }
    }
}
