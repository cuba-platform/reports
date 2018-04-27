/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.template.edit.pivottable.aggregation.edit;

import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.reports.entity.pivottable.PivotTableProperty;

public class PivotTableAggregationEdit extends AbstractEditor<PivotTableProperty> {
    @Override
    public boolean commit() {
        return true;
    }
}
