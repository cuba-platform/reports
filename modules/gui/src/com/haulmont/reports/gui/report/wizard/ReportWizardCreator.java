/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.gui.report.wizard;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.MessageTools;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.MetadataTools;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.ValueChangingListener;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.reports.app.EntityTree;
import com.haulmont.reports.app.service.ReportWizardService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportGroup;
import com.haulmont.reports.entity.ReportOutputType;
import com.haulmont.reports.entity.wizard.*;
import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.reports.gui.ReportGuiManager;
import com.haulmont.reports.gui.components.actions.OrderableItemMoveAction;
import com.haulmont.reports.gui.report.wizard.step.MainWizardFrame;
import com.haulmont.reports.gui.report.wizard.step.StepFrame;
import com.haulmont.reports.gui.report.wizard.step.StepFrameManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author fedorchenko
 * @version $Id$
 */
public class ReportWizardCreator extends AbstractEditor<ReportData> implements MainWizardFrame<AbstractEditor> {

    //injected UI and main form descriptor fields:
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
    //injected step UI frames fields:
    //detail frame
//    @Named("detailsStep.isListed")
    protected OptionsGroup isListedReport;
    //    @Named("detailsStep.templateFileFormat")
    protected LookupField templateFileFormat;
    //    @Named("detailsStep.entity")
    protected LookupField entity;
    //    @Named("detailsStep.reportName")
    protected TextField reportName;

    @Named("detailsStep.mainFields")
    protected FieldGroup mainFields;

//    @Named("detailsStep.mainFields.templateFileFormat")
//    protected LookupField templateFileFormat;
//    @Named("detailsStep.entity")
//    protected LookupField entity;
//    @Named("detailsStep.reportName")
//    protected TextField reportName;


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
    @Inject
    protected Label tipLabel;
    @Inject
    protected BoxLayout editAreaVbox;
    @Inject
    protected ButtonsPanel navBtnsPanel;
    @Inject
    protected GroupBoxLayout editAreaGroupBox;
    //injected service fields:
    @Inject
    protected Metadata metadata;
    @Inject
    protected MetadataTools metadataTools;
    @Inject
    protected MessageTools messageTools;
    @Inject
    protected UserSession userSession;
    @Inject
    protected ComponentsFactory componentsFactory;
    @Inject
    protected ReportWizardService reportWizardService;
    //non-injected fields:
    protected StepFrame detailsStepFrame;
    protected StepFrame regionsStepFrame;
    protected StepFrame saveStepFrame;
    protected StepFrameManager stepFrameManager;
    protected ReportGuiManager reportGuiManager = AppBeans.get(ReportGuiManager.class);
    protected byte[] lastGeneratedTemplate;
    protected Report lastGeneratedTmpReport;
    protected boolean entityTreeHasSimpleAttrs;
    protected boolean entityTreeHasCollections;
    protected int windowWidth = 800;
    protected boolean needUpdateEntityModel = false;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        getWindowManager().getDialogParams().setWidth(windowWidth);
        stepFrameManager = new StepFrameManager(this, getStepFrames());

