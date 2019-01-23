/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.reports.desktop.exception;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.desktop.App;
import com.haulmont.cuba.desktop.TopLevelFrame;
import com.haulmont.cuba.desktop.exception.AbstractExceptionHandler;
import com.haulmont.cuba.desktop.sys.DialogWindow;
import com.haulmont.cuba.desktop.sys.JXErrorPaneExt;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.reports.exception.*;
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

        final App app = App.getInstance();
        final TopLevelFrame mainFrame = app.getMainFrame();

        if (FailedToConnectToOpenOfficeException.class.getName().equals(className)) {
            String msg = messages.getMessage(getClass(), "reportException.failedConnectToOffice");
            mainFrame.getWindowManager().showNotification(msg, Frame.NotificationType.ERROR);
        } else if (NoOpenOfficeFreePortsException.class.getName().equals(className)) {
            String msg = messages.getMessage(getClass(), "reportException.noOpenOfficeFreePorts");
            mainFrame.getWindowManager().showNotification(msg, com.haulmont.cuba.gui.components.Frame.NotificationType.ERROR);
        } else {
            JXErrorPane errorPane = new JXErrorPaneExt();
            ErrorInfo errorInfo = new ErrorInfo(
                    messages.getMessage(getClass(), "reportException.message"), message,
                    null, null, throwable, null, null);
            errorPane.setErrorInfo(errorInfo);
            JDialog dialog = JXErrorPane.createDialog(mainFrame, errorPane);
            dialog.setMinimumSize(new Dimension(600, (int) dialog.getMinimumSize().getHeight()));

            final DialogWindow lastDialogWindow = getLastDialogWindow(mainFrame);
            dialog.addWindowListener(
                    new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            if (lastDialogWindow != null)
                                lastDialogWindow.enableWindow();
                            else {
                                mainFrame.activate();
                            }
                        }
                    }
            );
            dialog.setModal(false);

            if (lastDialogWindow != null)
                lastDialogWindow.disableWindow(null);
            else
                mainFrame.deactivate(null);

            dialog.setVisible(true);
        }
    }

    protected DialogWindow getLastDialogWindow(TopLevelFrame mainFrame) {
        try {
            return mainFrame.getWindowManager().getLastDialogWindow();
        } catch (Exception e) {
            // this may happen in case of initialization error
            return null;
        }
    }
}