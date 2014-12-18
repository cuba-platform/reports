/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.definition.edit;

import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.data.Datasource;

/**
 * @author degtyarjov
 * @version $Id$
 */
class LinkedWithPropertyNewOptionHandler implements LookupField.NewOptionHandler {
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
    public void addNewOption(String caption) {
        datasource.getItem().setValue(fieldName, caption);
    }
}
