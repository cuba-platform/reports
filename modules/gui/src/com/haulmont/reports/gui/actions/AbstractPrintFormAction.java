/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.actions;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.screen.ScreenContext;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.exception.ReportingException;
import com.haulmont.reports.gui.ReportGuiManager;
import com.haulmont.reports.gui.report.run.ReportRun;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public abstract class AbstractPrintFormAction extends AbstractAction implements Action.HasBeforeActionPerformedHandler {

    protected ReportGuiManager reportGuiManager = AppBeans.get(ReportGuiManager.class);
    protected DataManager dataManager = AppBeans.get(DataManager.class);

    protected BeforeActionPerformedHandler beforeActionPerformedHandler;

    protected AbstractPrintFormAction(String id) {
        super(id);
    }

    protected void openRunReportScreen(Window window, final Object selectedValue, MetaClass inputValueMetaClass) {
        openRunReportScreen(window, selectedValue, inputValueMetaClass, null);
    }

    protected void openRunReportScreen(Window window, final Object selectedValue, final MetaClass inputValueMetaClass,
                                       @Nullable final String outputFileName) {
        User user = AppBeans.get(UserSessionSource.class).getUserSession().getUser();
        List<Report> reports = reportGuiManager.getAvailableReports(window.getId(), user, inputValueMetaClass);

        ScreenContext screenContext = ComponentsHelper.getScreenContext(window);

        if (reports.size() > 1) {
            Map<String, Object> params = ParamsMap.of(ReportRun.REPORTS_PARAMETER, reports);

            WindowManager wm = (WindowManager) screenContext.getScreens();
            WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo("report$Report.run");

            wm.openLookup(windowInfo, items -> {
                if (CollectionUtils.isNotEmpty(items)) {
                    Report report = (Report) items.iterator().next();
                    Report reloadedReport = reloadReport(report);
                    ReportInputParameter parameter = getParameterAlias(reloadedReport, inputValueMetaClass);
                    if (selectedValue instanceof ParameterPrototype) {
                        ((ParameterPrototype) selectedValue).setParamName(parameter.getAlias());
                    }
                    reportGuiManager.runReport(reloadedReport, window, parameter, selectedValue, null, outputFileName);
                }
            }, WindowManager.OpenType.DIALOG, params);
        } else if (reports.size() == 1) {
            Report report = reports.get(0);
            Report reloadedReport = reloadReport(report);
            ReportInputParameter parameter = getParameterAlias(reloadedReport, inputValueMetaClass);
            if (selectedValue instanceof ParameterPrototype) {
                ((ParameterPrototype) selectedValue).setParamName(parameter.getAlias());
            }
            reportGuiManager.runReport(reloadedReport, window, parameter, selectedValue, null, outputFileName);
        } else {
            Messages messages = AppBeans.get(Messages.NAME);

            WindowManager wm = (WindowManager) screenContext.getScreens();

            wm.showNotification(
                    messages.getMessage(ReportGuiManager.class, "report.notFoundReports"),
                    Frame.NotificationType.HUMANIZED);
        }
    }

    protected ReportInputParameter getParameterAlias(Report report, MetaClass inputValueMetaClass) {
        for (ReportInputParameter parameter : report.getInputParameters()) {
            if (reportGuiManager.parameterMatchesMetaClass(parameter, inputValueMetaClass)) {
                return parameter;
            }
        }

        throw new ReportingException(String.format("Selected report [%s] doesn't have parameter with class [%s].",
                report.getName(), inputValueMetaClass));
    }

    @Override
    public BeforeActionPerformedHandler getBeforeActionPerformedHandler() {
        return beforeActionPerformedHandler;
    }

    @Override
    public void setBeforeActionPerformedHandler(BeforeActionPerformedHandler handler) {
        beforeActionPerformedHandler = handler;
    }

    protected Report reloadReport(Report report) {
        View view = new View(Report.class)
                .addProperty("name")
                .addProperty("localeNames")
                .addProperty("description")
                .addProperty("templates")
                .addProperty("defaultTemplate")
                .addProperty("code")
                .addProperty("xml");
        return dataManager.reload(report, view);
    }
}