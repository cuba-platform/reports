/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.gui.definition.edit;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.autocomplete.AutoCompleteSupport;
import com.haulmont.cuba.gui.autocomplete.JpqlSuggestionFactory;
import com.haulmont.cuba.gui.autocomplete.Suggester;
import com.haulmont.cuba.gui.autocomplete.Suggestion;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.*;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.app.service.ReportWizardService;
import com.haulmont.reports.entity.*;
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
    private CollectionDatasource<ReportInputParameter, UUID> parametersDs;

    @Inject
    protected Table dataSets;
    @Named("text")
    protected SourceCodeEditor dataSetScriptField;
    @Inject
    protected BoxLayout textBox;
    @Inject
    protected GridLayout entityGrid;
    @Inject
    protected GridLayout entitiesGrid;
    @Inject
    protected GridLayout commonEntityGrid;
    @Inject
    protected Label viewNameLabel;
    @Inject
    protected LookupField orientation;
    @Inject
    protected LookupField parentBand;
    @Inject
    protected TextField name;
    @Inject
    protected LookupField viewNameLookup;
    @Inject
    protected LookupField entitiesParamLookup;
    @Inject
    protected LookupField entityParamLookup;
    @Inject
    protected CheckBox useExistingViewCheckbox;
    @Inject
    protected Button viewEditButton;
    @Inject
    protected Label buttonEmptyElement;
    @Inject
    protected Label checkboxEmptyElement;
    @Inject
    protected Label spacer;
    @Inject
    protected Metadata metadata;
    @Inject
    protected ReportService reportService;
    @Inject
    protected ReportWizardService reportWizardService;
    @Inject
    protected BoxLayout editPane;

    public interface Companion {
        void initDatasetsTable(Table table);
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

    @Override
    public List<Suggestion> getSuggestions(AutoCompleteSupport source, String text, int cursorPosition) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        int queryPosition = cursorPosition - 1;

        return JpqlSuggestionFactory.requestHint(text, queryPosition, source, cursorPosition);
    }

    @Override
    protected void initNewItem(BandDefinition item) {
        item.setOrientation(Orientation.HORIZONTAL);
    }

    @Override
    protected void postInit() {
        selectFirstDataSet();
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        initDataSetListeners();

        initBandDefinitionsListeners();

        initParametersListeners();

        initCompanion();

        initActions();
    }

    protected void initCompanion() {
        Companion companion = getCompanion();
        if (companion != null) {
            companion.initDatasetsTable(dataSets);
        }
    }

    protected void initActions() {
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

        Action editDataSetViewAction = new EditViewAction(this);
        viewEditButton.setAction(editDataSetViewAction);

        viewNameLookup.setOptionsMap(new HashMap<String, Object>());

        entitiesParamLookup.setNewOptionAllowed(true);
        entityParamLookup.setNewOptionAllowed(true);
        viewNameLookup.setNewOptionAllowed(true);
        entitiesParamLookup.setNewOptionHandler(LinkedWithPropertyNewOptionHandler.handler(dataSetsDs, "listEntitiesParamName"));
        entityParamLookup.setNewOptionHandler(LinkedWithPropertyNewOptionHandler.handler(dataSetsDs, "entityParamName"));
        viewNameLookup.setNewOptionHandler(LinkedWithPropertyNewOptionHandler.handler(dataSetsDs, "viewName"));
    }

    protected void initParametersListeners() {
        parametersDs.addListener(new CollectionDsListenerAdapter<ReportInputParameter>() {
            @Override
            public void collectionChanged(CollectionDatasource ds, Operation operation, List<ReportInputParameter> items) {
                super.collectionChanged(ds, operation, items);
                Map<String, Object> paramAliases = new HashMap<>();
                for (ReportInputParameter item : (Collection<ReportInputParameter>) ds.getItems()) {
                    paramAliases.put(item.getName(), item.getAlias());
                }
                entitiesParamLookup.setOptionsMap(paramAliases);
                entityParamLookup.setOptionsMap(paramAliases);
            }
        });
    }

    protected void initBandDefinitionsListeners() {
        bandDefinitionDs.addListener(new DsListenerAdapter<BandDefinition>() {
            @Override
            public void itemChanged(Datasource<BandDefinition> ds, BandDefinition prevItem, BandDefinition item) {
                updateRequiredIndicators(item);
                selectFirstDataSet();
            }
        });
    }

    protected void initDataSetListeners() {
        dataSetsDs.addListener(new DsListenerAdapter<DataSet>() {
            @Override
            public void valueChanged(DataSet source, String property, @Nullable Object prevValue, @Nullable Object value) {
                applyVisibilityRules(source);
                if ("entityParamName".equals(property) || "listEntitiesParamName".equals(property)) {
                    ReportInputParameter linkedParameter = findParameterByAlias(String.valueOf(value));
                    refreshViewNames(linkedParameter);
                }

                @SuppressWarnings("unchecked")
                DatasourceImplementation<DataSet> implementation = (DatasourceImplementation<DataSet>) dataSetsDs;
                implementation.modified(source);
            }

            @Override
            public void itemChanged(Datasource ds, @Nullable DataSet prevItem, @Nullable DataSet item) {
                if (item != null) {
                    applyVisibilityRules(item);

                    ReportInputParameter linkedParameter = null;
                    if (item.getType() == DataSetType.SINGLE) {
                        linkedParameter = findParameterByAlias(item.getEntityParamName());
                        refreshViewNames(linkedParameter);
                    } else if (item.getType() == DataSetType.MULTI) {
                        linkedParameter = findParameterByAlias(item.getListEntitiesParamName());
                        refreshViewNames(linkedParameter);
                    }
                } else {
                    hideAllDataSetEditComponents();
                }
            }
        });

        hideAllDataSetEditComponents();
    }

    protected void updateRequiredIndicators(BandDefinition item) {
        boolean required = !(item == null || reportDs.getItem().getRootBandDefinition().equals(item));
        parentBand.setRequired(required);
        orientation.setRequired(required);
        name.setRequired(item != null);
    }

    @Nullable
    protected ReportInputParameter findParameterByAlias(String alias) {
        for (ReportInputParameter reportInputParameter : parametersDs.getItems()) {
            if (reportInputParameter.getAlias().equals(alias)) {
                return reportInputParameter;
            }
        }
        return null;
    }

    protected void refreshViewNames(@Nullable ReportInputParameter reportInputParameter) {
        if (reportInputParameter != null) {
            if (StringUtils.isNotBlank(reportInputParameter.getEntityMetaClass())) {
                MetaClass parameterMetaClass = metadata.getClass(reportInputParameter.getEntityMetaClass());
                Collection<String> viewNames = metadata.getViewRepository().getViewNames(parameterMetaClass);
                if (viewNames != null) {
                    Map<String, Object> views = new HashMap<>();
                    for (String viewName : viewNames) {
                        views.put(viewName, viewName);
                    }
                    views.put(View.LOCAL, View.LOCAL);
                    views.put(View.MINIMAL, View.MINIMAL);
                    viewNameLookup.setOptionsMap(views);
                    return;
                }
            }
        }

        viewNameLookup.setOptionsMap(new HashMap<String, Object>());
    }

    protected void applyVisibilityRules(DataSet item) {
        applyVisibilityRulesForType(item.getType());
        if (item.getType() == DataSetType.SINGLE || item.getType() == DataSetType.MULTI) {
            applyVisibilityRulesForEntityType(item);
        }
    }

    protected void applyVisibilityRulesForType(DataSetType dsType) {
        hideAllDataSetEditComponents();

        if (dsType != null) {
            switch (dsType) {
                case SQL:
                case JPQL:
                case GROOVY:
                    editPane.add(textBox);
                    break;
                case SINGLE:
                    editPane.add(entityGrid);
                    editPane.add(commonEntityGrid);
                    editPane.add(spacer);
                    editPane.expand(spacer);
                    break;
                case MULTI:
                    editPane.add(entitiesGrid);
                    editPane.add(commonEntityGrid);
                    editPane.add(spacer);
                    editPane.expand(spacer);
                    break;
            }

            switch (dsType) {
                case SQL:
                    dataSetScriptField.setMode(SourceCodeEditor.Mode.SQL);
                    dataSetScriptField.setSuggester(null);
                    break;

                case GROOVY:
                    dataSetScriptField.setSuggester(null);
                    dataSetScriptField.setMode(SourceCodeEditor.Mode.Groovy);
                    break;

                case JPQL:
                    dataSetScriptField.setSuggester(this);
                    dataSetScriptField.setMode(SourceCodeEditor.Mode.Text);
                    break;

                default:
                    dataSetScriptField.setSuggester(null);
                    dataSetScriptField.setMode(SourceCodeEditor.Mode.Text);
                    break;
            }
        }
    }

    protected void applyVisibilityRulesForEntityType(DataSet item) {
        commonEntityGrid.remove(viewNameLabel);
        commonEntityGrid.remove(viewNameLookup);
        commonEntityGrid.remove(viewEditButton);
        commonEntityGrid.remove(buttonEmptyElement);
        commonEntityGrid.remove(useExistingViewCheckbox);
        commonEntityGrid.remove(checkboxEmptyElement);

        if (Boolean.TRUE.equals(item.getUseExistingView())) {
            commonEntityGrid.add(viewNameLabel);
            commonEntityGrid.add(viewNameLookup);
        } else {
            commonEntityGrid.add(viewEditButton);
            commonEntityGrid.add(buttonEmptyElement);
        }

        commonEntityGrid.add(useExistingViewCheckbox);
        commonEntityGrid.add(checkboxEmptyElement);
    }

    protected void hideAllDataSetEditComponents() {
        // do not use setVisible(false) due to web legacy (Vaadin 6) layout problems #PL-3916
        editPane.remove(textBox);
        editPane.remove(entityGrid);
        editPane.remove(entitiesGrid);
        editPane.remove(commonEntityGrid);
        editPane.remove(spacer);
    }

    protected void selectFirstDataSet() {
        dataSetsDs.refresh();
        if (!dataSetsDs.getItemIds().isEmpty()) {
            Entity item = dataSetsDs.getItem(dataSetsDs.getItemIds().iterator().next());
            dataSets.setSelected(item);
        } else {
            dataSets.setSelected((Entity) null);
        }
    }

    // For EditViewAction
    protected String formatMessage(String key, Object... params) {
        return super.formatMessage(key, params);
    }

    // This is a stub for using set in some DataSet change listener
    protected void setViewEditVisibility(DataSet dataSet) {
        if (isViewEditAllowed(dataSet)) {
            viewEditButton.setVisible(true);
        } else {
            viewEditButton.setVisible(false);
        }
    }

    protected boolean isViewEditAllowed(DataSet dataSet) {
        return true;
    }

}