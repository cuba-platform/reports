/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.reports.desktop.gui;

import com.haulmont.chile.core.model.utils.InstanceUtils;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.reports.entity.DataSetType;
import com.haulmont.reports.gui.definition.edit.BandDefinitionEditor;

import java.util.Map;
import java.util.TreeMap;

public class BandDefinitionEditorCompanion implements BandDefinitionEditor.Companion {
    @Override
    public void initDatasetsTable(Table table) {
        final Messages messages = AppBeans.get(Messages.NAME);
        final ComponentsFactory factory = AppBeans.get(ComponentsFactory.NAME);
        table.addGeneratedColumn("name", entity -> {
            TextField nameField = factory.createComponent(TextField.class);
            final DataSet dataset = (DataSet) entity;
            nameField.setValue(dataset.getName());
            nameField.addValueChangeListener(e -> InstanceUtils.setValueEx(dataset, "name", e.getValue()));
            return nameField;
        });

        table.addGeneratedColumn("type", entity -> {
            LookupField lookupField = factory.createComponent(LookupField.class);
            final DataSet dataset = (DataSet) entity;
            Map<String, Object> options = new TreeMap<>();
            for (DataSetType type : DataSetType.values()) {
                options.put(messages.getMessage(type), type);
            }
            lookupField.setOptionsMap(options);
            lookupField.setValue(dataset.getType());
            lookupField.addValueChangeListener(e -> InstanceUtils.setValueEx(dataset, "type", e.getValue()));

            lookupField.setRequired(true);
            return lookupField;
        });
    }
}