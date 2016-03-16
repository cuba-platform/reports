/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.web.report.template;

import com.haulmont.cuba.gui.components.Window;
import com.haulmont.reports.gui.template.edit.ChartEditFrameController;
import com.haulmont.reports.web.WindowCompanionHelper;

/**
 */
public class WebChartEditControllerCompanion implements ChartEditFrameController.Companion {
    @Override
    public void setWindowWidth(Window window, int width) {
        WindowCompanionHelper.setWindowWidth(window, width);
    }

    @Override
    public void setWindowHeight(Window window, int height) {
        WindowCompanionHelper.setWindowHeight(window, height);
    }

    @Override
    public void center(Window window) {
        WindowCompanionHelper.center(window);
    }

    @Override
    public void setWindowResizable(Window window, boolean resizable) {
        WindowCompanionHelper.setWindowResizable(window, resizable);
    }
}
