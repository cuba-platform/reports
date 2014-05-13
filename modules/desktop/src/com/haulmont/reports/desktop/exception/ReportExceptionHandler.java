/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.desktop.exception;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.desktop.App;
import com.haulmont.cuba.desktop.exception.AbstractExceptionHandler;
import com.haulmont.cuba.desktop.sys.DialogWindow;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.reports.exception.*;
import com.haulmont.yarg.formatters.impl.doc.connector.NoFreePortsException;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
                NoOpenOfficeFreePortsException.class.getName(),
                FailedToConnectToOpenOfficeException.class.getName(),
                UnsupportedFormatException.class.getName(),
                FailedToLoadTemplateClassException.class.getName()
        );
    }

    @Override
    protected void doHandle(Thread thread, String className, String message, @Nullable Throwable throwable) {
        Messages messages = AppBeans.get(Messages.class);

        if (FailedToConnectToOpenOfficeException.class.getName().equals(className)) {
            String msg = messages.getMessage(getClass(), "reportException.failedConnectToOffice");
            App.getInstance().getMainFrame().getWindowManager().showNotification(msg, IFrame.NotificationType.ERROR);
        } if (NoOpenOfficeFreePortsException.class.getName().equals(className)) {
            String msg = messages.getMessage(getClass(), "reportException.noOpenOfficeFreePorts");
            App.getInstance().getMainFrame().getWindowManager().showNotification(msg, IFrame.NotificationType.ERROR);
        } else {
            JXErrorPane errorPane = new JXErrorPane();
            ErrorInfo errorInfo = new ErrorInfo(
                    messages.getMessage(getClass(), "reportException.message"), message,
                    null, null, throwable, null, null);
            errorPane.setErrorInfo(errorInfo);
            JDialog dialog = JXErrorPane.createDialog(App.getInstance().getMainFrame(), errorPane);
            dialog.setMinimumSize(new Dimension(600, (int) dialog.getMinimumSize().getHeight()));

            final DialogWindow lastDialogWindow = getLastDialogWindow();
            dialog.addWindowListener(
                    new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            if (lastDialogWindow != null)
                                lastDialogWindow.enableWindow();
                            else
                                App.getInstance().getMainFrame().activate();
                        }
                    }
            );
            dialog.setModal(false);

            if (lastDialogWindow != null)
                lastDialogWindow.disableWindow(null);
            else
                App.getInstance().getMainFrame().deactivate(null);

            dialog.setVisible(true);
        }
   }

    private DialogWindow getLastDialogWindow() {
        try {
            return App.getInstance().getMainFrame().getWindowManager().getLastDialogWindow();
        } catch (Exception e) {
            // this may happen in case of initialization error
            return null;
        }
    }
}