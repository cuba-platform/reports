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

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.gui.ReportGuiManager;
import com.haulmont.reports.gui.report.run.ReportRun;

import java.util.Collections;

import static com.google.common.base.Preconditions.checkArgument;

public class RunReportAction extends AbstractAction implements Action.HasBeforeActionPerformedHandler {

    protected FrameOwner screen;

    protected ReportGuiManager reportGuiManager = AppBeans.get(ReportGuiManager.class);

    protected BeforeActionPerformedHandler beforeActionPerformedHandler;

    /**
     * @deprecated Use {@link RunReportAction#RunReportAction()} instead
     * */
    @Deprecated
    public RunReportAction(FrameOwner screen) {
        this("runReport", screen);
    }

    /**
     * @deprecated Use {@link RunReportAction#RunReportAction(String)} instead
     * */
    @Deprecated
    public RunReportAction(String id, FrameOwner screen) {
        super(id);

        checkArgument(screen != null, "Can not create RunReportAction with null window");

        this.screen = screen;
        Messages messages = AppBeans.get(Messages.NAME);
        this.caption = messages.getMessage(getClass(), "actions.Report");
        this.icon = "icons/reports-print.png";
    }

    public RunReportAction() {
        this("runReport");
    }

    public RunReportAction(String id) {
        super(id);

        Messages messages = AppBeans.get(Messages.NAME);
        this.caption = messages.getMessage(getClass(), "actions.Report");
        this.icon = "icons/reports-print.png";
    }

    @Override
    public void actionPerform(Component component) {
        if (beforeActionPerformedHandler != null) {
            if (!beforeActionPerformedHandler.beforeActionPerformed())
                return;
        }
        if (screen != null) {
            openLookup(screen);
        } else if (component instanceof Component.BelongToFrame) {
            FrameOwner screen = ComponentsHelper.getWindowNN((Component.BelongToFrame) component).getFrameOwner();
            openLookup(screen);
        } else {
            throw new IllegalStateException("Please set window or specified component for performAction call");
        }
    }

    protected void openLookup(FrameOwner screen) {
        ScreenContext screenContext = UiControllerUtils.getScreenContext(screen);

        WindowManager wm = (WindowManager) screenContext.getScreens();
        WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo("report$Report.run");

        Screen hostScreen;
        if (screen instanceof Screen) {
            hostScreen = (Screen) screen;
        } else {
            hostScreen = UiControllerUtils.getHostScreen((ScreenFragment) screen);
        }

        wm.openLookup(windowInfo, items -> {
            if (items != null && items.size() > 0) {
                Report report = (Report) items.iterator().next();

                if (screen instanceof LegacyFrame) {
                    DataSupplier dataSupplier = ((LegacyFrame) screen).getDsContext().getDataSupplier();
                    report = dataSupplier.reload(report, "report.edit");
                } else {
                    DataManager dataManager = AppBeans.get(DataManager.NAME);
                    report = dataManager.reload(report, "report.edit");
                }

                if (report.getInputParameters() != null && report.getInputParameters().size() > 0
                        || reportGuiManager.inputParametersRequiredByTemplates(report)) {
                    openReportParamsDialog(report, screen);
                } else {
                    reportGuiManager.printReport(report, Collections.emptyMap(), screen);
                }
            }
        }, OpenType.DIALOG, ParamsMap.of(ReportRun.SCREEN_PARAMETER, hostScreen.getId()));
    }

    protected void openReportParamsDialog(Report report, FrameOwner screen) {
        ScreenContext screenContext = UiControllerUtils.getScreenContext(screen);

        WindowManager wm = (WindowManager) screenContext.getScreens();
        WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo("report$inputParameters");

        wm.openWindow(windowInfo, OpenType.DIALOG, ParamsMap.of("report", report));
    }

    public void setScreen(FrameOwner screen) {
        this.screen = screen;
    }

    /**
     * @deprecated Use {@link #setScreen(FrameOwner)} instead.
     */
    @Deprecated
    public void setWindow(FrameOwner screen) {
        this.screen = screen;
    }

    @Override
    public BeforeActionPerformedHandler getBeforeActionPerformedHandler() {
        return beforeActionPerformedHandler;
    }

    @Override
    public void setBeforeActionPerformedHandler(BeforeActionPerformedHandler handler) {
        beforeActionPerformedHandler = handler;
    }
}