/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.actions;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.gui.ReportGuiManager;
import com.haulmont.reports.gui.report.run.ReportRun;

import java.util.Collections;

import static com.google.common.base.Preconditions.checkArgument;

public class RunReportAction extends AbstractAction implements Action.HasBeforeActionPerformedHandler {

    protected Frame window;

    protected ReportGuiManager reportGuiManager = AppBeans.get(ReportGuiManager.class);

    protected BeforeActionPerformedHandler beforeActionPerformedHandler;

    /**
     * @deprecated Use {@link RunReportAction#RunReportAction()} instead
     * */
    @Deprecated
    public RunReportAction(Frame window) {
        this("runReport", window);
    }

    /**
     * @deprecated Use {@link RunReportAction#RunReportAction(String)} instead
     * */
    @Deprecated
    public RunReportAction(String id, Frame window) {
        super(id);

        checkArgument(window != null, "Can not create RunReportAction with null window");

        this.window = window;
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
        if (window != null) {
            openLookup(window);
        } else if (component instanceof Component.BelongToFrame) {
            Window window = ComponentsHelper.getWindow((Component.BelongToFrame) component);
            openLookup(window);
        } else {
            throw new IllegalStateException("Please set window or specified component for performAction call");
        }
    }

    protected void openLookup(Frame window) {
        WindowManager wm = (WindowManager) window.getFrameOwner().getScreenContext().getScreens();
        WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo("report$Report.run");

        wm.openLookup(windowInfo, items -> {
            if (items != null && items.size() > 0) {
                Report report = (Report) items.iterator().next();

                if (window.getFrameOwner() instanceof LegacyFrame) {
                    DataSupplier dataSupplier = ((LegacyFrame) window.getFrameOwner()).getDsContext().getDataSupplier();
                    report = dataSupplier.reload(report, "report.edit");
                } else {
                    DataManager dataManager = AppBeans.get(DataManager.NAME);
                    report = dataManager.reload(report, "report.edit");
                }

                if (report.getInputParameters() != null && report.getInputParameters().size() > 0
                        || reportGuiManager.inputParametersRequiredByTemplates(report)) {
                    openReportParamsDialog(report, window);
                } else {
                    reportGuiManager.printReport(report, Collections.emptyMap(), window);
                }
            }
        }, OpenType.DIALOG, ParamsMap.of(ReportRun.SCREEN_PARAMETER, window.getId()));
    }

    protected void openReportParamsDialog(Report report, Frame window) {
        WindowManager wm = (WindowManager) window.getFrameOwner().getScreenContext().getScreens();
        WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo("report$inputParameters");

        wm.openWindow(windowInfo, OpenType.DIALOG, ParamsMap.of("report", report));
    }

    public void setWindow(Frame window) {
        this.window = window;
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