        fwdBtn.setAction(new AbstractAction("fwd") {
            @Override
            public void actionPerform(Component component) {
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
        FieldGroup.FieldConfig f = mainFields.getField("entity");
        mainFields.addCustomField(f, new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                final LookupField lookupField = AppConfig.getFactory().createComponent(LookupField.NAME);
                entity = lookupField;
                return lookupField;
            }
        });
        f = mainFields.getField("reportName");
        mainFields.addCustomField(f, new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                final TextField textField = AppConfig.getFactory().createComponent(TextField.NAME);
                reportName = textField;
                return textField;
            }
        });
        f = mainFields.getField("templateFileFormat");
        mainFields.addCustomField(f, new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                final LookupField lookupField = AppConfig.getFactory().createComponent(LookupField.NAME);
                templateFileFormat = lookupField;
                return lookupField;
            }
        });
        f = mainFields.getField("isListed");
        mainFields.addCustomField(f, new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                final OptionsGroup optionsGroup = AppConfig.getFactory().createComponent(OptionsGroup.NAME);
                optionsGroup.setMultiSelect(false);
                optionsGroup.setOrientation(OptionsGroup.Orientation.VERTICAL);
                isListedReport = optionsGroup;
                return optionsGroup;
            }
        });
        stepFrameManager.showCurrentFrame();
        tipLabel.setValue(getMessage("enterMainParameters"));
        reportRegionsDs.addListener(new CollectionDsListenerAdapter<ReportRegion>() {
            @Override
            public void collectionChanged(CollectionDatasource ds, Operation operation, List<ReportRegion> items) {
                super.collectionChanged(ds, operation, items);
                if (Operation.ADD.equals(operation)) regionsTable.setSelected((List) items);
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
        List<StepFrame> result = new ArrayList<>();
        createDetailsStepFrame();
        createRegionsStepFrame();
        createSaveStepFrame();
        result.add(detailsStepFrame);
        result.add(regionsStepFrame);
        result.add(saveStepFrame);
        return result;
    }

    protected void createDetailsStepFrame() {
        detailsStepFrame = new DetailsStepFrame();
    }

    protected void createRegionsStepFrame() {
        regionsStepFrame = new RegionsStepFrame();
    }

    protected void createSaveStepFrame() {
        saveStepFrame = new SaveStepFrame();
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

    protected void showOrHideAddRegionBtns() {
        buttonsBox.remove(addRegionDisabledBtn);
        buttonsBox.remove(addTabulatedRegionDisabledBtn);
        buttonsBox.remove(addSimpleRegionBtn);
        buttonsBox.remove(addTabulatedRegionBtn);
        buttonsBox.remove(addRegionPopupBtn);
        if (BooleanUtils.isTrue((Boolean) isListedReport.getValue())) {
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
            } else if (entityTreeHasSimpleAttrs && !entityTreeHasCollections) {
                buttonsBox.add(addSimpleRegionBtn);
            } else if (!entityTreeHasSimpleAttrs && entityTreeHasCollections) {
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

    protected Report toReport(boolean tmp) {
        byte[] templateByteArray;
        ReportData reportData = getItem();
        reportData.setName((String) reportName.getValue());
        reportData.setTemplateFileName(generateTemplateFileName(templateFileFormat.getValue().toString().toLowerCase()));
        if (outputFileFormat.getValue() == null) {
            reportData.setOutputFileType(ReportOutputType.fromId(((TemplateFileType) templateFileFormat.getValue()).getId()));
        } else {
            //lets generate output report in same format as the template
            reportData.setOutputFileType((ReportOutputType) outputFileFormat.getValue());
        }
        reportData.setIsTabulatedReport((Boolean) isListedReport.getValue());
        groupsDs.refresh();
        if (groupsDs.getItemIds() != null) {
            UUID id = groupsDs.getItemIds().iterator().next();
            reportData.setGroup(groupsDs.getItem(id));
        }
        //be sure that reportData.name and reportData.outputFileFormat is not null before generation of template
        try {
            templateByteArray = reportWizardService.generateTemplate(reportData, (TemplateFileType) templateFileFormat.getValue());
        } catch (TemplateGenerationException e) {
            showNotification(getMessage("templateGenerationException"), NotificationType.WARNING);
            return null;
        }
        reportData.setOutputNamePattern(outputFileName.<String>getValue());

        Report report = reportWizardService.toReport(reportData, templateByteArray, tmp, (TemplateFileType) templateFileFormat.getValue());
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
    /*
    protected class TreeModelBuilder {

        public String[] IGNORED_ENTITIES_PREFIXES = new String[]{"sys$", "sec$"};

        protected int entityTreeModelMaxDeep = 3;

        public int getEntityTreeModelMaxDeep() {
            return entityTreeModelMaxDeep;
        }

        public EntityTreeNode buildNewEntityTreeModelAndGetRoot(MetaClass metaClass) {
            entityTreeHasSimpleAttrs = false;
            entityTreeRootHasCollections = false;
            EntityTreeNode root = metadata.create(EntityTreeNode.class);
            root.setName(metaClass.getName());
            root.setLocalizedName(messageTools.getEntityCaption(metaClass));
            root.setWrappedMetaClass(metaClass);
            fillChildNodes(root, 1, new HashSet<String>());
            return root;
        }

        protected EntityTreeNode fillChildNodes(final EntityTreeNode parentEntityTreeNode, int depth, final Set<String> alreadyAddedMetaProps) {
            if (depth > getEntityTreeModelMaxDeep()) {
                return parentEntityTreeNode;
            }
            for (com.haulmont.chile.core.model.MetaProperty metaProperty : parentEntityTreeNode.getWrappedMetaClass().getProperties()) {
                if (!reportWizardService.isPropertyAllowedForReportWizard(parentEntityTreeNode.getWrappedMetaClass(), metaProperty)) {
                    continue;
                }
                if (metaProperty.getRange().isClass()) {
                    MetaClass metaClass = metaProperty.getRange().asClass();
                    MetaClass effectiveMetaClass = metadata.getExtendedEntities().getEffectiveMetaClass(metaClass);
                    //does we need to do security checks here? no
                    if (!StringUtils.startsWithAny(effectiveMetaClass.getName(), IGNORED_ENTITIES_PREFIXES)) {
                        int newDepth = depth + 1;
                        EntityTreeNode newParentModelNode = metadata.create(EntityTreeNode.class);
                        newParentModelNode.setName(metaProperty.getName());
                        //newParentModelNode.setLocalizedName(messageTools.getEntityCaption(effectiveMetaClass));
                        newParentModelNode.setLocalizedName(messageTools.getPropertyCaption(parentEntityTreeNode.getWrappedMetaClass(), metaProperty.getName()));
                        newParentModelNode.setWrappedMetaClass(effectiveMetaClass);
                        newParentModelNode.setWrappedMetaProperty(metaProperty);
                        newParentModelNode.setParent(parentEntityTreeNode);


                        if (alreadyAddedMetaProps.contains(getTreeNodeInfo(parentEntityTreeNode) + "|" + getTreeNodeInfo(newParentModelNode))) {
                            continue; //avoid parent-child-parent-... infinite loops
                        }
                        //alreadyAddedMetaProps.add(getTreeNodeInfo(parentEntityTreeNode) + "|" + getTreeNodeInfo(newParentModelNode));
                        alreadyAddedMetaProps.add(getTreeNodeInfo(newParentModelNode) + "|" + getTreeNodeInfo(parentEntityTreeNode));

                        //System.err.println(StringUtils.leftPad("", newDepth * 2, " ") + getTreeNodeInfo(parentEntityTreeNode) + "     |     " + getTreeNodeInfo(newParentModelNode));
                        //System.err.println(StringUtils.leftPad("", newDepth * 2, " ") + getTreeNodeInfo(newParentModelNode) + "     |     " + getTreeNodeInfo(parentEntityTreeNode));
                        //System.err.println("");

                        if (!entityTreeRootHasCollections && metaProperty.getRange().getCardinality().isMany() && newDepth < getEntityTreeModelMaxDeep()) {
                            entityTreeRootHasCollections = true;//TODO set to true if only simple attributes of that collection as children exists
                            entityTreeRootHasCollections = true;//TODO set to true if only simple attributes of that collection as children exists
                        }
                        fillChildNodes(newParentModelNode, newDepth, alreadyAddedMetaProps);

                        parentEntityTreeNode.getChildren().add(newParentModelNode);
                    }
                } else {
                    if (!entityTreeHasSimpleAttrs) {
                        entityTreeHasSimpleAttrs = true;
                    }
                    EntityTreeNode child = metadata.create(EntityTreeNode.class);
                    child.setName(metaProperty.getName());
                    child.setLocalizedName(messageTools.getPropertyCaption(metaProperty));
                    child.setWrappedMetaProperty(metaProperty);
                    child.setParent(parentEntityTreeNode);
                    parentEntityTreeNode.getChildren().add(child);

                }

            }
            return parentEntityTreeNode;
        }

        private String getTreeNodeInfo(EntityTreeNode parentEntityTreeNode) {
            if (parentEntityTreeNode.getWrappedMetaProperty() != null) {
                return parentEntityTreeNode.getWrappedMetaClass().getName() + " isMany:" + parentEntityTreeNode.getWrappedMetaProperty().getRange().getCardinality().isMany();
            } else {
                return parentEntityTreeNode.getWrappedMetaClass().getName() + " isMany:false";
            }
        }
    }    */

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
        outputFileFormat.setOptionsMap(
                refreshOutputAvailableFormats(
                        (TemplateFileType) templateFileFormat.getValue())
        );


        if (outputFileFormatPrevValue != null) {
            if (outputFileFormat.getOptionsMap().containsKey(outputFileFormatPrevValue.toString().toLowerCase())) {
                outputFileFormat.setValue(outputFileFormatPrevValue);
            }
        }
        if (outputFileFormat.getValue() == null) {
            outputFileFormat.setValue(outputFileFormat.getOptionsMap().get(templateFileFormat.getValue().toString().toLowerCase()));
        }
    }

    protected Map<String, Object> refreshOutputAvailableFormats(TemplateFileType templateFileType) {

        Map<String, Object> result = new LinkedHashMap<>();
        switch (templateFileType) {
            case DOCX:
                result.put(ReportOutputType.DOCX.toString().toLowerCase(), ReportOutputType.DOCX);
                result.put(ReportOutputType.HTML.toString().toLowerCase(), ReportOutputType.HTML);
                result.put(ReportOutputType.PDF.toString().toLowerCase(), ReportOutputType.PDF);
                break;
            case XLSX:
                result.put(ReportOutputType.XLSX.toString().toLowerCase(), ReportOutputType.XLSX);
                result.put(ReportOutputType.PDF.toString().toLowerCase(), ReportOutputType.PDF);
                break;
            case HTML:
                result.put(ReportOutputType.HTML.toString().toLowerCase(), ReportOutputType.HTML);
                result.put(ReportOutputType.PDF.toString().toLowerCase(), ReportOutputType.PDF);
                break;
        }
        return result;
    }

    protected class DetailsStepFrame extends StepFrame {
        public DetailsStepFrame() {
            super(ReportWizardCreator.this, getMessage("reportDetails"), "detailsStep");
            isFirst = true;
            initFrameHandler = new InitDetailsStepFrameHandler();
        }

        protected class InitDetailsStepFrameHandler implements InitStepFrameHandler {
            @Override
            public void initFrame() {

                isListedReport.setOptionsMap(getListedReportOptionsMap());
                isListedReport.setValue(Boolean.FALSE);
                isListedReport.setValueChangingListener(new ValueChangingListener() {
                    @Nullable
                    @Override
                    public Object valueChanging(Object source, String property, @Nullable final Object prevValue, @Nullable final Object value) {
                        if (!getItem().getReportRegions().isEmpty()) {
                            showOptionDialog(getMessage("dialogs.Confirmation"), getMessage("regionsClearConfirm"), MessageType.CONFIRMATION, new AbstractAction[]{
                                    new DialogAction(DialogAction.Type.YES) {
                                        @Override
                                        public void actionPerform(Component component) {
                                            getItem().getReportRegions().clear();
                                            regionsTable.refresh(); //for web6
                                            //((RegionsStepFrame) regionsStepFrame).isAddRegionActionPerformed = false;
                                            isListedReport.setValue(value);
                                        }
                                    },
                                    new DialogAction(DialogAction.Type.NO) {
                                    }

                            });

                            return prevValue;
                        } else {
                            return value;
                        }
                    }
                });
                templateFileFormat.setOptionsMap(getTemplateAvailableFormats());
                templateFileFormat.setValue(templateFileFormat.getOptionsMap().
                        get(ReportOutputType.fromId(40).toString().toLowerCase()));//select doc as default value


                entity.setOptionsMap(getForReportAvailableEntities());

                entity.setValueChangingListener(new ValueChangingListener() {
                    @Nullable
                    @Override
                    public Object valueChanging(Object source, String property, @Nullable final Object prevValue, @Nullable final Object value) {
                        if (!getItem().getReportRegions().isEmpty()) {
                            showOptionDialog(getMessage("dialogs.Confirmation"), getMessage("regionsClearConfirm"), MessageType.CONFIRMATION, new AbstractAction[]{
                                    new DialogAction(DialogAction.Type.YES) {
                                        @Override
                                        public void actionPerform(Component component) {
                                            getItem().getReportRegions().clear();
                                            regionsTable.refresh(); //for web6
                                            needUpdateEntityModel = true;
                                            entity.setValue(value);
                                        }
                                    },
                                    new DialogAction(DialogAction.Type.NO) {
                                    }

                            });

                            return prevValue;
                        } else {
                            needUpdateEntityModel = true;
                            return value;
                        }
                    }
                });

                entity.addListener(new ValueListener() {
                    @Override
                    public void valueChanged(Object source, String property, @Nullable Object prevValue, @Nullable Object value) {
                        setGeneratedReportName((MetaClass) prevValue, (MetaClass) value);
                    }

                    private void setGeneratedReportName(MetaClass prevValue, MetaClass value) {
                        String oldReportName = reportName.getValue();
                        if (StringUtils.isBlank(oldReportName)) {
                            String newText = formatMessage("reportNamePattern", messageTools.getEntityCaption(value));
                            reportName.setValue(newText);
                        } else {
                            //if old text contains MetaClass name substring, just replace it
                            if (prevValue != null && StringUtils.contains(oldReportName, messageTools.getEntityCaption(prevValue))) {
                                String newText = StringUtils.replace(oldReportName, messageTools.getEntityCaption(prevValue), messageTools.getEntityCaption(value));
                                reportName.setValue(newText);
                                if (!oldReportName.equals(formatMessage("reportNamePattern", messageTools.getEntityCaption(prevValue)))) {
                                    //if user changed auto generated report name and we have changed it, we show message to him
                                    showNotification(getMessage("reportNameChanged"), NotificationType.TRAY);
                                }
                            } else {
                                //do nothing. Do not change non-empty report name without entity simple name inside it
                            }
                        }
                    }
                });
            }

            protected Map<String, Object> getListedReportOptionsMap() {
                Map<String, Object> result = new LinkedHashMap<>(2);
                result.put(getMessage("isNotListed"), Boolean.FALSE);
                result.put(getMessage("isListed"), Boolean.TRUE);
                return result;
            }

            protected Map<String, Object> getTemplateAvailableFormats() {
                Map<String, Object> result = new LinkedHashMap<>(3);
                result.put(TemplateFileType.fromId(50).toString().toLowerCase(), TemplateFileType.fromId(50));
                result.put(TemplateFileType.fromId(40).toString().toLowerCase(), TemplateFileType.fromId(40));
                result.put(TemplateFileType.fromId(30).toString().toLowerCase(), TemplateFileType.fromId(30));
                return result;
            }

            protected Map<String, Object> getForReportAvailableEntities() {
                Map<String, Object> result = new TreeMap<>(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
                    }
                });
                Collection<MetaClass> classes = metadataTools.getAllPersistentMetaClasses();
                for (MetaClass metaClass : classes) {
                    MetaClass effectiveMetaClass = metadata.getExtendedEntities().getEffectiveMetaClass(metaClass);
                    if (!reportWizardService.isEntityAllowedForReportWizard(effectiveMetaClass)) {
                        continue;
                    }
                    result.put(messageTools.getEntityCaption(effectiveMetaClass) + " (" + effectiveMetaClass.getName() + ")", effectiveMetaClass);
                }
                return result;
            }

        }
    }

    protected class RegionsStepFrame extends StepFrame {
        protected static final String ADD_TABULATED_REGION_ACTION_ID = "tabulatedRegion";
        protected static final String ADD_SIMPLE_REGION_ACTION_ID = "simpleRegion";
        protected AddSimpleRegionAction addSimpleRegionAction;
        protected AddTabulatedRegionAction addTabulatedRegionAction;
        protected EditRegionAction editRegionAction;
        protected RemoveRegionAction removeRegionAction;
        //protected boolean isAddRegionActionPerformed;

        public RegionsStepFrame() {
            super(ReportWizardCreator.this, getMessage("reportRegions"), "regionsStep");
            initFrameHandler = new InitRegionsStepFrameHandler();

            beforeShowFrameHandler = new BeforeShowRegionsStepFrameHandler();

            beforeHideFrameHandler = new BeforeHideRegionsStepFrameHandler();
        }

        protected abstract class AddRegionAction extends AbstractAction {

            protected AddRegionAction(String id) {
                super(id);
            }

            protected ReportRegion createReportRegion(boolean tabulated) {
                ReportRegion reportRegion = metadata.create(ReportRegion.class);
                reportRegion.setReportData(getItem());
                reportRegion.setIsTabulatedRegion(tabulated);
                reportRegion.setOrderNum((long) getItem().getReportRegions().size() + 1L);
                return reportRegion;
            }

            protected void openRegionEditor(boolean tabulated, final ReportRegion item) {
                if (tabulated && BooleanUtils.isFalse((Boolean) isListedReport.getValue())) {
                    //show lookup for choosing parent collection for tabulated region
                    final Map<String, Object> lookupParams = new HashMap<>();
                    lookupParams.put("rootEntity", getItem().getEntityTreeRootNode());
                    lookupParams.put("collectionsOnly", Boolean.TRUE);
                    openLookup("report$ReportEntityTree.lookup", new Lookup.Handler() {
                        @Override
                        public void handleLookup(Collection items) {
                            if (items.size() == 1) {
                                Map<String, Object> editorParams = new HashMap<>();
                                //editorParams.put("showRoot", false););
                                editorParams.put("scalarOnly", Boolean.TRUE);
                                EntityTreeNode regionPropertiesRootNode = (EntityTreeNode) CollectionUtils.get(items, 0);
                                editorParams.put("rootEntity", regionPropertiesRootNode);
                                item.setRegionPropertiesRootNode(regionPropertiesRootNode);
                                Editor regionEditor = openEditor("report$Report.regionEditor", item, WindowManager.OpenType.DIALOG, editorParams, reportRegionsDs);
                                regionEditor.addListener(new RegionEditorCloseListener());
                            }
                        }
                    }, WindowManager.OpenType.DIALOG, lookupParams);
                } else {
                    Map<String, Object> editorParams = new HashMap<>();
                    editorParams.put("rootEntity", getItem().getEntityTreeRootNode());
                    item.setRegionPropertiesRootNode(getItem().getEntityTreeRootNode());
                    editorParams.put("scalarOnly", Boolean.TRUE);
                    Editor regionEditor = openEditor("report$Report.regionEditor", item, WindowManager.OpenType.DIALOG, editorParams, reportRegionsDs);
                    regionEditor.addListener(new RegionEditorCloseListener());
                }
            }

            protected class RegionEditorCloseListener implements CloseListener {
                @Override
                public void windowClosed(String actionId) {
                    if (COMMIT_ACTION_ID.equals(actionId)) {
                        regionsTable.refresh();
                        showOrHideAddRegionBtns();
                    }
                }
            }

        }

        protected class AddSimpleRegionAction extends AddRegionAction {
            public AddSimpleRegionAction() {
                super(ADD_SIMPLE_REGION_ACTION_ID);
            }

            @Override
            public void actionPerform(Component component) {
                openRegionEditor(false, createReportRegion(false));
            }
        }

        protected class AddTabulatedRegionAction extends AddRegionAction {
            public AddTabulatedRegionAction() {
                super(ADD_TABULATED_REGION_ACTION_ID);
            }

            @Override
            public void actionPerform(Component component) {
                openRegionEditor(true, createReportRegion(true));
            }
        }

        protected class ReportRegionTableColumnGenerator implements Table.ColumnGenerator<ReportRegion> {
            protected static final String WIDTH_PERCENT_100 = "100%";
            protected static final int MAX_ATTRS_BTN_CAPTION_WIDTH = 95;
            protected static final String BOLD_LABEL_STYLE = "semi-bold-label";
            private ReportRegion currentReportRegionGeneratedColumn;

            @Override
            public Component generateCell(ReportRegion entity) {
                currentReportRegionGeneratedColumn = entity;
                BoxLayout mainLayout = componentsFactory.createComponent(BoxLayout.VBOX);
                mainLayout.setWidth(WIDTH_PERCENT_100);
                mainLayout.add(createFirstTwoRowsLayout());
                mainLayout.add(createThirdRowAttrsLayout());
                return mainLayout;
            }

            private BoxLayout createFirstTwoRowsLayout() {
                BoxLayout firstTwoRowsLayout = componentsFactory.createComponent(BoxLayout.HBOX);
                BoxLayout expandedAttrsLayout = createExpandedAttrsLayout();
                firstTwoRowsLayout.setWidth(WIDTH_PERCENT_100);
                firstTwoRowsLayout.add(expandedAttrsLayout);
                firstTwoRowsLayout.add(createBtnsLayout());
                firstTwoRowsLayout.expand(expandedAttrsLayout);
                return firstTwoRowsLayout;
            }

            private BoxLayout createExpandedAttrsLayout() {
                BoxLayout expandedAttrsLayout = componentsFactory.createComponent(BoxLayout.VBOX);
                expandedAttrsLayout.setWidth(WIDTH_PERCENT_100);
                expandedAttrsLayout.add(createFirstRowAttrsLayout());
                expandedAttrsLayout.add(createSecondRowAttrsLayout());
                return expandedAttrsLayout;
            }

            private BoxLayout createFirstRowAttrsLayout() {
                BoxLayout firstRowAttrsLayout = componentsFactory.createComponent(BoxLayout.HBOX);
                firstRowAttrsLayout.setSpacing(true);
                Label regionLbl = componentsFactory.createComponent(Label.NAME);
                regionLbl.setStyleName(BOLD_LABEL_STYLE);
                regionLbl.setValue(getMessage("region"));
                Label regionValueLbl = componentsFactory.createComponent(Label.NAME);
                regionValueLbl.setValue(currentReportRegionGeneratedColumn.getName());
                regionValueLbl.setWidth(WIDTH_PERCENT_100);
                firstRowAttrsLayout.add(regionLbl);
                firstRowAttrsLayout.add(regionValueLbl);
                return firstRowAttrsLayout;
            }

            private BoxLayout createSecondRowAttrsLayout() {
                BoxLayout secondRowAttrsLayout = componentsFactory.createComponent(BoxLayout.HBOX);
                secondRowAttrsLayout.setSpacing(true);
                Label entityLbl = componentsFactory.createComponent(Label.NAME);
                entityLbl.setStyleName(BOLD_LABEL_STYLE);
                entityLbl.setValue(getMessage("entity"));
                Label entityValueLbl = componentsFactory.createComponent(Label.NAME);
                entityValueLbl.setValue(messageTools.getEntityCaption(currentReportRegionGeneratedColumn.getRegionPropertiesRootNode().getWrappedMetaClass()));
                entityValueLbl.setWidth(WIDTH_PERCENT_100);
                secondRowAttrsLayout.add(entityLbl);
                secondRowAttrsLayout.add(entityValueLbl);
                return secondRowAttrsLayout;
            }

            private BoxLayout createBtnsLayout() {
                BoxLayout btnsLayout = componentsFactory.createComponent(BoxLayout.HBOX);
                btnsLayout.setSpacing(true);
                btnsLayout.setStyleName("on-hover-visible-layout");
                return btnsLayout;
            }

            private BoxLayout createThirdRowAttrsLayout() {
                BoxLayout thirdRowAttrsLayout = componentsFactory.createComponent(BoxLayout.HBOX);
                thirdRowAttrsLayout.setSpacing(true);
                Label entityLbl = componentsFactory.createComponent(Label.NAME);
                entityLbl.setStyleName(BOLD_LABEL_STYLE);
                entityLbl.setValue(getMessage("attributes"));
                Button editBtn = componentsFactory.createComponent(Button.NAME);
                editBtn.setCaption(generateAttrsBtnCaption());
                editBtn.setStyleName("link");
                editBtn.setWidth(WIDTH_PERCENT_100);
                editBtn.setAction(editRegionAction);
                thirdRowAttrsLayout.add(entityLbl);
                thirdRowAttrsLayout.add(editBtn);
                return thirdRowAttrsLayout;
            }

            private String generateAttrsBtnCaption() {

                return StringUtils.abbreviate(StringUtils.join(CollectionUtils.collect(currentReportRegionGeneratedColumn.getRegionProperties(), new Transformer() {
                    @Override
                    public Object transform(Object input) {
                        return ((RegionProperty) input).getHierarchicalLocalizedNameExceptRoot();
                    }
                }), ", "), MAX_ATTRS_BTN_CAPTION_WIDTH);
            }
        }

        protected class RemoveRegionAction extends AbstractAction {
            public RemoveRegionAction() {
                super("removeRegion");
            }

            @Override
            public void actionPerform(Component component) {
                if (regionsTable.getSingleSelected() != null) {
                    showOptionDialog(getMessage("dialogs.Confirmation"), formatMessage("deleteRegion", ((ReportRegion) regionsTable.getSingleSelected()).getName()), MessageType.CONFIRMATION, new Action[]{
                            new DialogAction(DialogAction.Type.YES) {
                                @Override
                                public void actionPerform(Component component) {
                                    reportRegionsDs.removeItem((ReportRegion) regionsTable.getSingleSelected());
                                    normalizeRegionPropertiesOrderNum();
                                    regionsTable.refresh();
                                    showOrHideAddRegionBtns();
                                }
                            },
                            new DialogAction(DialogAction.Type.NO) {
                            }
                    });
                }
            }

            @Override
            public String getCaption() {
                return "";
            }

            protected void normalizeRegionPropertiesOrderNum() {
                long normalizedIdx = 0;
                List<ReportRegion> allItems = new ArrayList(reportRegionsDs.getItems());
                for (ReportRegion item : allItems) {
                    item.setOrderNum(++normalizedIdx); //first must to be 1
                }
            }
        }

        protected class EditRegionAction extends AddRegionAction {
            public EditRegionAction() {
                super("removeRegion");
            }

            @Override
            public void actionPerform(Component component) {
                if (regionsTable.getSingleSelected() != null) {
                    Map<String, Object> editorParams = new HashMap<>();
                    editorParams.put("rootEntity", ((ReportRegion) regionsTable.getSingleSelected()).getRegionPropertiesRootNode());
                    editorParams.put("scalarOnly", Boolean.TRUE);
                    Editor regionEditor = openEditor("report$Report.regionEditor", ((ReportRegion) regionsTable.getSingleSelected()), WindowManager.OpenType.DIALOG, editorParams, reportRegionsDs);
                    regionEditor.addListener(new RegionEditorCloseListener());
                }
            }

            @Override
            public String getCaption() {
                return "";
            }

        }

        protected class InitRegionsStepFrameHandler implements InitStepFrameHandler {
            @Override
            public void initFrame() {
                addSimpleRegionAction = new AddSimpleRegionAction();
                addTabulatedRegionAction = new AddTabulatedRegionAction();
                addSimpleRegionBtn.setAction(addSimpleRegionAction);
                addTabulatedRegionBtn.setAction(addTabulatedRegionAction);
                addRegionPopupBtn.addAction(addSimpleRegionAction);
                addRegionPopupBtn.addAction(addTabulatedRegionAction);
                regionsTable.addGeneratedColumn("regionsGeneratedColumn", new ReportRegionTableColumnGenerator());
                editRegionAction = new EditRegionAction();
                removeRegionAction = new RemoveRegionAction();

                moveDownBtn.setAction(new OrderableItemMoveAction("downItem", OrderableItemMoveAction.Direction.DOWN, regionsTable));
                moveUpBtn.setAction(new OrderableItemMoveAction("upItem", OrderableItemMoveAction.Direction.UP, regionsTable));
                removeBtn.setAction(removeRegionAction);
            }
        }

        protected class BeforeShowRegionsStepFrameHandler implements BeforeShowStepFrameHandler {
            @Override
            public void beforeShowFrame() {
                showOrHideAddRegionBtns();
                runBtn.setAction(new AbstractAction("runReport") {
                    @Override
                    public void actionPerform(Component component) {
                        if (getItem().getReportRegions().isEmpty()) {
                            showNotification(getMessage("addRegionsWarn"), NotificationType.TRAY);
                            return;
                        }
                        lastGeneratedTmpReport = toReport(true);

                        if (lastGeneratedTmpReport != null) {
                            reportGuiManager.runReport(lastGeneratedTmpReport, stepFrameManager.getCurrentIFrame());
                        }
                    }
                });
                showAddRegion();
                setCorrectReportOutputType();
            }

            private void showAddRegion() {
                if (reportRegionsDs.getItems().isEmpty()) {
                    if (BooleanUtils.isTrue((Boolean) isListedReport.getValue())) {
                        if (entityTreeHasSimpleAttrs && getItem().getReportRegions().isEmpty()) {
                            addTabulatedRegionAction.actionPerform(regionsStepFrame.getFrame());
                        }
                    } else {
                        if (entityTreeHasSimpleAttrs && entityTreeHasCollections) {
                            addSimpleRegionAction.actionPerform(regionsStepFrame.getFrame());
                        } else if (entityTreeHasSimpleAttrs && !entityTreeHasCollections) {
                            addSimpleRegionAction.actionPerform(regionsStepFrame.getFrame());
                        } else if (!entityTreeHasSimpleAttrs && entityTreeHasCollections) {
                            addTabulatedRegionAction.actionPerform(regionsStepFrame.getFrame());
                        }
                    }
                }
            }
        }

        protected class BeforeHideRegionsStepFrameHandler implements BeforeHideStepFrameHandler {
            @Override
            public void beforeHideFrame() {
//                runBtn.setVisible(false);
            }
        }
    }

    protected class SaveStepFrame extends StepFrame {

        public SaveStepFrame() {
            super(ReportWizardCreator.this, getMessage("saveReport"), "saveStep");
            isLast = true;
            beforeShowFrameHandler = new BeforeShowSaveStepFrameHandler();

            beforeHideFrameHandler = new BeforeHideSaveStepFrameHandler();
        }

        protected class BeforeShowSaveStepFrameHandler implements BeforeShowStepFrameHandler {
            @Override
            public void beforeShowFrame() {
                saveBtn.setVisible(true);
                saveBtn.setAction(new AbstractAction("saveReport") {
                    @Override
                    public void actionPerform(Component component) {
                        try {
                            outputFileName.validate();
                        } catch (ValidationException e) {
                            showNotification(getMessage("validationFail.caption"),
                                    String.format(getMessage("validation.required.defaultMsg"), getMessage("outputFileName")), NotificationType.TRAY);
                            return;
                        }
                        if (getItem().getReportRegions().isEmpty()) {
                            showOptionDialog(getMessage("dialogs.Confirmation"), getMessage("confirmSaveWithoutRegions"), MessageType.CONFIRMATION, new Action[]{
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
                        Report r = toReport(false);
                        if (r != null) {
                            ReportWizardCreator.this.close(COMMIT_ACTION_ID, true); //true is ok cause it is a save btn
                        }
                    }
                });
                downloadTemplateFile.setCaption(generateTemplateFileName(
                        templateFileFormat.getValue().toString().toLowerCase()));
                downloadTemplateFile.setAction(new AbstractAction("generateNewTemplateAndGet") {
                    @Override
                    public void actionPerform(Component component) {
                        byte[] newTemplate = null;
                        try {
                            getItem().setName(reportName.getValue().toString());
                            newTemplate = reportWizardService.generateTemplate(getItem(), (TemplateFileType) templateFileFormat.getValue());
                            ExportDisplay exportDisplay = AppConfig.createExportDisplay((IFrame) getComponent("saveStep"));
                            exportDisplay.show(new ByteArrayDataProvider(newTemplate),
                                    downloadTemplateFile.getCaption(), ExportFormat.getByExtension(templateFileFormat.getValue().toString().toLowerCase()));
                        } catch (TemplateGenerationException e) {
                            showNotification(getMessage("templateGenerationException"), NotificationType.WARNING);
                        }
                        if (newTemplate != null) {
                            lastGeneratedTemplate = newTemplate;
                        }
                    }
                });
                if (StringUtils.isEmpty(outputFileName.<String>getValue()))
                    outputFileName.setValue(generateOutputFileName(templateFileFormat.getValue().toString().toLowerCase()));
                setCorrectReportOutputType();

            }

        }

        protected class BeforeHideSaveStepFrameHandler implements BeforeHideStepFrameHandler {
            @Override
            public void beforeHideFrame() {
                saveBtn.setVisible(false);
            }
        }
    }

}