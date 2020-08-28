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

package com.haulmont.reports.gui.actions;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.data.BindingState;
import com.haulmont.cuba.gui.components.data.meta.ContainerDataUnit;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.model.*;
import com.haulmont.cuba.gui.screen.ScreenContext;
import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.gui.ReportGuiManager;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
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
        this.caption = messages.getMessage(ListPrintFormAction.class, "actions.ListPrintForm");
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
            return container instanceof HasLoader && unit.getState() == BindingState.ACTIVE && !container.getItems().isEmpty();
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
        Window window = ComponentsHelper.getWindowNN(listComponent);
        openRunReportScreen(window.getFrameOwner(), selected, metaClass);
    }

    protected void printAll() {
        MetaClass metaClass;
        LoadContext loadContext;

        if (listComponent.getItems() instanceof ContainerDataUnit) {
            ContainerDataUnit unit = (ContainerDataUnit) listComponent.getItems();
            CollectionContainer container = unit.getContainer();
            if (container instanceof CollectionPropertyContainer) {
                // as CollectionPropertyContainer does not have loader it always fetches all records,
                // so print these records as selected
                printSelected(new HashSet(container.getMutableItems()));
                return;
            }
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
        parameterPrototype.setCondition(query.getCondition());
        parameterPrototype.setSort(query.getSort());

        if (!Strings.isNullOrEmpty(loadContext.getView().getName())) {
            parameterPrototype.setViewName(loadContext.getView().getName());
        } else {
            parameterPrototype.setView(View.copy(loadContext.getView()));
        }

        Window window = ComponentsHelper.getWindowNN(listComponent);
        openRunReportScreen(window.getFrameOwner(), parameterPrototype, metaClass);
    }
}