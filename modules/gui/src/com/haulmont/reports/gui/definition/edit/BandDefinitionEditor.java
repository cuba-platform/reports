/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports.gui.definition.edit;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Stores;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.components.autocomplete.AutoCompleteSupport;
import com.haulmont.cuba.gui.components.autocomplete.JpqlSuggestionFactory;
import com.haulmont.cuba.gui.components.autocomplete.Suggester;
import com.haulmont.cuba.gui.components.autocomplete.Suggestion;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.app.service.ReportWizardService;
import com.haulmont.reports.entity.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

public class BandDefinitionEditor extends AbstractFrame implements Suggester {

    @Inject
    protected Datasource<BandDefinition> bandDefinitionDs;
    @Inject
    protected CollectionDatasource<DataSet, UUID> dataSetsDs;
    @Inject
    protected Datasource<Report> reportDs;
    @Inject
    protected CollectionDatasource<ReportInputParameter, UUID> parametersDs;
    @Inject
    protected Table<DataSet> dataSets;
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
    protected HBoxLayout textParamsBox;
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
    protected LookupField dataStore;
    @Inject
    protected CheckBox processTemplate;
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
    public void init(Map<String, Object> params) {
        super.init(params);

        initDataSetListeners();

        initBandDefinitionsListeners();

        initParametersListeners();

        initCompanion();

        initActions();

        initDataStoreField();

    }

    protected void initDataStoreField() {
        Map<String, Object> all = new HashMap<>();
        all.put(getMessage("dataSet.dataStoreMain"), Stores.MAIN);
        for (String additional : Stores.getAdditional()) {
            all.put(additional, additional);
        }
        dataStore.setOptionsMap(all);
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
                        selectedBand.setDataSets(new ArrayList<>());
                    }
                    dataset.setBandDefinition(selectedBand);
                    dataset.setName(selectedBand.getName() != null ? selectedBand.getName() : "dataset");
                    dataset.setType(DataSetType.GROOVY);
                    dataset.setText("return [[:]]");

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

        viewNameLookup.setOptionsMap(new HashMap<>());

        entitiesParamLookup.setNewOptionAllowed(true);
        entityParamLookup.setNewOptionAllowed(true);
        viewNameLookup.setNewOptionAllowed(true);
        entitiesParamLookup.setNewOptionHandler(LinkedWithPropertyNewOptionHandler.handler(dataSetsDs, "listEntitiesParamName"));
        entityParamLookup.setNewOptionHandler(LinkedWithPropertyNewOptionHandler.handler(dataSetsDs, "entityParamName"));
        viewNameLookup.setNewOptionHandler(LinkedWithPropertyNewOptionHandler.handler(dataSetsDs, "viewName"));
    }

    protected void initParametersListeners() {
        parametersDs.addCollectionChangeListener(e -> {
            Map<String, Object> paramAliases = new HashMap<>();

            for (ReportInputParameter item : e.getDs().getItems()) {
                paramAliases.put(item.getName(), item.getAlias());
            }
            entitiesParamLookup.setOptionsMap(paramAliases);
            entityParamLookup.setOptionsMap(paramAliases);
        });
    }

    protected void initBandDefinitionsListeners() {
        bandDefinitionDs.addItemChangeListener(e -> {
            updateRequiredIndicators(e.getItem());
            selectFirstDataSet();
        });
        bandDefinitionDs.addItemPropertyChangeListener(e -> {
            if ("name".equals(e.getProperty()) && StringUtils.isBlank((String) e.getValue())) {
                e.getItem().setName("*");
            }
        });
    }

    protected void initDataSetListeners() {
        dataSetsDs.addItemChangeListener(e -> {
            if (e.getItem() != null) {
                applyVisibilityRules(e.getItem());

                ReportInputParameter linkedParameter = null;
                if (e.getItem().getType() == DataSetType.SINGLE) {
                    linkedParameter = findParameterByAlias(e.getItem().getEntityParamName());
                    refreshViewNames(linkedParameter);
                } else if (e.getItem().getType() == DataSetType.MULTI) {
                    linkedParameter = findParameterByAlias(e.getItem().getListEntitiesParamName());
                    refreshViewNames(linkedParameter);
                }

                dataSetScriptField.resetEditHistory();
            } else {
                hideAllDataSetEditComponents();
            }
        });

        dataSetsDs.addItemPropertyChangeListener(e -> {
            applyVisibilityRules(e.getItem());
            if ("entityParamName".equals(e.getProperty()) || "listEntitiesParamName".equals(e.getProperty())) {
                ReportInputParameter linkedParameter = findParameterByAlias(String.valueOf(e.getValue()));
                refreshViewNames(linkedParameter);
            }

            if ("processTemplate".equals(e.getProperty()) && e.getItem() != null) {
                applyVisibilityRulesForType(e.getItem().getType());
            }

            @SuppressWarnings("unchecked")
            DatasourceImplementation<DataSet> implementation = (DatasourceImplementation<DataSet>) dataSetsDs;
            implementation.modified(e.getItem());
        });

        dataSetScriptField.resetEditHistory();

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

        viewNameLookup.setOptionsMap(new HashMap<>());
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
                    textParamsBox.add(dataStore);
                    textBox.add(processTemplate);
                case JPQL:
                    textParamsBox.add(dataStore);
                    textBox.add(processTemplate);
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
                    if (processTemplate.isChecked()) {
                        dataSetScriptField.setSuggester(null);
                    } else {
                        dataSetScriptField.setSuggester(this);
                    }
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
        textParamsBox.remove(dataStore);
        textBox.remove(processTemplate);
        editPane.remove(textBox);
        editPane.remove(entityGrid);
        editPane.remove(entitiesGrid);
        editPane.remove(commonEntityGrid);
        editPane.remove(spacer);
    }

    protected void selectFirstDataSet() {
        dataSetsDs.refresh();
        if (!dataSetsDs.getItemIds().isEmpty()) {
            DataSet item = dataSetsDs.getItem(dataSetsDs.getItemIds().iterator().next());
            dataSets.setSelected(item);
        } else {
            dataSets.setSelected((DataSet) null);
        }
    }

    // For EditViewAction
    @Override
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