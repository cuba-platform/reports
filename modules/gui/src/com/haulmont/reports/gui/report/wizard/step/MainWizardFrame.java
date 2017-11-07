/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.report.wizard.step;

import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.Button;

public interface MainWizardFrame<T extends AbstractWindow> {

    String getMessage(String key);

    String formatMessage(String key, Object... params);

    T getMainWizardFrame();

    Button getBackwardBtn();

    Button getForwardBtn();

    void removeBtns();

    void addForwardBtn();

    void addBackwardBtn();

    void addSaveBtn();
}
