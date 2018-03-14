/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.report.edit.tabs;

import com.haulmont.cuba.gui.components.AbstractFrame;

public class LocalesFrame extends AbstractFrame {

    public void getLocaleTextHelp() {
        showMessageDialog(getMessage("localeText"), getMessage("report.localeTextHelp"),
                MessageType.CONFIRMATION_HTML
                        .modal(false)
                        .width(560f));
    }
}
