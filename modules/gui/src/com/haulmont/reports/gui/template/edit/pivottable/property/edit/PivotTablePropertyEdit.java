/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
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
