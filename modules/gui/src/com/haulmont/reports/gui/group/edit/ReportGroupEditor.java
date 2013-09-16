/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.group.edit;

import com.haulmont.cuba.gui.components.AbstractEditor;

import java.util.Map;

/**
 * @author artamonov
 */
public class ReportGroupEditor extends AbstractEditor {

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        getDialogParams().setResizable(false);
        getDialogParams().setWidth(400);
    }
}