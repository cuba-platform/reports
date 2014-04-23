/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.report.wizard.step;

import com.haulmont.cuba.gui.components.*;

/**
 * @author fedorchenko
 * @version $Id$
 */
public interface MainWizardFrame<T extends AbstractEditor> {

    String getMessage(String key) ;

    String formatMessage(String key, Object... params);

    T getMainEditorFrame();

    Button getBackwardBtn();

    Button getForwardBtn();
}
