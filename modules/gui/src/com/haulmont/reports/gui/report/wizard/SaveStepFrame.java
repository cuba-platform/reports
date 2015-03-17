/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.report.wizard;

import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.wizard.TemplateFileType;
import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.reports.gui.report.wizard.step.StepFrame;
import org.apache.commons.lang.StringUtils;

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
            if (StringUtils.isEmpty(wizard.outputFileName.<String>getValue()))
                wizard.outputFileName.setValue(wizard.generateOutputFileName(wizard.templateFileFormat.getValue().toString().toLowerCase()));
            wizard.setCorrectReportOutputType();

        }

    }

    protected class BeforeHideSaveStepFrameHandler implements BeforeHideStepFrameHandler {
        @Override
        public void beforeHideFrame() {
            wizard.saveBtn.setVisible(false);
        }
    }
}
