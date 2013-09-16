/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.actions;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.AppConfig;
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
    private final String name;

    private final static String DEFAULT_ACTION_ID = "editorPrintForm";

    public EditorPrintFormAction(Window.Editor editor, String name) {
        this(DEFAULT_ACTION_ID, editor, name);
    }

    public EditorPrintFormAction(String captionId, Window.Editor editor, @Nullable final String name) {
        super(captionId);
        this.editor = editor;
        this.name = name;
    }

    @Override
    public void actionPerform(Component component) {
        final Entity entity = editor.getItem();
        if (entity != null) {
            final String javaClassName = entity.getClass().getCanonicalName();
            openRunReportScreen(editor, entity, javaClassName, name);
        } else
            editor.showNotification(AppBeans.get(Messages.class).getMessage(ReportGuiManager.class, "notifications.noSelectedEntity"),
                    IFrame.NotificationType.HUMANIZED);

    }

    @Override
    public String getCaption() {
        final String messagesPackage = AppConfig.getMessagesPack();
        return AppBeans.get(Messages.class).getMessage(messagesPackage, "actions.Report");
    }
}