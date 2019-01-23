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

package com.haulmont.reports.gui.template.edit.pivottable.property.edit;

import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.FieldGroup;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.reports.entity.pivottable.PivotTableProperty;
import com.haulmont.reports.entity.pivottable.PivotTablePropertyType;

import javax.inject.Inject;

public class PivotTablePropertyEdit extends AbstractEditor<PivotTableProperty> {
    @Inject
    protected FieldGroup editGroup;
    @Inject
    protected Datasource<PivotTableProperty> propertyDs;

    @Override
    protected void postInit() {
        super.postInit();
        initFunctionField();
        propertyDs.addItemPropertyChangeListener(e -> {
            if ("type".equals(e.getProperty())) {
                initFunctionField();
            }
        });
    }

    protected void initFunctionField() {
        PivotTableProperty property = getItem();
        editGroup.getFieldNN("function")
                .setVisible(property.getType() == PivotTablePropertyType.DERIVED);
    }

    public void getFunctionHelp() {
        showMessageDialog(getMessage("pivotTable.functionHelpCaption"), getMessage("pivotTable.propertyFunctionHelp"),
                MessageType.CONFIRMATION_HTML
                        .modal(false)
                        .width(560f));
    }

    @Override
    public boolean commit() {
        return true;
    }
}
