/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.gui.definition.edit;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.global.ViewProperty;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.autocomplete.AutoCompleteSupport;
import com.haulmont.cuba.gui.autocomplete.JpqlSuggestionFactory;
import com.haulmont.cuba.gui.autocomplete.Suggester;
import com.haulmont.cuba.gui.autocomplete.Suggestion;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.reports.app.EntityTree;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.app.service.ReportWizardService;
import com.haulmont.reports.entity.*;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.ReportRegion;
import com.haulmont.reports.entity.wizard.TemplateFileType;
import com.haulmont.reports.exception.TemplateGenerationException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class BandDefinitionEditor extends AbstractEditor<BandDefinition> implements Suggester {

    @Inject
    protected Datasource<BandDefinition> bandDefinitionDs;
    @Inject
    protected CollectionDatasource<DataSet, UUID> dataSetsDs;
    @Inject
    protected Datasource<Report> reportDs;
    @Inject
    protected CollectionDatasource<ReportTemplate, UUID> templatesDs;
    @Inject
    protected HierarchicalDatasource<BandDefinition, UUID> treeDs;
    @Inject
    protected Table dataSets;
    @Named("text")
    protected SourceCodeEditor datasetScriptField;
    @Named("textBox")
    protected BoxLayout textBox;
    @Named("entityBox")
    protected BoxLayout entityBox;
    @Named("entitiesBox")
    protected BoxLayout entitiesBox;
    @Inject
    protected LookupField orientation;
    @Inject
    protected LookupField parentBand;
    @Inject
    protected TextField name;
    @Inject
    protected Button singleDataSetEditViewButton;
    @Inject
    protected Button multiDataSetEditViewButton;
    @Inject
    protected Metadata metadata;
    @Inject
    protected ReportService reportService;
    @Inject
    protected ReportWizardService reportWizardService;
    @Inject
    protected BoxLayout editPane;

    protected List xlsExts = Arrays.asList("xls", "xlsx");

    @Override
    protected void initNewItem(BandDefinition item) {
        item.setOrientation(Orientation.HORIZONTAL);
    }

    @Override
    protected void postInit() {
        selectFirstDataset();
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        dataSets.addAction(new RemoveAction(dataSets, false) {
            @Override
            public String getDescription() {
                return getMessage("description.removeDataSet");
            }

            @Override
            public String getCaption() {
                return "";
            }
        });

        dataSets.addAction(new AbstractAction("create") {
            @Override
            public String getDescription() {
                return getMessage("description.createDataSet");
            }

            @Override
            public String getCaption() {
                return "";
            }

            @Override
            public void actionPerform(Component component) {
                DataSet dataset = new DataSet();
                BandDefinition selectedBand = bandDefinitionDs.getItem();
                if (selectedBand != null) {
                    if (CollectionUtils.isEmpty(selectedBand.getDataSets())) {
                        selectedBand.setDataSets(new ArrayList<DataSet>());
                    }
                    dataset.setBandDefinition(selectedBand);
                    dataset.setName(selectedBand.getName() != null ? selectedBand.getName() : "dataset");
                    dataset.setType(DataSetType.GROOVY);

                    dataset.setEntityParamName("entity");
                    dataset.setListEntitiesParamName("entities");

                    selectedBand.getDataSets().add(dataset);
                    dataSetsDs.addItem(dataset);
                    dataSetsDs.setItem(dataset);

                    dataSets.setSelected(dataset);
                }
            }
        });

        initDataSetControls();

        bandDefinitionDs.addListener(new DsListenerAdapter<BandDefinition>() {
            @Override
            public void itemChanged(Datasource<BandDefinition> ds, BandDefinition prevItem, BandDefinition item) {
                updateRequiredIndicators(item);
                selectFirstDataset();
            }
        });

        BandDefinitionEditor.Companion companion = getCompanion();
        if (companion != null) {
            companion.initDatasetsTable(dataSets);
        }
        Action editDataSetViewAction = new EditDataSetViewAction();
        singleDataSetEditViewButton.setAction(editDataSetViewAction);
        multiDataSetEditViewButton.setAction(editDataSetViewAction);
    }

    //TODO it is a stub for using set in some dataset change listener
    protected void showOrHideEditDataSetViewBtn(DataSet dataSet) {
        if (isDataSetViewEditAllowed(dataSet)) {
            singleDataSetEditViewButton.setVisible(true);
            singleDataSetEditViewButton.setVisible(true);
        } else {
            singleDataSetEditViewButton.setVisible(false);
            singleDataSetEditViewButton.setVisible(false);
        }
    }

    //TODO
    protected boolean isDataSetViewEditAllowed(DataSet dataSet) {
        return true;
    }

    protected void updateRequiredIndicators(BandDefinition item) {
        boolean required = !(item == null || reportDs.getItem().getRootBandDefinition().equals(item));
        parentBand.setRequired(required);
        orientation.setRequired(required);
        name.setRequired(item != null);
    }

    @Override
    public boolean validateAll() {
        //validation works in ReportEditor
        return true;
    }

    public void initDataSetControls() {
        dataSetsDs.addListener(new DsListenerAdapter<DataSet>() {
            @Override
            public void valueChanged(DataSet source, String property,
                                     @Nullable Object prevValue, @Nullable Object value) {
                if (property.equals("type")) {
                    applyType((DataSetType) value);
                }
                ((DatasourceImplementation) dataSetsDs).modified(source);
            }

            @Override
            public void itemChanged(Datasource ds, @Nullable DataSet prevItem, @Nullable DataSet item) {
                if (item != null) {
                    applyType(item.getType());
                } else {
                    hideEditComponents();
                }
            }
        });

        hideEditComponents();
    }

    protected void hideEditComponents() {
        // do not use setVisible(false) due to web legacy (Vaadin 6) layout problems #PL-3916
        editPane.remove(textBox);
        editPane.remove(entityBox);
        editPane.remove(entitiesBox);
    }

    protected void applyType(DataSetType dsType) {
        hideEditComponents();

        if (dsType != null) {
            switch (dsType) {
                case SQL:
                case JPQL:
                case GROOVY:
                    editPane.add(textBox);
                    break;
                case SINGLE:
                    editPane.add(entityBox);
                    break;
                case MULTI:
                    editPane.add(entitiesBox);
                    break;
            }

            switch (dsType) {
                case SQL:
                    datasetScriptField.setMode(SourceCodeEditor.Mode.SQL);
                    datasetScriptField.setSuggester(null);
                    break;

                case GROOVY:
                    datasetScriptField.setSuggester(null);
                    datasetScriptField.setMode(SourceCodeEditor.Mode.Groovy);
                    break;

                case JPQL:
                    datasetScriptField.setSuggester(this);
                    datasetScriptField.setMode(SourceCodeEditor.Mode.Text);
                    break;

                default:
                    datasetScriptField.setSuggester(null);
                    datasetScriptField.setMode(SourceCodeEditor.Mode.Text);
                    break;
            }
        }
    }

    protected void selectFirstDataset() {
        dataSetsDs.refresh();
        if (!dataSetsDs.getItemIds().isEmpty()) {
            Entity item = dataSetsDs.getItem(dataSetsDs.getItemIds().iterator().next());
            dataSets.setSelected(item);
        } else {
            dataSets.setSelected((Entity) null);
        }
    }

    @Override
    public List<Suggestion> getSuggestions(AutoCompleteSupport source, String text, int cursorPosition) {
        if (text == null || "".equals(text.trim())) {
            return Collections.emptyList();
        }
        int queryPosition = cursorPosition - 1;

        return JpqlSuggestionFactory.requestHint(text, queryPosition, source, cursorPosition);
    }

    public void setBandDefinition(BandDefinition bandDefinition) {
        bandDefinitionDs.setItem(bandDefinition);
        if (bandDefinition != null && bandDefinition.getParent() == null) {
            name.setEditable(false);
        } else {
            name.setEditable(true);
        }
    }

    public Datasource<BandDefinition> getBandDefinitionDs() {
        return bandDefinitionDs;
    }

    @Override
    public void setEnabled(boolean enabled) {
        //Desktop Component containers doesn't apply disable flags for child components
        for (Component component : getComponents()) {
            component.setEnabled(enabled);
        }
    }

    public interface Companion {
        void initDatasetsTable(Table table);
    }

    protected class EditDataSetViewAction extends AbstractAction {
        public EditDataSetViewAction() {
            super("editView");
        }

        @Override
        public void actionPerform(Component component) {
            if (dataSets.getSingleSelected() instanceof DataSet) {
                final DataSet dataSet = dataSets.getSingleSelected();
                if (dataSet != null && (DataSetType.SINGLE == dataSet.getType() || DataSetType.MULTI == dataSet.getType())) {
                    MetaClass forEntityTreeModelMetaClass = findMetaClassByAlias(dataSet);
                    if (forEntityTreeModelMetaClass != null) {

                        final EntityTree entityTree = reportWizardService.buildEntityTree(forEntityTreeModelMetaClass);
                        ReportRegion reportRegion = dataSetToReportRegion(dataSet, entityTree);


                        if (reportRegion != null) {
                            if (reportRegion.getRegionPropertiesRootNode() == null) {
                                showNotification(getMessage("dataSet.entityAliasInvalid"), NotificationType.TRAY);
                                //without that root node region editor form will not initialized correctly and became empty. just return
                                return;
                            } else {
                                //Open editor and convert saved in editor ReportRegion item to View
                                Map<String, Object> editorParams = new HashMap<>();
                                editorParams.put("asViewEditor", Boolean.TRUE);
                                editorParams.put("rootEntity", reportRegion.getRegionPropertiesRootNode());
                                editorParams.put("scalarOnly", Boolean.TRUE);

                                final Editor regionEditor = openEditor("report$Report.regionEditor", reportRegion, WindowManager.OpenType.DIALOG, editorParams, dataSetsDs);
                                regionEditor.addListener(new CloseListener() {
                                    @Override
                                    public void windowClosed(String actionId) {
                                        if (COMMIT_ACTION_ID.equals(actionId)) {
                                            dataSet.setView(reportRegionToView(entityTree, (ReportRegion) regionEditor.getItem()));
                                        }
                                    }
                                });
                            }

                        }

                    }
                }

            }
        }

        protected void generateAndAddNewReportTemplate(EntityTree entityTree) {
            Report report = reportDs.getItem();

            ReportData reportData = new ReportData();
            reportData.setName(report.getName());
            reportData.setTemplateFileName(report.getDefaultTemplate() == null ? "default.docx" :
                    report.getDefaultTemplate().getName().replaceFirst("(\\(\\d+\\))?\\.", "(" + report.getTemplates().size() + ")."));
            reportData.setIsTabulatedReport(!CollectionUtils.isEmpty(report.getInputParameters()) && ParameterType.ENTITY_LIST.equals(report.getInputParameters().get(0).getType()));
            reportData.setOutputFileType(report.getDefaultTemplate().getReportOutputType());
            reportData.setGroup(report.getGroup());

            List<ReportRegion> regionList = new ArrayList<ReportRegion>();
            List<BandDefinition> bands = new ArrayList<BandDefinition>(treeDs.getItems());
            Collections.sort(bands, new Comparator<BandDefinition>() {
                @Override
                public int compare(BandDefinition o1, BandDefinition o2) {
                    return o1.getPosition() < o2.getPosition() ? -1 : o1.getPosition() == o2.getPosition() ? 0 : 1;
                }
            });
            for (BandDefinition bandDefinition : bands) {
                if (!bandDefinition.equals(report.getRootBand()))
                    for (DataSet dataSetC : bandDefinition.getDataSets()) {
                        if (DataSetType.SINGLE == dataSetC.getType() || DataSetType.MULTI == dataSetC.getType()) {
                            ReportRegion reportRegion = dataSetToReportRegion(dataSetC, entityTree);
                            reportRegion.setReportData(reportData);
                            reportRegion.setBandNameFromReport(bandDefinition.getName());
                            regionList.add(reportRegion);
                        }
                    }
            }

            reportData.setReportRegions(regionList);

            TemplateFileType templateFileType = TemplateFileType.HTML.name().equals(report.getDefaultTemplate().getExt().toUpperCase()) ?
                    TemplateFileType.HTML : (xlsExts.contains(report.getDefaultTemplate().getExt().toUpperCase()) ?
                    TemplateFileType.XLSX : TemplateFileType.DOCX);
            byte[] templateByteArray = null;
            try {
                templateByteArray = reportWizardService.generateTemplate(reportData, templateFileType);
            } catch (TemplateGenerationException e) {
                showNotification(getMessage("templateGenerationException"), NotificationType.WARNING);
            }
            if (templateByteArray != null) {
                ReportTemplate reportTemplate = metadata.create(ReportTemplate.class);
                reportTemplate.setReport(report);
                reportTemplate.setCode(ReportService.DEFAULT_TEMPLATE_CODE);
                reportTemplate.setName(reportData.getTemplateFileName());
                reportTemplate.setContent(templateByteArray);
                reportTemplate.setCustomFlag(Boolean.FALSE);
                reportTemplate.setReportOutputType(reportData.getOutputFileType());
                report.getTemplates().add(reportTemplate);
                templatesDs.addItem(reportTemplate);
                report.setDefaultTemplate(reportTemplate);
            }
        }

        //Detect metaclass by an alias and parameter
        protected MetaClass findMetaClassByAlias(DataSet dataSet) {
            MetaClass byAliasMetaClass;
            String dataSetAlias = null;
            switch (dataSet.getType()) {
                case SINGLE:
                    dataSetAlias = dataSet.getEntityParamName();
                    break;
                case MULTI:
                    dataSetAlias = dataSet.getListEntitiesParamName();
                    break;
            }

            byAliasMetaClass = reportService.findMetaClassByDataSetEntityAlias(dataSetAlias, dataSet.getType(), dataSet.getBandDefinition().getReport().getInputParameters());

            //Lets return some value
            if (byAliasMetaClass == null) {
                //Can`t determine parameter and its metaClass by alias
                showNotification(getMessage("dataSet.entityAliasInvalid"), NotificationType.TRAY);
                return null;
                //when byAliasMetaClass is null we return also null
            } else {
                //Detect metaclass by current view for comparison
                MetaClass viewMetaClass = null;
                if (dataSet.getView() != null) {
                    viewMetaClass = metadata.getClass(dataSet.getView().getEntityClass());
                }
                if (viewMetaClass != null && !byAliasMetaClass.getName().equals(viewMetaClass.getName())) {
                    showNotification(formatMessage("dataSet.entityWasChanged", byAliasMetaClass.getName()), NotificationType.TRAY);
                }
                return byAliasMetaClass;
            }
        }

        protected ReportRegion dataSetToReportRegion(DataSet dataSet, EntityTree entityTree) {
            boolean isTabulatedRegion;
            View view = null;
            String collectionPropertyName;
            switch (dataSet.getType()) {
                case SINGLE:
                    isTabulatedRegion = false;
                    view = dataSet.getView();
                    collectionPropertyName = null;
                    break;
                case MULTI:
                    isTabulatedRegion = true;
                    collectionPropertyName = StringUtils.substringAfter(dataSet.getListEntitiesParamName(), "#");
                    if (StringUtils.isBlank(collectionPropertyName) && dataSet.getListEntitiesParamName().indexOf("#") != -1) {
                        showNotification(getMessage("dataSet.entityAliasInvalid"), NotificationType.TRAY);
                        return null;
                    }
                    if (StringUtils.isNotBlank(collectionPropertyName)) {

                        if (dataSet.getView() != null) {
                            view = findSubViewByCollectionPropertyName(dataSet.getView(), collectionPropertyName);

                        }
                        if (view == null) {
                            //View was never created for current dataset.
                            //We must to create minimal view that contains collection property for ability of creating ReportRegion.regionPropertiesRootNode later
                            MetaClass metaClass = entityTree.getEntityTreeRootNode().getWrappedMetaClass();
                            MetaProperty metaProperty = metaClass.getProperty(collectionPropertyName);
                            if (metaProperty != null && metaProperty.getDomain() != null && metaProperty.getRange().getCardinality().isMany()) {
                                view = new View(metaProperty.getDomain().getJavaClass());
                            } else {
                                showNotification(formatMessage("dataSet.cantFindCollectionProperty", collectionPropertyName, metaClass.getName()), NotificationType.TRAY);
                                return null;
                            }
                        }
                    } else {
                        view = dataSet.getView();
                    }
                    break;
                default:
                    return null;
            }
            return reportWizardService.createReportRegionByView(entityTree, isTabulatedRegion,
                    view, collectionPropertyName);
        }

        protected View reportRegionToView(EntityTree entityTree, ReportRegion reportRegion) {
            return reportWizardService.createViewByReportRegions(entityTree.getEntityTreeRootNode(), Collections.singletonList(reportRegion));
        }

        public View findSubViewByCollectionPropertyName(View view, final String propertyName) {
            if (view == null) {
                return null;
            }
            for (ViewProperty viewProperty : view.getProperties()) {
                if (propertyName.equals(viewProperty.getName())) {
                    if (viewProperty.getView() != null) {
                        return viewProperty.getView();
                    }
                }

                if (viewProperty.getView() != null) {
                    View foundedView = findSubViewByCollectionPropertyName(viewProperty.getView(), propertyName);
                    if (foundedView != null) {
                        return foundedView;
                    }
                }
            }
            return null;
        }
    }
}