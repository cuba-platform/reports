/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.gui.report.wizard;

import com.google.common.collect.ImmutableMap;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.MessageTools;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.MetadataTools;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.WindowManagerProvider;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.gui.theme.ThemeConstants;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.reports.app.EntityTree;
import com.haulmont.reports.app.service.ReportWizardService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportGroup;
import com.haulmont.reports.entity.ReportOutputType;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.ReportRegion;
import com.haulmont.reports.entity.wizard.TemplateFileType;
import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.reports.gui.ReportGuiManager;
import com.haulmont.reports.gui.report.wizard.step.MainWizardFrame;
import com.haulmont.reports.gui.report.wizard.step.StepFrame;
import com.haulmont.reports.gui.report.wizard.step.StepFrameManager;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author fedorchenko
 * @version $Id$
 */
public class ReportWizardCreator extends AbstractEditor<ReportData> implements MainWizardFrame<AbstractEditor> {
    //main wizard window
    @Inject
    protected Datasource reportDataDs;
    @Inject
    protected CollectionDatasource<ReportRegion, UUID> reportRegionsDs;
    @Inject
    protected CollectionDatasource<ReportGroup, UUID> groupsDs;
    @Named("fwd")
    protected Button fwdBtn;
    @Named("regionsStep.run")
    protected Button runBtn;
    @Named("bwd")
    protected Button bwdBtn;
    @Named("save")
    protected Button saveBtn;
    @Inject
    protected Label tipLabel;
    @Inject
    protected BoxLayout editAreaVbox;
    @Inject
    protected ButtonsPanel navBtnsPanel;
    @Inject
    protected GroupBoxLayout editAreaGroupBox;

    //detail frame
    @Named("detailsStep.mainFields")
    protected FieldGroup mainFields;
    @Named("detailsStep.setQuery")
    protected Button setQueryButton;
    protected OptionsGroup reportTypeOptionGroup;//this and following are set during creation
    protected LookupField templateFileFormat;
    protected LookupField entity;
    protected TextField reportName;

    //regions frame
    @Named("regionsStep.addRegionDisabledBtn")
    protected Button addRegionDisabledBtn;
    @Named("regionsStep.addTabulatedRegionDisabledBtn")
    protected Button addTabulatedRegionDisabledBtn;
    @Named("regionsStep.addSimpleRegionBtn")
    protected Button addSimpleRegionBtn;
    @Named("regionsStep.addTabulatedRegionBtn")
    protected Button addTabulatedRegionBtn;
    @Named("regionsStep.addRegionPopupBtn")
    protected PopupButton addRegionPopupBtn;
    @Named("regionsStep.moveUpBtn")
    protected Button moveUpBtn;
    @Named("regionsStep.moveDownBtn")
    protected Button moveDownBtn;
    @Named("regionsStep.removeBtn")
    protected Button removeBtn;
    @Named("regionsStep.regionsTable")
    protected Table regionsTable;
    @Named("regionsStep.buttonsBox")
    protected BoxLayout buttonsBox;

    //save frame
    @Named("saveStep.outputFileFormat")
    protected LookupField outputFileFormat;
    @Named("saveStep.outputFileName")
    protected TextField outputFileName;
    @Named("saveStep.downloadTemplateFile")
    protected Button downloadTemplateFile;
    @Named("saveStep.diagramTypeLabel")
    protected Label diagramTypeLabel;
    @Named("saveStep.diagramType")
    protected LookupField diagramType;
    @Named("saveStep.chartPreviewBox")
    protected BoxLayout chartPreviewBox;

    //services
    @Inject
    protected Metadata metadata;
    @Inject
    protected MetadataTools metadataTools;
    @Inject
    protected MessageTools messageTools;
    @Inject
    protected ComponentsFactory componentsFactory;
    @Inject
    protected ReportWizardService reportWizardService;
    @Inject
    protected WindowManagerProvider windowManagerProvider;
    @Inject
    protected WindowConfig windowConfig;
    @Inject
    protected ThemeConstants themeConstants;

    //other
    protected StepFrame detailsStepFrame;
    protected StepFrame regionsStepFrame;
    protected StepFrame saveStepFrame;
    protected StepFrameManager stepFrameManager;
    protected ReportGuiManager reportGuiManager = AppBeans.get(ReportGuiManager.class);

    protected byte[] lastGeneratedTemplate;
    protected Report lastGeneratedTmpReport;
    protected boolean entityTreeHasSimpleAttrs;
    protected boolean entityTreeHasCollections;
    protected boolean needUpdateEntityModel = false;

