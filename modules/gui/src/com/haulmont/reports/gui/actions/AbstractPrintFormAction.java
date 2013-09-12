/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports.gui.actions;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.entity.*;
import com.haulmont.reports.exception.ReportingException;
import com.haulmont.reports.gui.ReportGuiManager;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author artamonov
 * @version $Id$
 */
abstract class AbstractPrintFormAction extends AbstractAction {
    protected ReportGuiManager reportGuiManager = AppBeans.get(ReportGuiManager.class);

    protected Messages messages = AppBeans.get(Messages.class);

    protected AbstractPrintFormAction(String id) {
        super(id);
    }

    protected void openRunReportScreen(final Window window, final Object selectedValue, String javaClassName) {
        openRunReportScreen(window, selectedValue, javaClassName, null);
    }

    protected void openRunReportScreen(final Window window, final Object selectedValue, String javaClassName, @Nullable final String outputFileName) {

        Map<String, Object> params = new HashMap<>();

        final MetaClass metaClass;
        try {
            Class<?> javaClass = Class.forName(javaClassName);
            metaClass = AppBeans.get(Metadata.class).getSession().getClass(javaClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        User user = AppBeans.get(UserSessionSource.class).getUserSession().getUser();
        boolean selectListReportsOnly = selectedValue instanceof ParameterPrototype;
        List<Report> reports = reportGuiManager.getAvailableReports(window.getId(), user, metaClass, selectListReportsOnly);

        params.put("entityMetaClass", metaClass.getName());
        params.put("screen", window.getId());
        params.put("reports", reports);

        if (reports.size() > 1) {
            window.openLookup("report$Report.run", new Window.Lookup.Handler() {

                @Override
                public void handleLookup(Collection items) {
                    if (CollectionUtils.isNotEmpty(items)) {
                        Report report = (Report) items.iterator().next();
                        ReportInputParameter parameter = getParameterAlias(report, metaClass.getName());
                        if (selectedValue instanceof ParameterPrototype) {
                            ((ParameterPrototype) selectedValue).setParamName(parameter.getAlias());
                        }
                        reportGuiManager.runReport(report, window, parameter, selectedValue, null, outputFileName);
                    }
                }
            }, WindowManager.OpenType.DIALOG, params);
        } else if (reports.size() == 1) {
            Report report = reports.get(0);
            ReportInputParameter parameter = getParameterAlias(report, metaClass.getName());
            if (selectedValue instanceof ParameterPrototype) {
                ((ParameterPrototype) selectedValue).setParamName(parameter.getAlias());
            }
            reportGuiManager.runReport(report, window, parameter, selectedValue, null, outputFileName);
        } else {
            window.showNotification(messages.getMessage(ReportGuiManager.class, "report.notFoundReports"), IFrame.NotificationType.HUMANIZED);
        }
    }

    protected ReportInputParameter getParameterAlias(Report report, String paramMetaClassName) {
        for (ReportInputParameter parameter : report.getInputParameters()) {
            if (parameter.getEntityMetaClass().equals(paramMetaClassName)) {
                return parameter;
            }
        }

        throw new ReportingException(String.format("Selected report [%s] doesn't have parameter with class [%s].", report.getName(), paramMetaClassName));
    }
}