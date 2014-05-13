/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.web.exception;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.AppUI;
import com.haulmont.cuba.web.exception.AbstractExceptionHandler;
import com.haulmont.cuba.web.exception.ExceptionDialog;
import com.haulmont.reports.exception.*;
import com.vaadin.ui.Window;

import javax.annotation.Nullable;

/**
 * Handles reporting exceptions.
 *
 * @author artamonov
 * @version $Id$
 */
public class ReportExceptionHandler extends AbstractExceptionHandler {

    public ReportExceptionHandler() {
        super(
                ReportingException.class.getName(),
                NoOpenOfficeFreePortsException.class.getName(),
                FailedToConnectToOpenOfficeException.class.getName(),
                UnsupportedFormatException.class.getName(),
                FailedToLoadTemplateClassException.class.getName()
        );
    }

    @Override
    protected void doHandle(App app, String className, String message, @Nullable Throwable throwable) {
        Messages messages = AppBeans.get(Messages.class);

        if (FailedToConnectToOpenOfficeException.class.getName().equals(className)) {
            String msg = messages.getMessage(getClass(), "reportException.failedConnectToOffice");
            app.getWindowManager().showNotification(msg, IFrame.NotificationType.ERROR);
        } if (NoOpenOfficeFreePortsException.class.getName().equals(className)) {
            String msg = messages.getMessage(getClass(), "reportException.noOpenOfficeFreePorts");
            app.getWindowManager().showNotification(msg, IFrame.NotificationType.ERROR);
        } else {
            ExceptionDialog dialog = new ExceptionDialog(
                    throwable,
                    messages.getMessage(getClass(), "reportException.message"),
                    message
            );
            for (Window window : AppUI.getCurrent().getWindows()) {
                if (window.isModal()) {
                    dialog.setModal(true);
                    break;
                }
            }
            app.getAppUI().addWindow(dialog);
            dialog.focus();
        }
    }
}