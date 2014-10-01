/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.actions;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.reports.gui.ReportGuiManager;

import javax.annotation.Nullable;

/**
 * @author artamonov
 * @author $Id$
 */
public class EditorPrintFormAction extends AbstractPrintFormAction {

    private final Window.Editor editor;
    private final String reportOutputName;

    public EditorPrintFormAction(Window.Editor editor, @Nullable String reportOutputName) {
        this("editorReport", editor, reportOutputName);
    }

    public EditorPrintFormAction(String id, Window.Editor editor, @Nullable String reportOutputName) {
        super(id);

        this.editor = editor;
        this.caption = messages.getMessage(getClass(), "actions.Report");
        this.reportOutputName = reportOutputName;
        this.icon = "icons/reports-print.png";
    }

    @Override
    public void actionPerform(Component component) {
        final Entity entity = editor.getItem();
        if (entity != null) {
            MetaClass metaClass = entity.getMetaClass();
            openRunReportScreen(editor, entity, metaClass, reportOutputName);
        } else {
            editor.showNotification(messages.getMessage(ReportGuiManager.class, "notifications.noSelectedEntity"),
                    IFrame.NotificationType.TRAY);
        }
    }
}