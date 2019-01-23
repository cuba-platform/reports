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
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.ScreenContext;
import com.haulmont.cuba.gui.screen.UiControllerUtils;
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

    protected void openRunReportScreen(Screen screen, Object selectedValue, MetaClass inputValueMetaClass) {
        openRunReportScreen(screen, selectedValue, inputValueMetaClass, null);
    }

    protected void openRunReportScreen(Screen screen, Object selectedValue, MetaClass inputValueMetaClass,
                                       @Nullable String outputFileName) {
        User user = AppBeans.get(UserSessionSource.class).getUserSession().getUser();
        List<Report> reports = reportGuiManager.getAvailableReports(screen.getId(), user, inputValueMetaClass);

        ScreenContext screenContext = UiControllerUtils.getScreenContext(screen);

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
                    reportGuiManager.runReport(reloadedReport, screen, parameter, selectedValue, null, outputFileName);
                }
            }, WindowManager.OpenType.DIALOG, params);
        } else if (reports.size() == 1) {
            Report report = reports.get(0);
            Report reloadedReport = reloadReport(report);
            ReportInputParameter parameter = getParameterAlias(reloadedReport, inputValueMetaClass);
            if (selectedValue instanceof ParameterPrototype) {
                ((ParameterPrototype) selectedValue).setParamName(parameter.getAlias());
            }
            reportGuiManager.runReport(reloadedReport, screen, parameter, selectedValue, null, outputFileName);
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