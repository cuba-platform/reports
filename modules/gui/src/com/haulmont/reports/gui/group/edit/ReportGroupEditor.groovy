/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports.gui.group.edit

import com.haulmont.cuba.gui.components.AbstractEditor

/**
 * @author artamonov
 * @version
 */
class ReportGroupEditor extends AbstractEditor {

    @Override
    void init(Map<String, Object> params) {
        super.init(params)

        getDialogParams().setResizable(false)
        getDialogParams().setWidth(400)
    }
}