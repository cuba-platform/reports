/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.actions;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.gui.ReportGuiManager;
import org.apache.commons.collections.CollectionUtils;

import java.util.Set;

/**
 * @author artamonov
 * @version $Id$
 */
public class TablePrintFormAction extends AbstractPrintFormAction {
    private final Window window;
    private final Table table;

    private final static String DEFAULT_ACTION_ID = "tablePrintForm";

    protected Messages messages;

    public TablePrintFormAction(Window window, Table table, final boolean multiObjects) {
        this(DEFAULT_ACTION_ID, window, table, multiObjects);
    }

    public TablePrintFormAction(String captionId, final Window window,
                                final Table table, final boolean multiObjects) {
        super(captionId);
        this.window = window;
        this.table = table;

        messages = AppBeans.get(Messages.class);
    }

    @Override
    public void actionPerform(Component component) {
        final Set selected = table.getSelected();

        Action cancelAction = new DialogAction(DialogAction.Type.CANCEL);

        if (CollectionUtils.isNotEmpty(selected)) {
            Action printSelectedAction = new AbstractAction("actions.printSelected") {
                @Override
                public void actionPerform(Component component) {
                    printSelected(selected);
                }

                @Override
                public String getIcon() {
                    return "icons/reports-print-row.png";
                }
            };

            Action printAllAction = new AbstractAction("actions.printAll") {
                @Override
                public void actionPerform(Component component) {
                    printAll();
                }

                @Override
                public String getIcon() {
                    return "icons/reports-print-all.png";
                }
            };

            Action[] actions;
            if (selected.size() > 1) {
                actions = new Action[]{printAllAction, printSelectedAction, cancelAction};

                window.showOptionDialog(messages.getMessage(ReportGuiManager.class, "notifications.confirmPrintSelectedheader"),
                        messages.getMessage(ReportGuiManager.class, "notifications.confirmPrintSelected"),
                        IFrame.MessageType.CONFIRMATION,
                        actions);
            } else {
                printSelected(selected);
            }
        } else {
            if ((table.getDatasource().getState() == Datasource.State.VALID) && (table.getDatasource().getItemIds().size() > 0)) {
                Action yesAction = new DialogAction(DialogAction.Type.OK) {
                    @Override
                    public void actionPerform(Component component) {
                        printAll();
                    }
                };

                window.showOptionDialog(messages.getMessage(getClass(), "notifications.confirmPrintAllheader"),
                        messages.getMessage(getClass(), "notifications.confirmPrintAll"),
                        IFrame.MessageType.CONFIRMATION, new Action[]{yesAction, cancelAction});
            } else {
                window.showNotification(messages.getMessage(ReportGuiManager.class, "notifications.noSelectedEntity"),
                        IFrame.NotificationType.HUMANIZED);
            }
        }
    }

    private void printSelected(Set selected) {
        Class<?> entityClass = selected.iterator().next().getClass();
        String javaClassName = entityClass.getCanonicalName();

        openRunReportScreen(window, selected, javaClassName);
    }

    private void printAll() {
        CollectionDatasource datasource = table.getDatasource();

        MetaClass metaClass = datasource.getMetaClass();
        String javaClassName = metaClass.getJavaClass().getCanonicalName();

        LoadContext loadContext = datasource.getCompiledLoadContext();

        ParameterPrototype parameterPrototype = new ParameterPrototype("");//todo
        parameterPrototype.setMetaClassName(metaClass.getFullName());
        parameterPrototype.setQueryString(loadContext.getQuery().getQueryString());
        parameterPrototype.setQueryParams(loadContext.getQuery().getParameters());
        parameterPrototype.setViewName(loadContext.getView().getName());
        parameterPrototype.setUseSecurityConstraints(loadContext.isUseSecurityConstraints());

        openRunReportScreen(window, parameterPrototype, javaClassName);
    }

    @Override
    public String getCaption() {
        final String messagesPackage = AppConfig.getMessagesPack();
        return messages.getMessage(messagesPackage, "actions.Report");
    }
}