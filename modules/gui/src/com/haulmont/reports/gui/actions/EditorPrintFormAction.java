/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.actions;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.reports.gui.ReportGuiManager;

import javax.annotation.Nullable;

public class EditorPrintFormAction extends AbstractPrintFormAction {

    protected final Window.Editor editor;
    protected final String reportOutputName;

    public EditorPrintFormAction(Window.Editor editor, @Nullable String reportOutputName) {
        this("editorReport", editor, reportOutputName);
    }

    public EditorPrintFormAction(String id, Window.Editor editor, @Nullable String reportOutputName) {
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
        final Entity entity = editor.getItem();
        if (entity != null) {
            MetaClass metaClass = entity.getMetaClass();
            openRunReportScreen(editor, entity, metaClass, reportOutputName);
        } else {
            Messages messages = AppBeans.get(Messages.NAME);

            editor.showNotification(
                    messages.getMessage(ReportGuiManager.class, "notifications.noSelectedEntity"),
                    Frame.NotificationType.HUMANIZED);
        }
    }
}