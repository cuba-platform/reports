/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.cuba.desktop.exception;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.desktop.App;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.report.exception.FailedToConnectToOpenOfficeException;
import com.haulmont.cuba.report.exception.ReportingException;
import com.haulmont.cuba.report.exception.UnsupportedFormatException;

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
                UnsupportedFormatException.class.getName()
        );
    }

    @Override
    protected void doHandle(Thread thread, String className, String message, @Nullable Throwable throwable) {
        String messageCode = "reportException.message";
        if (FailedToConnectToOpenOfficeException.class.getName().equals(className)) {
            messageCode = "reportException.failedConnectToOffice";
        } else if (UnsupportedFormatException.class.getName().equals(className)) {
            messageCode = "reportException.unsupportedFileFormat";
        }
        String msg = AppBeans.get(Messages.class).getMessage(getClass(), messageCode);
        App.getInstance().getMainFrame().showNotification(msg, IFrame.NotificationType.ERROR);
    }
}
