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

package com.haulmont.reports.gui.actions.list;

import com.google.common.collect.ImmutableMap;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.BeanLocator;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.ScreenBuilders;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.ActionType;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.actions.ListAction;
import com.haulmont.cuba.gui.components.data.DataUnit;
import com.haulmont.cuba.gui.components.data.meta.EntityDataUnit;
import com.haulmont.cuba.gui.icons.CubaIcon;
import com.haulmont.cuba.gui.icons.Icons;
import com.haulmont.cuba.gui.meta.StudioAction;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.gui.ReportGuiManager;
import com.haulmont.reports.gui.report.run.InputParametersFrame;
import com.haulmont.reports.gui.report.run.InputParametersWindow;
import com.haulmont.reports.gui.report.run.ReportRun;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Standard action for running the reports associated with current screen or list component.
 * <p>
 * Should be defined in the screen that is associated with {@link Report}. Should be defined for a {@code Button}
 * or a list component ({@code Table}, {@code DataGrid}, etc.).
 */
@StudioAction(category = "Reports list actions", description = "Runs the reports associated with current screen or list component")
@ActionType(RunReportAction.ID)
public class RunReportAction extends ListAction implements Action.HasBeforeActionPerformedHandler {

    public static final String ID = "runReport";
    public static final String DEFAULT_SINGLE_ENTITY_ALIAS = "entity";
    public static final String DEFAULT_LIST_OF_ENTITIES_ALIAS = "entities";

    protected BeanLocator beanLocator;
    protected ScreenBuilders screenBuilders;

    protected BeforeActionPerformedHandler beforeActionPerformedHandler;

    public RunReportAction() {
        this(ID);
    }

    public RunReportAction(String id) {
        super(id);
    }

    @Inject
    public void setIcons(Icons icons) {
        this.icon = icons.get(CubaIcon.PRINT);
    }

    @Inject
    public void setMessages(Messages messages) {
        this.caption = messages.getMessage(RunReportAction.class, "actions.RunReport");
    }

    @Inject
    public void setBeanLocator(BeanLocator beanLocator) {
        this.beanLocator = beanLocator;
    }

    @Inject
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Override
    public BeforeActionPerformedHandler getBeforeActionPerformedHandler() {
        return beforeActionPerformedHandler;
    }

    @Override
    public void setBeforeActionPerformedHandler(BeforeActionPerformedHandler handler) {
        beforeActionPerformedHandler = handler;
    }

    @Override
    public void actionPerform(Component component) {
        if (beforeActionPerformedHandler != null
                && !beforeActionPerformedHandler.beforeActionPerformed()) {
            return;
        }
        if (target != null && target.getFrame() != null) {
            MetaClass metaClass = null;
            DataUnit items = target.getItems();
            if (items instanceof EntityDataUnit) {
                metaClass = ((EntityDataUnit) items).getEntityMetaClass();
            }

            openLookup(target.getFrame().getFrameOwner(), metaClass);
        } else if (component instanceof Component.BelongToFrame) {
            FrameOwner screen = ComponentsHelper.getWindowNN((Component.BelongToFrame) component).getFrameOwner();
            openLookup(screen, null);
        } else {
            throw new IllegalStateException("No target screen or component found for 'RunReportAction'");
        }
    }

    protected void openLookup(FrameOwner screen, MetaClass metaClass) {
        Screen hostScreen;
        if (screen instanceof Screen) {
            hostScreen = (Screen) screen;
        } else {
            hostScreen = UiControllerUtils.getHostScreen((ScreenFragment) screen);
        }

        screenBuilders.lookup(Report.class, screen)
                .withScreenId("report$Report.run")
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(ParamsMap.of(
                        ReportRun.SCREEN_PARAMETER, hostScreen.getId(),
                        ReportRun.META_CLASS_PARAMETER, metaClass)))
                .withSelectHandler(reports -> runReports(reports, screen))
                .show();
    }

    protected void runReports(Collection<Report> reports, FrameOwner screen) {
        if (reports != null && reports.size() > 0) {
            Report report = reports.iterator().next();

            DataManager dataManager = beanLocator.get(DataManager.NAME);
            report = dataManager.reload(report, "report.edit");

            ReportGuiManager reportGuiManager = beanLocator.get(ReportGuiManager.class);
            if (report.getInputParameters() != null
                    && report.getInputParameters().size() > 0
                    || reportGuiManager.inputParametersRequiredByTemplates(report)) {
                openReportParamsDialog(report, screen);
            } else {
                reportGuiManager.printReport(report, Collections.emptyMap(), screen);
            }
        }
    }

    protected void openReportParamsDialog(Report report, FrameOwner screen) {
        Map<String, Object> selectedItems = null;
        if (target != null) {
            Set items = target.getSelected();
            if (!items.isEmpty()) {
                selectedItems = ImmutableMap.of(
                        DEFAULT_LIST_OF_ENTITIES_ALIAS, items,
                        DEFAULT_SINGLE_ENTITY_ALIAS, items.stream().findFirst().get());
            }
        }

        screenBuilders.screen(screen)
                .withScreenId("report$inputParameters")
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(ParamsMap.of(
                        InputParametersWindow.REPORT_PARAMETER, report,
                        InputParametersFrame.PARAMETERS_PARAMETER, selectedItems)))
                .show();
    }
}