    protected String query;
    protected List<ReportData.Parameter> queryParameters;
    protected int wizardWidth;
    protected int wizardHeight;

    public interface Companion {
        void setWindowHeight(Window window, int height);

        void center(Window window);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init(Map<String, Object> params) {
        super.init(params);

        wizardWidth = themeConstants.getInt("cuba.gui.report.ReportWizard.width");
        wizardHeight = themeConstants.getInt("cuba.gui.report.ReportWizard.height");
        getDialogParams()
                .setWidth(wizardWidth)
                .setHeight(wizardHeight);

        stepFrameManager = new StepFrameManager(this, getStepFrames());

        initAvailableFormats();
        initMainButtons();
        initMainFields();

        stepFrameManager.showCurrentFrame();
        tipLabel.setValue(getMessage("enterMainParameters"));
        reportRegionsDs.addListener(new CollectionDsListenerAdapter<ReportRegion>() {
            @Override
            public void collectionChanged(CollectionDatasource ds, Operation operation, List<ReportRegion> items) {
                super.collectionChanged(ds, operation, items);
                if (Operation.ADD.equals(operation)) {
                    regionsTable.setSelected((Collection) items);
                }
            }

            @Override
            public void itemChanged(Datasource<ReportRegion> ds, @Nullable ReportRegion prevItem, @Nullable ReportRegion item) {
                super.itemChanged(ds, prevItem, item);
                if (regionsTable.getSingleSelected() != null) {
                    moveDownBtn.setEnabled(true);
                    moveUpBtn.setEnabled(true);
                    removeBtn.setEnabled(true);
                }
            }
        });
    }

    protected void initMainButtons() {
        fwdBtn.setAction(new AbstractAction("fwd") {
            @Override
            public void actionPerform(Component component) {
                if (entity.getValue() == null) {
                    showNotification(getMessage("fillEntityMsg"), NotificationType.TRAY_HTML);
                    return;
                }

                if (needUpdateEntityModel) {
                    EntityTree entityTree = reportWizardService.buildEntityTree((MetaClass) entity.getValue());
                    entityTreeHasSimpleAttrs = entityTree.getEntityTreeStructureInfo().isEntityTreeHasSimpleAttrs();
                    entityTreeHasCollections = entityTree.getEntityTreeStructureInfo().isEntityTreeRootHasCollections();
                    entityTree.getEntityTreeRootNode().getLocalizedName();
                    getItem().setEntityTreeRootNode(entityTree.getEntityTreeRootNode());
                    needUpdateEntityModel = false;
                }
                stepFrameManager.nextFrame();
                refreshFrameVisible();
            }
        });
        bwdBtn.setAction(new AbstractAction("bwd") {
            @Override
            public void actionPerform(Component component) {
                stepFrameManager.prevFrame();
                refreshFrameVisible();
            }
        });
    }

