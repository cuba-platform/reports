/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.actions;

import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.screen.Screen;

public class TablePrintFormAction extends ListPrintFormAction {

    /**
     * @deprecated Use {@link TablePrintFormAction#TablePrintFormAction(Table)} instead.
     */
    @Deprecated
    public TablePrintFormAction(Screen screen, Table table) {
        this("tableReport", screen, table);
    }

    /**
     * @deprecated Use {@link TablePrintFormAction#TablePrintFormAction(String, Table)} instead.
     */
    @Deprecated
    public TablePrintFormAction(String id, Screen screen, Table table) {
        super(id, table);
    }

    public TablePrintFormAction(Table table) {
        this("tableReport", table);
    }

    public TablePrintFormAction(String id, Table table) {
        super(id, table);
    }
}