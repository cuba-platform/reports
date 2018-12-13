/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.actions;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.screen.EditorScreen;
import com.haulmont.cuba.gui.screen.FrameOwner;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.UiControllerUtils;
import com.haulmont.reports.gui.ReportGuiManager;

import javax.annotation.Nullable;

public class EditorPrintFormAction extends AbstractPrintFormAction {

    protected final EditorScreen editor;
    protected final String reportOutputName;

    public EditorPrintFormAction(EditorScreen editor, @Nullable String reportOutputName) {
        this("editorReport", editor, reportOutputName);
    }

    public EditorPrintFormAction(String id, EditorScreen editor, @Nullable String reportOutputName) {
        super(id);

        Messages messages = AppBeans.get(Messages.NAME);

        this.editor = editor;
        this.caption = messages.getMessage(getClass(), "actions.Report");
        this.reportOutputName = reportOutputName;
        this.icon = "icons/reports-print.png";
    }

    @Override
    public void actionPerform(Component component) {
        if (beforeActionPerformedHandler != null) {
            if (!beforeActionPerformedHandler.beforeActionPerformed())
                return;
        }
        Entity entity = editor.getEditedEntity();
        if (entity != null) {
            MetaClass metaClass = entity.getMetaClass();
            openRunReportScreen((Screen) editor, entity, metaClass, reportOutputName);
        } else {
            Messages messages = AppBeans.get(Messages.NAME);

            Notifications notifications = UiControllerUtils.getScreenContext((FrameOwner) editor).getNotifications();

            notifications.create()
                    .withCaption(messages.getMessage(ReportGuiManager.class, "notifications.noSelectedEntity"))
                    .show();
        }
    }
}