/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.gui.definition.edit;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.gui.autocomplete.AutoCompleteSupport;
import com.haulmont.cuba.gui.autocomplete.JpqlSuggestionFactory;
import com.haulmont.cuba.gui.autocomplete.Suggester;
import com.haulmont.cuba.gui.autocomplete.Suggestion;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.reports.entity.BandDefinition;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.reports.entity.DataSetType;
import com.haulmont.reports.entity.Orientation;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class BandDefinitionEditor extends AbstractEditor implements Suggester {

    @Inject
    private Datasource<BandDefinition> bandDefinitionDs;

    @Inject
    private CollectionDatasource<DataSet, UUID> dataSetsDs;

    @Inject
    protected Table dataSets;

    @Named("text")
    protected SourceCodeEditor datasetScriptField;

    @Named("textBox")
    BoxLayout textBox;

    @Named("entityBox")
    BoxLayout entityBox;

    @Named("entitiesBox")
    BoxLayout entitiesBox;

    @Override
    protected void initItem(Entity item) {
        BandDefinition definition = (BandDefinition) item;
        if (PersistenceHelper.isNew(item))
            definition.setOrientation(Orientation.HORIZONTAL);
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
            public String getCaption() {
                return "";
            }
        });

        dataSets.addAction(new AbstractAction("create") {

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
                }
            }
        });

        initDataSetControls();
    }

    public void initDataSetControls() {
        dataSetsDs.addListener(new DsListenerAdapter<DataSet>() {
            @Override
            public void valueChanged(DataSet source, String property, @Nullable Object prevValue, @Nullable Object value) {
                if (property.equals("type")) {
                    applyType((DataSetType) value);
                }
            }

            @Override
            public void itemChanged(Datasource ds, @Nullable DataSet prevItem, @Nullable DataSet item) {
                if (item != null) {
                    applyType(item.getType());
                }
            }
        });

        textBox.setVisible(false);
        entityBox.setVisible(false);
        entitiesBox.setVisible(false);
        dataSetsDs.addListener(
                new DsListenerAdapter<DataSet>() {
                    @Override
                    public void itemChanged(Datasource<DataSet> datasetDs, DataSet prevItem, DataSet item) {
                        List<Field> fields = Arrays.<Field>asList(datasetScriptField);
                        for (Field field : fields) {
                            field.setEnabled(item != null);
                        }
                    }
                }
        );
    }

    private void applyType(DataSetType dsType) {
        textBox.setVisible(false);
        entityBox.setVisible(false);
        entitiesBox.setVisible(false);

        if (dsType != null) {
            switch (dsType) {
                case SQL:
                case JPQL:
                case GROOVY:
                    textBox.setVisible(true);
                    break;
                case SINGLE:
                    entityBox.setVisible(true);
                    break;
                case MULTI:
                    entitiesBox.setVisible(true);
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

    void selectFirstDataset() {
        dataSetsDs.refresh();
        if (!dataSetsDs.getItemIds().isEmpty()) {
            Entity item = dataSetsDs.getItem(dataSetsDs.getItemIds().iterator().next());
            dataSets.setSelected(item);
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
    }

    public Datasource<BandDefinition> getBandDefinitionDs() {
        return bandDefinitionDs;
    }
}