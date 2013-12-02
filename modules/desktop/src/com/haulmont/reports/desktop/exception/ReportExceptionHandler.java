/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.desktop.exception;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.desktop.App;
import com.haulmont.cuba.desktop.exception.AbstractExceptionHandler;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.reports.exception.FailedToConnectToOpenOfficeException;
import com.haulmont.reports.exception.FailedToLoadTemplateClassException;
import com.haulmont.reports.exception.ReportingException;
import com.haulmont.reports.exception.UnsupportedFormatException;

import javax.annotation.Nullable;

/**
 * Handles reporting exceptions.
 *
 * @author devyatkin
 * @version $Id$
 */
public class ReportExceptionHandler extends AbstractExceptionHandler {

    public ReportExceptionHandler() {
        super(
                ReportingException.class.getName(),
                FailedToConnectToOpenOfficeException.class.getName(),
                UnsupportedFormatException.class.getName(),
                FailedToLoadTemplateClassException.class.getName()
        );
    }

    @Override
    protected void doHandle(Thread thread, String className, String message, @Nullable Throwable throwable) {
        String messageCode = "reportException.message";
        if (FailedToConnectToOpenOfficeException.class.getName().equals(className)) {
            messageCode = "reportException.failedConnectToOffice";
        } else if (UnsupportedFormatException.class.getName().equals(className)) {
            messageCode = "reportException.unsupportedFileFormat";
        } else if (FailedToLoadTemplateClassException.class.getName().equals(className)) {
            messageCode = "reportException.failedToLoadTemplateClass";
        }
        String msg = AppBeans.get(Messages.class).getMessage(getClass(), messageCode);
        App.getInstance().getMainFrame().showNotification(msg, IFrame.NotificationType.ERROR);
    }
}