/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.gui.definition.edit

import com.haulmont.cuba.core.entity.Entity
import com.haulmont.cuba.core.global.PersistenceHelper
import com.haulmont.cuba.gui.autocomplete.AutoCompleteSupport
import com.haulmont.cuba.gui.autocomplete.JpqlSuggestionFactory
import com.haulmont.cuba.gui.autocomplete.Suggester
import com.haulmont.cuba.gui.autocomplete.Suggestion
import com.haulmont.cuba.gui.components.actions.CreateAction
import com.haulmont.cuba.gui.components.actions.RemoveAction
import com.haulmont.cuba.gui.data.CollectionDatasource
import com.haulmont.cuba.gui.data.Datasource
import com.haulmont.cuba.gui.data.ValueListener
import com.haulmont.cuba.gui.data.impl.DatasourceImpl
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter
import com.haulmont.reports.entity.BandDefinition
import com.haulmont.reports.entity.DataSet
import com.haulmont.reports.entity.DataSetType
import com.haulmont.reports.entity.Orientation
import javax.inject.Inject
import com.haulmont.cuba.gui.components.*

/**
 * @author degtyarjov
 * @version $Id$
 */
public class BandDefinitionEditor extends AbstractEditor implements Suggester {

    @Inject
    private Datasource<BandDefinition> bandDefinitionDs

    @Override
    protected void initItem(Entity item) {
        BandDefinition definition = (BandDefinition) item
        if (PersistenceHelper.isNew(item))
            definition.orientation = Orientation.HORIZONTAL
    }

    @Override
    protected void postInit() {
        selectFirstDataset()
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        Table table = getComponent('dataSets')
        table.addAction(new RemoveAction(table, false))

        table.addAction(new CreateAction(table) {
            @Override
            void actionPerform(Component component) {
                DataSet dataset = new DataSet()
                dataset.bandDefinition = (BandDefinition) BandDefinitionEditor.this.getItem()
                dataset.name = dataset.bandDefinition.name ?: 'dataset'
                dataset.type = DataSetType.GROOVY

                dataset.entityParamName = 'entity'
                dataset.listEntitiesParamName = 'entities'

                table.datasource.addItem(dataset)
            }
        })

        initDataSetControls()
    }

    @Override
    void commitAndClose() {
        if (commit()) {
            CollectionDatasource<UUID, BandDefinition> parentDs = ((DatasourceImpl) bandDefinitionDs).getParent()
            BandDefinition definition = parentDs.getItem(bandDefinitionDs.item.id)

            BandDefinition parentDefinition = definition.parentBandDefinition
            if (parentDefinition) {
                if (PersistenceHelper.isNew(definition)) {
                    if (parentDefinition.childrenBandDefinitions == null)
                        parentDefinition.childrenBandDefinitions = new ArrayList<BandDefinition>()

                    if (!parentDefinition.childrenBandDefinitions.contains(definition))
                        parentDefinition.childrenBandDefinitions.add(definition)
                }
            }
            close(COMMIT_ACTION_ID)
        }
    }

    def initDataSetControls() {
        LookupField lookupField = getComponent('type')
        TextField queryTextField = getComponent('text')
        TextField nameField = getComponent('datasetName')
        Label queryLabel = getComponent('dataSet_text')

        Label entityParamLabel = getComponent('entityParamLabel')
        TextField entityParamTextBox = getComponent('entityParamTextBox')

        Label entitiesParamLabel = getComponent('entitiesParamLabel')
        TextField entitiesParamTextBox = getComponent('entitiesParamTextBox')

        def queryEditors = [
                queryLabel, queryTextField
        ]

        def entityParamEditors = [
                entityParamLabel, entityParamTextBox
        ]

        def entitiesParamEditors = [
                entitiesParamLabel, entitiesParamTextBox
        ]

        def allParams = [
                queryLabel, queryTextField,
                entityParamLabel, entityParamTextBox,
                entitiesParamLabel, entitiesParamTextBox
        ]

        lookupField.addListener(
                new ValueListener() {
                    @Override
                    void valueChanged(Object source, String property, Object prevValue, Object value) {
                        // Hide all editors for dataset
                        allParams.each { Component c -> c.visible = false }

                        DataSetType dsType = (DataSetType) value;
                        switch (dsType) {
                            case DataSetType.SQL:
                            case DataSetType.JPQL:
                            case DataSetType.GROOVY:
                                queryEditors.each { Component c -> c.visible = true }
//                                queryTextField.setSuggester(DataSetType.JPQL.equals(value) ? BandDefinitionEditor.this : null)
                                break

                            case DataSetType.SINGLE:
                                entityParamEditors.each { Component c -> c.visible = true }
                                break

                            case DataSetType.MULTI:
                                entitiesParamEditors.each { Component c -> c.visible = true }
                                break
                        }
                    }
                }
        )

        allParams.each { Component c -> c.visible = false }
        queryEditors.each { Component c -> c.visible = true }

        def enableDatasetControls = {
            boolean value ->
            [lookupField, queryTextField, nameField].each {it.enabled = value}
        }

        Table datasets = getComponent('dataSets')
        CollectionDatasource ds = datasets.datasource
        ds.addListener(
                new DsListenerAdapter<DataSet>() {
                    @Override
                    void itemChanged(Datasource<DataSet> datasetDs, DataSet prevItem, DataSet item) {
                        enableDatasetControls(item != null)
                    }
                }
        )

        enableDatasetControls(false)
    }

    def selectFirstDataset() {
        Table datasets = getComponent('dataSets')
        CollectionDatasource ds = datasets.datasource
        ds.refresh()
        if (!ds.itemIds.empty) {
            def item = ds.getItem(ds.itemIds.iterator().next())
            def set = new HashSet()
            set.add(item)
            datasets.setSelected(set)
        }
    }

    @Override
    java.util.List<Suggestion> getSuggestions(AutoCompleteSupport source, String text, int cursorPosition) {
        String query = (String) source.getValue()
        if (query == null || "".equals(query.trim())) {
            return Collections.emptyList()
        }
        int queryPosition = cursorPosition - 1
        return JpqlSuggestionFactory.requestHint(query, queryPosition, source, cursorPosition)
    }
}
