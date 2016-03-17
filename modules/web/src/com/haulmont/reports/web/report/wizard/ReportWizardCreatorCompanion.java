/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.web.report.wizard;

import com.haulmont.cuba.gui.components.Window;
import com.haulmont.reports.gui.report.wizard.ReportWizardCreator;
import com.haulmont.reports.web.WindowCompanionHelper;

/**
 */
public class ReportWizardCreatorCompanion implements ReportWizardCreator.Companion {
    @Override
    public void setWindowHeight(Window window, int height) {
        window.getDialogOptions().setHeight(height);
    }

    @Override
    public void center(Window window) {
        WindowCompanionHelper.center(window);
    }
}