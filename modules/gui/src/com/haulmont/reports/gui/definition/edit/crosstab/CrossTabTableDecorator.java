/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.definition.edit.crosstab;

import com.google.common.base.Strings;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.reports.entity.BandDefinition;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.reports.entity.Orientation;
import com.haulmont.reports.gui.definition.edit.BandDefinitionEditor;
import com.haulmont.reports.util.DataSetFactory;

import javax.inject.Inject;
import java.util.Objects;
import java.util.UUID;

import static com.haulmont.cuba.gui.data.Datasource.State.VALID;

/**
 * Class presents decorator been for add some extra behavior on report band orientation change
 *
 * @see BandDefinitionEditor#initDataSetListeners()
 */
public class CrossTabTableDecorator {

    protected static final String HORIZONTAL_TPL = "dynamic_header";
    protected static final String VERTICAL_TPL = "master_data";

    @Inject
    protected DataSetFactory dataSetFactory;

    @Inject
    protected ComponentsFactory componentsFactory;

    public void decorate(Table<DataSet> dataSets, final Datasource<BandDefinition> bandDefinitionDs) {
        dataSets.addGeneratedColumn("name", entity -> {
            TextField textField = componentsFactory.createComponent(TextField.class);
            textField.setParent(dataSets);
            textField.setWidthFull();
            textField.setHeightAuto();
            textField.setValue(entity.getName());
            textField.setDatasource(dataSets.getItemDatasource(entity), "name");

            if (bandDefinitionDs.getItem() != null) {
                if (Orientation.CROSS == bandDefinitionDs.getItem().getOrientation() &&
                        !Strings.isNullOrEmpty(entity.getName()) &&
                        (entity.getName().endsWith(HORIZONTAL_TPL) || entity.getName().endsWith(VERTICAL_TPL))) {
                    textField.setEditable(false);
                }
            }
            return textField;
        });

        bandDefinitionDs.addItemChangeListener(band -> {
            if (VALID == dataSets.getDatasource().getState()) {
                onTableReady(dataSets, bandDefinitionDs);
            } else {
                dataSets.getDatasource().addStateChangeListener(new Datasource.StateChangeListener<DataSet>() {
                    @Override
                    public void stateChanged(Datasource.StateChangeEvent<DataSet> e) {
                        if (VALID == e.getState()) {
                            onTableReady(dataSets, bandDefinitionDs);
                            dataSets.getDatasource().removeStateChangeListener(this);
                        }
                    }
                });
            }
        });
    }

    protected void onHorizontalSetChange(DataSet dataSet) {
        dataSet.setName(String.format("%s_" + HORIZONTAL_TPL, dataSet.getBandDefinition().getName()));
    }

    protected void onVerticalSetChange(DataSet dataSet) {
        dataSet.setName(String.format("%s_" + VERTICAL_TPL, dataSet.getBandDefinition().getName()));
    }


    protected void onTableReady(Table<DataSet> dataSets, Datasource<BandDefinition> bandDefinitionDs) {
        CollectionDatasource<DataSet, UUID> dataSetsDs = dataSets.getDatasource();

        initCrossDatasets(dataSetsDs, bandDefinitionDs);
    }

    protected void initCrossDatasets(CollectionDatasource<DataSet, UUID> dataSetsDs,
                                     Datasource<BandDefinition> bandDefinitionDs) {
        if (bandDefinitionDs.getItem() == null) {
            return;
        }

        DataSet horizontal = null;
        DataSet vertical = null;

        for (DataSet dataSet : dataSetsDs.getItems()) {
            if (horizontal == null && !Strings.isNullOrEmpty(dataSet.getName()) && dataSet.getName().endsWith(HORIZONTAL_TPL)) {
                horizontal = dataSet;
            }

            if (vertical == null && !Strings.isNullOrEmpty(dataSet.getName()) && dataSet.getName().endsWith(VERTICAL_TPL)) {
                vertical = dataSet;
            }

            if (horizontal != null && vertical != null) break;
        }

        if (horizontal == null) {
            horizontal = dataSetFactory.createEmptyDataSet(bandDefinitionDs.getItem());
            onHorizontalSetChange(horizontal);
        }

        if (vertical == null) {
            vertical = dataSetFactory.createEmptyDataSet(bandDefinitionDs.getItem());
            onVerticalSetChange(vertical);
        }

        initListeners(dataSetsDs, bandDefinitionDs, horizontal, vertical);
    }

    protected void initListeners(CollectionDatasource<DataSet, UUID> dataSetsDs,
                                 Datasource<BandDefinition> bandDefinitionDs,
                                 DataSet horizontal, DataSet vertical) {
        bandDefinitionDs.addItemPropertyChangeListener(e -> {
            if ("orientation".equals(e.getProperty())) {
                Orientation orientation = (Orientation) e.getValue();
                Orientation prevOrientation = (Orientation) e.getPrevValue();
                if (orientation == prevOrientation) return;

                if (Orientation.CROSS == orientation || Orientation.CROSS == prevOrientation) {
                    onOrientationChange(dataSetsDs, bandDefinitionDs);
                }

                if (Orientation.CROSS == orientation) {
                    dataSetsDs.addItem(horizontal);
                    dataSetsDs.addItem(vertical);
                }
            }

            if (bandDefinitionDs.getItem().getOrientation() == Orientation.CROSS && "name".equals(e.getProperty())) {
                onHorizontalSetChange(horizontal);
                onVerticalSetChange(vertical);
            }
        });
    }

    protected void onOrientationChange(CollectionDatasource<DataSet, UUID> dataSetsDs, Datasource<BandDefinition> bandDefinitionDs) {
        dataSetsDs.getItemIds().stream()
                .map(dataSetsDs::getItem)
                .filter(Objects::nonNull)
                .forEach(dataSetsDs::removeItem);
        dataSetsDs.addItem(dataSetFactory.createEmptyDataSet(bandDefinitionDs.getItem()));
    }
}
