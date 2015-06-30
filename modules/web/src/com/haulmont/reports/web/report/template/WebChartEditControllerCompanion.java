/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.web.report.template;

import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.reports.gui.template.edit.ChartEditFrameController;
import com.haulmont.reports.web.CompanionHelper;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class WebChartEditControllerCompanion implements ChartEditFrameController.Companion {
    @Override
    public void setWindowWidth(Window window, int width) {
        CompanionHelper.setWindowWidth(window, width);
    }

    @Override
    public void center(Window window) {
        CompanionHelper.center(window);
    }
}