    protected void initMainFields() {
        mainFields.addCustomField("entity", new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                LookupField lookupField = componentsFactory.createComponent(LookupField.NAME);
                lookupField.requestFocus();
                entity = lookupField;
                return lookupField;
            }
        });
        mainFields.addCustomField("reportName", new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                TextField textField = componentsFactory.createComponent(TextField.NAME);
                reportName = textField;
                return textField;
            }
        });
        mainFields.addCustomField("templateFileFormat", new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                LookupField lookupField = componentsFactory.createComponent(LookupField.NAME);
                templateFileFormat = lookupField;
                return lookupField;
            }
        });
        mainFields.addCustomField("reportType", new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                OptionsGroup optionsGroup = AppConfig.getFactory().createComponent(OptionsGroup.NAME);
                optionsGroup.setMultiSelect(false);
                optionsGroup.setOrientation(OptionsGroup.Orientation.VERTICAL);
                reportTypeOptionGroup = optionsGroup;
                return optionsGroup;
            }
        });
    }

    protected void refreshFrameVisible() {
        if (detailsStepFrame.getFrame().isVisible()) {
            tipLabel.setValue(getMessage("enterMainParameters"));
            editAreaVbox.add(editAreaGroupBox);
            editAreaVbox.remove(regionsStepFrame.getFrame());
            editAreaGroupBox.remove(saveStepFrame.getFrame());
            editAreaGroupBox.add(detailsStepFrame.getFrame());
        } else if (regionsStepFrame.getFrame().isVisible()) {
            tipLabel.setValue(getMessage("addPropertiesAndTableAreas"));
            editAreaVbox.remove(editAreaGroupBox);
            editAreaVbox.add(regionsStepFrame.getFrame());
        } else if (saveStepFrame.getFrame().isVisible()) {
            tipLabel.setValue(getMessage("finishPrepareReport"));
            editAreaVbox.add(editAreaGroupBox);
            editAreaVbox.remove(regionsStepFrame.getFrame());
            editAreaGroupBox.add(saveStepFrame.getFrame());
            editAreaGroupBox.remove(detailsStepFrame.getFrame());
        }
    }

    protected List<StepFrame> getStepFrames() {
        detailsStepFrame = new DetailsStepFrame(this);
        regionsStepFrame = new RegionsStepFrame(this);
        saveStepFrame = new SaveStepFrame(this);
        return Arrays.asList(detailsStepFrame, regionsStepFrame, saveStepFrame);
    }

    protected String generateTemplateFileName(String fileExtension) {
        if (entity.getValue() == null) {
            return "";
        }
        return formatMessage("downloadTemplateFileNamePattern", messageTools.getEntityCaption((MetaClass) entity.getValue()), fileExtension);
    }

    protected String generateOutputFileName(String fileExtension) {
        if (entity.getValue() == null) {
            return "";
        }
        return formatMessage("downloadOutputFileNamePattern", messageTools.getEntityCaption((MetaClass) entity.getValue()), fileExtension);
    }


    @Override
    public Button getForwardBtn() {
        return fwdBtn;
    }

    @Override
    public void removeBtns() {
        navBtnsPanel.remove(fwdBtn);
        navBtnsPanel.remove(bwdBtn);
        navBtnsPanel.remove(saveBtn);
    }

    @Override
    public void addForwardBtn() {
        navBtnsPanel.add(fwdBtn);
    }

    @Override
    public void addBackwardBtn() {
        navBtnsPanel.add(bwdBtn);
    }

    @Override
    public void addSaveBtn() {
        navBtnsPanel.add(saveBtn);
    }

    @Override
    public Button getBackwardBtn() {
        return bwdBtn;
    }

    @Override
    public AbstractEditor getMainEditorFrame() {
        return this;
    }

    protected void setupButtonsVisibility() {
        buttonsBox.remove(addRegionDisabledBtn);
        buttonsBox.remove(addTabulatedRegionDisabledBtn);
        buttonsBox.remove(addSimpleRegionBtn);
        buttonsBox.remove(addTabulatedRegionBtn);
        buttonsBox.remove(addRegionPopupBtn);
        if (((ReportData.ReportType) reportTypeOptionGroup.getValue()).isList()) {
            tipLabel.setValue(formatMessage("regionTabulatedMessage",
                    messages.getMessage(((MetaClass) entity.getValue()).getJavaClass(),
                            ((MetaClass) entity.getValue()).getJavaClass().getSimpleName())
            ));
            if (entityTreeHasSimpleAttrs && getItem().getReportRegions().isEmpty()) {
                buttonsBox.add(addTabulatedRegionBtn);
            } else {
                buttonsBox.add(addTabulatedRegionDisabledBtn);
            }
        } else {
            tipLabel.setValue(getMessage("addPropertiesAndTableAreas"));
            if (entityTreeHasSimpleAttrs && entityTreeHasCollections) {
                buttonsBox.add(addRegionPopupBtn);
            } else if (entityTreeHasSimpleAttrs) {
                buttonsBox.add(addSimpleRegionBtn);
            } else if (entityTreeHasCollections) {
                buttonsBox.add(addTabulatedRegionBtn);
            } else {
                buttonsBox.add(addRegionDisabledBtn);
            }
        }

        if (regionsTable.getSingleSelected() != null) {
            moveDownBtn.setEnabled(true);
            moveUpBtn.setEnabled(true);
            removeBtn.setEnabled(true);
        } else {
            moveDownBtn.setEnabled(false);
            moveUpBtn.setEnabled(false);
            removeBtn.setEnabled(false);
        }
    }

    protected Report buildReport(boolean temporary) {
        ReportData reportData = getItem();
        reportData.setName((String) reportName.getValue());
        reportData.setTemplateFileName(generateTemplateFileName(templateFileFormat.getValue().toString().toLowerCase()));
        if (outputFileFormat.getValue() == null) {
            reportData.setOutputFileType(ReportOutputType.fromId(((TemplateFileType) templateFileFormat.getValue()).getId()));
        } else {
            //lets generate output report in same format as the template
            reportData.setOutputFileType((ReportOutputType) outputFileFormat.getValue());
        }
        reportData.setReportType((ReportData.ReportType) reportTypeOptionGroup.getValue());
        groupsDs.refresh();
        if (groupsDs.getItemIds() != null) {
            UUID id = groupsDs.getItemIds().iterator().next();
            reportData.setGroup(groupsDs.getItem(id));
        }

        //be sure that reportData.name and reportData.outputFileFormat is not null before generation of template
        byte[] templateByteArray;
        try {
            templateByteArray = reportWizardService.generateTemplate(reportData, (TemplateFileType) templateFileFormat.getValue());
            reportData.setTemplateContent(templateByteArray);
        } catch (TemplateGenerationException e) {
            showNotification(getMessage("templateGenerationException"), NotificationType.WARNING);
            return null;
        }
        reportData.setTemplateFileType((TemplateFileType) templateFileFormat.getValue());
        reportData.setOutputNamePattern(outputFileName.<String>getValue());

        if (query != null) {
            reportData.setQuery(query);
            reportData.setQueryParameters(queryParameters);
        }

        Report report = reportWizardService.toReport(reportData, temporary);
        reportData.setGeneratedReport(report);
        return report;
    }

    /**
     * Dead code. Must to be tested after platform fixes in com.haulmont.cuba.web.WebWindowManager
     * Web modal editor window always closed forced, therefore that preClose method is not called
     * <p/>
     * Confirm closing without save if regions are created
     */
    @Override
    public boolean preClose(String actionId) {
        if (!COMMIT_ACTION_ID.equals(actionId) && reportRegionsDs.getItems() != null) {
            showOptionDialog(getMessage("dialogs.Confirmation"), getMessage("interruptConfirm"), MessageType.CONFIRMATION, new Action[]{
                    new DialogAction(DialogAction.Type.YES) {
                        @Override
                        public void actionPerform(Component component) {
                            ReportWizardCreator.this.close(CLOSE_ACTION_ID, true);
                        }
                    },
                    new DialogAction(DialogAction.Type.NO) {

                    }
            });
        }
        return false;
    }

    @Override
    public String getMessage(String key) {
        return super.getMessage(key);
    }

    @Override
    public String formatMessage(String key, Object... params) {
        return super.formatMessage(key, params);
    }


    protected void setCorrectReportOutputType() {
        ReportOutputType outputFileFormatPrevValue = outputFileFormat.getValue();
        outputFileFormat.setValue(null);
        Map<String, Object> optionsMap = refreshOutputAvailableFormats((TemplateFileType) templateFileFormat.getValue());
        outputFileFormat.setOptionsMap(optionsMap);

        if (outputFileFormatPrevValue != null) {
            if (optionsMap.containsKey(outputFileFormatPrevValue.toString().toLowerCase())) {
                outputFileFormat.setValue(outputFileFormatPrevValue);
            }
        }
        if (outputFileFormat.getValue() == null) {
            if (optionsMap.size() > 1) {
                outputFileFormat.setValue(optionsMap.get(templateFileFormat.getValue().toString().toLowerCase()));
            } else if (optionsMap.size() == 1) {
                outputFileFormat.setValue(optionsMap.values().iterator().next());
            }
        }
    }

    protected Map<String, Object> refreshOutputAvailableFormats(TemplateFileType templateFileType) {
        return availableOutputFormats.get(templateFileType);
    }

    protected Map<TemplateFileType, Map<String, Object>> availableOutputFormats;

    private void initAvailableFormats() {
        availableOutputFormats = new ImmutableMap.Builder<TemplateFileType, Map<String, Object>>()
                .put(TemplateFileType.DOCX, new ImmutableMap.Builder<String, Object>()
                        .put(ReportOutputType.DOCX.toString().toLowerCase(), ReportOutputType.DOCX)
                        .put(ReportOutputType.HTML.toString().toLowerCase(), ReportOutputType.HTML)
                        .put(ReportOutputType.PDF.toString().toLowerCase(), ReportOutputType.PDF)
                        .build())
                .put(TemplateFileType.XLSX, new ImmutableMap.Builder<String, Object>()
                        .put(ReportOutputType.XLSX.toString().toLowerCase(), ReportOutputType.XLSX)
                        .put(ReportOutputType.PDF.toString().toLowerCase(), ReportOutputType.PDF)
                        .build())
                .put(TemplateFileType.HTML, new ImmutableMap.Builder<String, Object>()
                        .put(ReportOutputType.HTML.toString().toLowerCase(), ReportOutputType.HTML)
                        .put(ReportOutputType.PDF.toString().toLowerCase(), ReportOutputType.PDF)
                        .build())
                .put(TemplateFileType.CHART, new ImmutableMap.Builder<String, Object>()
                        .put(messages.getMessage(ReportOutputType.CHART), ReportOutputType.CHART)
                        .build())
                .build();
    }
}