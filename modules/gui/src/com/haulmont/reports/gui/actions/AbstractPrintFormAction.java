/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.actions;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.exception.ReportingException;
import com.haulmont.reports.gui.ReportGuiManager;
import com.haulmont.reports.gui.report.run.ReportRun;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author artamonov
 * @version $Id$
 */
public abstract class AbstractPrintFormAction extends AbstractAction {
    protected ReportGuiManager reportGuiManager = AppBeans.get(ReportGuiManager.class);

    protected AbstractPrintFormAction(String id) {
        super(id);
    }

    protected void openRunReportScreen(final Window window, final Object selectedValue, MetaClass inputValueMetaClass) {
        openRunReportScreen(window, selectedValue, inputValueMetaClass, null);
    }

    protected void openRunReportScreen(final Window window, final Object selectedValue, final MetaClass inputValueMetaClass,
                                       @Nullable final String outputFileName) {
        User user = AppBeans.get(UserSessionSource.class).getUserSession().getUser();
        boolean selectListReportsOnly = selectedValue instanceof ParameterPrototype;
        List<Report> reports = reportGuiManager.getAvailableReports(window.getId(), user, inputValueMetaClass, selectListReportsOnly);

        if (reports.size() > 1) {
            Map<String, Object> params = Collections.<String, Object>singletonMap(ReportRun.REPORTS_PARAMETER, reports);

            window.openLookup("report$Report.run", new Window.Lookup.Handler() {

                @Override
                public void handleLookup(Collection items) {
                    if (CollectionUtils.isNotEmpty(items)) {
                        Report report = (Report) items.iterator().next();
                        ReportInputParameter parameter = getParameterAlias(report, inputValueMetaClass);
                        if (selectedValue instanceof ParameterPrototype) {
                            ((ParameterPrototype) selectedValue).setParamName(parameter.getAlias());
                        }
                        reportGuiManager.runReport(report, window, parameter, selectedValue, null, outputFileName);
                    }
                }
            }, WindowManager.OpenType.DIALOG, params);
        } else if (reports.size() == 1) {
            Report report = reports.get(0);
            ReportInputParameter parameter = getParameterAlias(report, inputValueMetaClass);
            if (selectedValue instanceof ParameterPrototype) {
                ((ParameterPrototype) selectedValue).setParamName(parameter.getAlias());
            }
            reportGuiManager.runReport(report, window, parameter, selectedValue, null, outputFileName);
        } else {
            window.showNotification(messages.getMessage(ReportGuiManager.class, "report.notFoundReports"),
                    IFrame.NotificationType.HUMANIZED);
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
}