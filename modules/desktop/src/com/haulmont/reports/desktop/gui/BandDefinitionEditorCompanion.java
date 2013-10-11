/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.desktop.gui;

import com.haulmont.chile.core.model.utils.InstanceUtils;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.desktop.gui.components.DesktopLookupField;
import com.haulmont.cuba.desktop.gui.components.DesktopTextField;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.reports.entity.BandDefinition;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.reports.entity.DataSetType;
import com.haulmont.reports.gui.definition.edit.BandDefinitionEditor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author devyatkin
 * @version $Id$
 */
public class BandDefinitionEditorCompanion implements BandDefinitionEditor.Companion {
    @Override
    public void initDatasetsTable(Table table) {
        final Messages messages = AppBeans.get(Messages.NAME);
        final ComponentsFactory factory = AppBeans.get(ComponentsFactory.NAME);
        table.addGeneratedColumn("name", new Table.ColumnGenerator() {
            @Override
            public Component generateCell(Entity entity) {
                TextField nameField = factory.createComponent(TextField.NAME);
                final DataSet dataset = (DataSet) entity;
                nameField.setValue(dataset.getName());
                nameField.addListener(new ValueListener() {
                    @Override
                    public void valueChanged(Object source, String property, Object prevValue, Object value) {
                        InstanceUtils.setValueEx(dataset, "name", value);
                    }
                });
                return nameField;
            }
        });

        table.addGeneratedColumn("type", new Table.ColumnGenerator() {
            @Override
            public Component generateCell(Entity entity) {
                LookupField lookupField = factory.createComponent(LookupField.NAME);
                final DataSet dataset = (DataSet) entity;
                Map<String, Object> options = new TreeMap<>();
                for (DataSetType type : DataSetType.values()) {
                    options.put(messages.getMessage(type), type);
                }
                lookupField.setOptionsMap(options);
                lookupField.setValue(dataset.getType());
                lookupField.addListener(new ValueListener() {
                    @Override
                    public void valueChanged(Object source, String property, Object prevValue, Object value) {
                        InstanceUtils.setValueEx(dataset, "type", value);
                    }
                });
                lookupField.setRequired(true);
                return lookupField;
            }
        });
    }
}
