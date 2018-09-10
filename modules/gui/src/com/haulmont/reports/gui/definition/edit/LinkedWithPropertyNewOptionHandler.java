/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.definition.edit;

import com.haulmont.cuba.gui.data.Datasource;

import java.util.function.Consumer;

/**
 */
class LinkedWithPropertyNewOptionHandler implements Consumer<String> {
    protected Datasource datasource;
    protected String fieldName;

    public static LinkedWithPropertyNewOptionHandler handler(Datasource datasource, String fieldName) {
        return new LinkedWithPropertyNewOptionHandler(datasource, fieldName);
    }

    public LinkedWithPropertyNewOptionHandler(Datasource datasource, String fieldName) {
        this.datasource = datasource;
        this.fieldName = fieldName;
    }

    @Override
    public void accept(String caption) {
        datasource.getItem().setValue(fieldName, caption);
    }
}
