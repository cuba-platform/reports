/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.template.edit.pivottable.aggregation.edit;

import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.ValidationErrors;
import com.haulmont.reports.entity.pivottable.PivotTableAggregation;

import java.util.Collection;
import java.util.Objects;

public class PivotTableAggregationEdit extends AbstractEditor<PivotTableAggregation> {

    @WindowParam
    protected Collection<PivotTableAggregation> existingItems;

    @Override
    protected boolean preCommit() {
        if (super.preCommit()) {
            PivotTableAggregation aggregation = getItem();
            boolean hasMatches = existingItems.stream().
                    anyMatch(e -> !Objects.equals(aggregation, e) &&
                            Objects.equals(aggregation.getCaption(), e.getCaption()));
            if (hasMatches) {
                ValidationErrors validationErrors = new ValidationErrors();
                validationErrors.add(getMessage("pivotTableEdit.uniqueAggregationOptionCaption"));
                showValidationErrors(validationErrors);
                return false;
            }
            return true;
        }
        return false;
    }

    public void getFunctionHelp() {
        showMessageDialog(getMessage("pivotTable.functionHelpCaption"), getMessage("pivotTable.aggregationFunctionHelp"),
                MessageType.CONFIRMATION_HTML
                        .modal(false)
                        .width(560f));
    }

    @Override
    public boolean commit() {
        return true;
    }
}
