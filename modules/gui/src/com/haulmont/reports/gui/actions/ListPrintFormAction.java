/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.actions;

import com.google.common.base.Preconditions;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.data.BindingState;
import com.haulmont.cuba.gui.components.data.meta.ContainerDataUnit;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.model.CollectionLoader;
import com.haulmont.cuba.gui.model.HasLoader;
import com.haulmont.cuba.gui.model.InstanceContainer;
import com.haulmont.cuba.gui.screen.ScreenContext;
import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.gui.ReportGuiManager;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Set;

public class ListPrintFormAction extends AbstractPrintFormAction {

    protected ListComponent listComponent;

    public ListPrintFormAction(ListComponent listComponent) {
        this("listComponentReport", listComponent);
    }

    public ListPrintFormAction(String id, ListComponent listComponent) {
        super(id);

        this.listComponent = listComponent;
        Messages messages = AppBeans.get(Messages.NAME);
        this.caption = messages.getMessage(ListPrintFormAction.class, "actions.Report");
        this.icon = "icons/reports-print.png";
    }

    @Override
    public void actionPerform(Component component) {
        DialogAction cancelAction = new DialogAction(DialogAction.Type.CANCEL);

        ScreenContext screenContext = ComponentsHelper.getScreenContext(listComponent);
        Preconditions.checkState(screenContext != null, "Component is not attached to window");

        if (beforeActionPerformedHandler != null) {
            if (!beforeActionPerformedHandler.beforeActionPerformed())
                return;
        }

        WindowManager wm = (WindowManager) screenContext.getScreens();
        Set selected = listComponent.getSelected();
        if (CollectionUtils.isNotEmpty(selected)) {
            if (selected.size() > 1) {
                Action printSelectedAction = new AbstractAction("actions.printSelected", Status.PRIMARY) {
                    @Override
                    public void actionPerform(Component component) {
                        printSelected(selected);
                    }
                };
                printSelectedAction.setIcon("icons/reports-print-row.png");

                Action printAllAction = new AbstractAction("actions.printAll") {
                    @Override
                    public void actionPerform(Component component) {
                        printAll();
                    }
                };
                printAllAction.setIcon("icons/reports-print-all.png");

                Messages messages = AppBeans.get(Messages.NAME);
                wm.showOptionDialog(messages.getMessage(ReportGuiManager.class, "notifications.confirmPrintSelectedheader"),
                        messages.getMessage(ReportGuiManager.class, "notifications.confirmPrintSelected"),
                        Frame.MessageType.CONFIRMATION,
                        new Action[]{printAllAction, printSelectedAction, cancelAction});
            } else {
                printSelected(selected);
            }
        } else {
            Messages messages = AppBeans.get(Messages.NAME);
            if (isDataAvailable()) {
                Action yesAction = new DialogAction(DialogAction.Type.OK) {
                    @Override
                    public void actionPerform(Component component) {
                        printAll();
                    }
                };

                cancelAction.setPrimary(true);

                wm.showOptionDialog(messages.getMessage(ListPrintFormAction.class, "notifications.confirmPrintAllheader"),
                        messages.getMessage(ListPrintFormAction.class, "notifications.confirmPrintAll"),
                        Frame.MessageType.CONFIRMATION, new Action[]{yesAction, cancelAction});
            } else {
                wm.showNotification(messages.getMessage(ReportGuiManager.class, "notifications.noSelectedEntity"),
                        Frame.NotificationType.HUMANIZED);
            }

        }
    }

    protected boolean isDataAvailable() {
        if (listComponent.getItems() instanceof ContainerDataUnit) {
            ContainerDataUnit unit = (ContainerDataUnit) listComponent.getItems();
            CollectionContainer container = unit.getContainer();
            return container instanceof HasLoader && unit.getState() == BindingState.ACTIVE && container.getItems().size() > 0;
        } else {
            CollectionDatasource ds = listComponent.getDatasource();
            if (ds != null)
                return ds.getState() == Datasource.State.VALID && ds.size() > 0;
        }
        return false;
    }

    protected void printSelected(Set selected) {
        MetaClass metaClass;
        if (listComponent.getItems() instanceof ContainerDataUnit) {
            ContainerDataUnit unit = (ContainerDataUnit) listComponent.getItems();
            InstanceContainer container = unit.getContainer();
            metaClass = container.getEntityMetaClass();
        } else {
            CollectionDatasource ds = listComponent.getDatasource();
            metaClass = ds.getMetaClass();
        }
        openRunReportScreen(ComponentsHelper.getWindow(listComponent), selected, metaClass);
    }

    protected void printAll() {


        MetaClass metaClass;
        LoadContext loadContext;

        if (listComponent.getItems() instanceof ContainerDataUnit) {
            ContainerDataUnit unit = (ContainerDataUnit) listComponent.getItems();
            CollectionContainer container = unit.getContainer();
            CollectionLoader loader = (CollectionLoader) ((HasLoader) unit.getContainer()).getLoader();
            metaClass = container.getEntityMetaClass();
            loadContext = loader.createLoadContext();
        } else {
            CollectionDatasource ds = listComponent.getDatasource();
            metaClass = ds.getMetaClass();
            loadContext = ds.getCompiledLoadContext();
        }

        ParameterPrototype parameterPrototype = new ParameterPrototype(metaClass.getName());
        parameterPrototype.setMetaClassName(metaClass.getName());
        LoadContext.Query query = loadContext.getQuery();
        parameterPrototype.setQueryString(query.getQueryString());
        parameterPrototype.setQueryParams(query.getParameters());
        parameterPrototype.setViewName(loadContext.getView().getName());
        openRunReportScreen(ComponentsHelper.getWindow(listComponent), parameterPrototype, metaClass);
    }
}

