/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
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