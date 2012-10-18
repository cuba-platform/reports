/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.web.exception;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.web.exception.AbstractExceptionHandler;
import com.haulmont.reports.exception.FailedToConnectToOpenOfficeException;
import com.haulmont.reports.exception.ReportingException;
import com.haulmont.cuba.web.App;
import com.haulmont.reports.exception.UnsupportedFormatException;
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
                FailedToConnectToOpenOfficeException.class.getName(),
                UnsupportedFormatException.class.getName()
        );
    }

    @Override
    protected void doHandle(App app, String className, String message, @Nullable Throwable throwable) {
        String messageCode = "reportException.message";
        if (FailedToConnectToOpenOfficeException.class.getName().equals(className)) {
            messageCode = "reportException.failedConnectToOffice";
        } else if (UnsupportedFormatException.class.getName().equals(className)) {
            messageCode = "reportException.unsupportedFileFormat";
        }
        String msg = AppBeans.get(Messages.class).getMessage(getClass(), messageCode);
        app.getAppWindow().showNotification(msg, Window.Notification.TYPE_ERROR_MESSAGE);
    }
}
