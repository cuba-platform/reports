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
package com.haulmont.reports.web.exception;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.Frame;
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
 */
public class ReportExceptionHandler extends AbstractExceptionHandler {

    public ReportExceptionHandler() {
        super(
                ReportingException.class.getName(),
                NoOpenOfficeFreePortsException.class.getName(),
                FailedToConnectToOpenOfficeException.class.getName(),
                UnsupportedFormatException.class.getName(),
                FailedToLoadTemplateClassException.class.getName(),
                ValidationException.class.getName()
        );
    }

    @Override
    protected void doHandle(App app, String className, String message, @Nullable Throwable throwable) {
        Messages messages = AppBeans.get(Messages.class);

        if (FailedToConnectToOpenOfficeException.class.getName().equals(className)) {
            String msg = messages.getMessage(getClass(), "reportException.failedConnectToOffice");
            app.getWindowManager().showNotification(msg, Frame.NotificationType.ERROR);
        } else if (NoOpenOfficeFreePortsException.class.getName().equals(className)) {
            String msg = messages.getMessage(getClass(), "reportException.noOpenOfficeFreePorts");
            app.getWindowManager().showNotification(msg, Frame.NotificationType.ERROR);
        } else if (ValidationException.class.getName().equals(className)) {
            app.getWindowManager().showNotification(message, Frame.NotificationType.ERROR);
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