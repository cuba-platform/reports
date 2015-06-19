/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.web.report.template;

import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.reports.gui.template.edit.ChartEditFrameController;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class WebChartEditControllerCompanion implements ChartEditFrameController.Companion {
    @Override
    public void setWindowWidth(Window window, int width) {
        if (window != null) {
            Component component = WebComponentsHelper.unwrap(window);
            while ( !(component.getParent() instanceof com.vaadin.ui.Window || component.getParent() == null)) {
                component = component.getParent();
            }

            if (component.getParent() != null) {
                com.vaadin.ui.Window vaadinWindow = (com.vaadin.ui.Window) component.getParent();
                vaadinWindow.setWidth(width, Sizeable.Unit.PIXELS);
                vaadinWindow.center();
            }
        }
    }

    @Override
    public void center(Window window) {
        if (window != null) {
            Component component = WebComponentsHelper.unwrap(window);
            while ( !(component.getParent() instanceof com.vaadin.ui.Window || component.getParent() == null)) {
                component = component.getParent();
            }

            if (component.getParent() != null) {
                com.vaadin.ui.Window vaadinWindow = (com.vaadin.ui.Window) component.getParent();
                vaadinWindow.center();
            }
        }
    }
}
