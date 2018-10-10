/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports.gui.definition.edit;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Stores;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.WindowManager;
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
import com.haulmont.reports.gui.ReportingClientConfig;
import com.haulmont.reports.gui.definition.edit.crosstab.CrossTabTableDecorator;
import com.haulmont.reports.gui.definition.edit.scripteditordialog.ScriptEditorDialog;
import com.haulmont.reports.util.DataSetFactory;
import org.apache.commons.lang3.StringUtils;

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
    protected SourceCodeEditor jsonGroovyCodeEditor;
    @Inject
    protected LinkButton jsonSourceGroovyCodeHelp;
    @Named("textHelp")
    protected LinkButton dataSetHelpField;
    @Inject
    protected BoxLayout textBox;
    @Inject
    protected Label entitiesParamLabel;
    @Inject
    protected Label entityParamLabel;
    @Inject
    protected GridLayout commonEntityGrid;
    @Inject
    protected LookupField jsonSourceTypeField;
    @Inject
    protected VBoxLayout jsonDataSetTypeVBox;
    @Inject
    protected Label jsonPathQueryLabel;
    @Inject
    protected HBoxLayout jsonPathQueryHBox;
    @Inject
    protected VBoxLayout jsonSourceGroovyCodeVBox;
    @Inject
    protected VBoxLayout jsonSourceURLVBox;
    @Inject
    protected VBoxLayout jsonSourceParameterCodeVBox;
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
    @Inject
    protected DataSetFactory dataSetFactory;
    @Inject
    protected CrossTabTableDecorator tabOrientationTableDecorator;
    @Inject
    protected Configuration configuration;

    protected SourceCodeEditor.Mode dataSetScriptFieldMode = SourceCodeEditor.Mode.Text;

    public interface Companion {
        void initDatasetsTable(Table table);
    }

    public void showJsonScriptEditorDialog() {
        ScriptEditorDialog editorDialog = (ScriptEditorDialog) openWindow(
                "scriptEditorDialog",
                WindowManager.OpenType.DIALOG,
                ParamsMap.of(
                        "scriptValue", jsonGroovyCodeEditor.getValue(),
                        "helpVisible", jsonSourceGroovyCodeHelp.isVisible(),
                        "helpMsgKey", "dataSet.jsonSourceGroovyCodeHelp"
                ));
        editorDialog.addCloseListener(actionId -> {
            if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                jsonGroovyCodeEditor.setValue(editorDialog.getValue());
            }
        });
    }

    public void showDataSetScriptEditorDialog() {
        ScriptEditorDialog editorDialog = (ScriptEditorDialog) openWindow(
                "scriptEditorDialog",
                WindowManager.OpenType.DIALOG,
                ParamsMap.of(
                        "mode", dataSetScriptFieldMode,
                        "suggester", dataSetScriptField.getSuggester(),
                        "scriptValue", dataSetScriptField.getValue(),
                        "helpVisible", dataSetHelpField.isVisible(),
                        "helpMsgKey", "dataSet.textHelp"
                ));
        editorDialog.addCloseListener(actionId -> {
            if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                dataSetScriptField.setValue(editorDialog.getValue());
            }
        });
    }

    public void setBandDefinition(BandDefinition bandDefinition) {
        bandDefinitionDs.setItem(bandDefinition);
        name.setEditable(bandDefinition == null || bandDefinition.getParent() != null);
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

        initSourceCodeOptions();
    }

    protected void initSourceCodeOptions() {
        ReportingClientConfig config = configuration.getConfig(ReportingClientConfig.class);
        boolean enableTabSymbolInDataSetEditor = config.getEnableTabSymbolInDataSetEditor();
        jsonGroovyCodeEditor.setHandleTabKey(enableTabSymbolInDataSetEditor);
        dataSetScriptField.setHandleTabKey(enableTabSymbolInDataSetEditor);
    }

    protected void initJsonDataSetOptions(DataSet dataSet) {
        jsonDataSetTypeVBox.removeAll();
        jsonDataSetTypeVBox.add(jsonSourceTypeField);
        jsonDataSetTypeVBox.add(jsonPathQueryLabel);
        jsonDataSetTypeVBox.add(jsonPathQueryHBox);

        if (dataSet.getJsonSourceType() == null) {
            dataSet.setJsonSourceType(JsonSourceType.GROOVY_SCRIPT);
        }

        switch (dataSet.getJsonSourceType()) {
            case GROOVY_SCRIPT:
                jsonDataSetTypeVBox.add(jsonSourceGroovyCodeVBox);
                jsonDataSetTypeVBox.expand(jsonSourceGroovyCodeVBox);
                break;
            case URL:
                jsonDataSetTypeVBox.add(jsonSourceURLVBox);
                jsonDataSetTypeVBox.expand(jsonSourceURLVBox);
                break;
            case PARAMETER:
                jsonDataSetTypeVBox.add(jsonSourceParameterCodeVBox);
                jsonDataSetTypeVBox.add(spacer);
                jsonDataSetTypeVBox.expand(spacer);
                break;
        }
    }

    public void getTextHelp() {
        showMessageDialog(getMessage("dataSet.text"), getMessage("dataSet.textHelp"),
                MessageType.CONFIRMATION_HTML
                        .modal(false)
                        .width(700f));
    }

    public void getJsonSourceGroovyCodeHelp() {
        showMessageDialog(getMessage("dataSet.text"), getMessage("dataSet.jsonSourceGroovyCodeHelp"),
                MessageType.CONFIRMATION_HTML
                        .modal(false)
                        .width(700f));
    }

    public void getJsonPathQueryHelp() {
        showMessageDialog(getMessage("dataSet.text"), getMessage("dataSet.jsonPathQueryHelp"),
                MessageType.CONFIRMATION_HTML
                        .modal(false)
                        .width(700f));
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
                BandDefinition selectedBand = bandDefinitionDs.getItem();
                if (selectedBand != null) {
                    DataSet dataset = dataSetFactory.createEmptyDataSet(selectedBand);
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
        tabOrientationTableDecorator.decorate(dataSets, bandDefinitionDs);

        dataSetsDs.addItemChangeListener(e -> {
            if (e.getItem() != null) {
                applyVisibilityRules(e.getItem());

                if (e.getItem().getType() == DataSetType.SINGLE) {
                    refreshViewNames(findParameterByAlias(e.getItem().getEntityParamName()));
                } else if (e.getItem().getType() == DataSetType.MULTI) {
                    refreshViewNames(findParameterByAlias(e.getItem().getListEntitiesParamName()));
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
                applyVisibilityRulesForType(e.getItem());
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
        applyVisibilityRulesForType(item);
        if (item.getType() == DataSetType.SINGLE || item.getType() == DataSetType.MULTI) {
            applyVisibilityRulesForEntityType(item);
        }
    }

    protected void applyVisibilityRulesForType(DataSet dataSet) {
        hideAllDataSetEditComponents();

        if (dataSet.getType() != null) {
            switch (dataSet.getType()) {
                case SQL:
                case JPQL:
                    textParamsBox.add(dataStore);
                    textBox.add(processTemplate);
                case GROOVY:
                    editPane.add(textBox);
                    break;
                case SINGLE:
                    editPane.add(commonEntityGrid);
                    setCommonEntityGridVisiblity(true, false);
                    editPane.add(spacer);
                    editPane.expand(spacer);
                    break;
                case MULTI:
                    editPane.add(commonEntityGrid);
                    setCommonEntityGridVisiblity(false, true);
                    editPane.add(spacer);
                    editPane.expand(spacer);
                    break;
                case JSON:
                    initJsonDataSetOptions(dataSet);
                    editPane.add(jsonDataSetTypeVBox);
                    break;
            }

            switch (dataSet.getType()) {
                case SQL:
                    dataSetScriptFieldMode = SourceCodeEditor.Mode.SQL;
                    dataSetScriptField.setMode(SourceCodeEditor.Mode.SQL);
                    dataSetScriptField.setSuggester(null);
                    dataSetHelpField.setVisible(false);
                    break;

                case GROOVY:
                    dataSetScriptFieldMode = SourceCodeEditor.Mode.Groovy;
                    dataSetScriptField.setSuggester(null);
                    dataSetScriptField.setMode(SourceCodeEditor.Mode.Groovy);
                    dataSetHelpField.setVisible(true);
                    break;

                case JPQL:
                    dataSetScriptFieldMode = SourceCodeEditor.Mode.Text;
                    dataSetScriptField.setSuggester(processTemplate.isChecked() ? null : this);
                    dataSetScriptField.setMode(SourceCodeEditor.Mode.Text);
                    dataSetHelpField.setVisible(false);
                    break;

                default:
                    dataSetScriptFieldMode = SourceCodeEditor.Mode.Text;
                    dataSetScriptField.setSuggester(null);
                    dataSetScriptField.setMode(SourceCodeEditor.Mode.Text);
                    dataSetHelpField.setVisible(false);
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
        editPane.remove(commonEntityGrid);
        editPane.remove(jsonDataSetTypeVBox);
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

    protected void setCommonEntityGridVisiblity(boolean visibleEntityGrid, boolean visibleEntitiesGrid) {
        entityParamLabel.setVisible(visibleEntityGrid);
        entityParamLookup.setVisible(visibleEntityGrid);
        entitiesParamLabel.setVisible(visibleEntitiesGrid);
        entitiesParamLookup.setVisible(visibleEntitiesGrid);
    }
}