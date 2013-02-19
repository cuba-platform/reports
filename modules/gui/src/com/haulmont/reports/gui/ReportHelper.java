/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.gui;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.RoleType;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.entity.UserRole;
import com.haulmont.reports.app.ReportOutputDocument;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.entity.ReportScreen;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $Id$
 */
@SuppressWarnings({"serial"})
public class ReportHelper {

    private ReportHelper() {
    }

    public static void runReport(Report report, Window window) {
        if (report != null) {
            if (report.getInputParameters() != null && report.getInputParameters().size() > 0) {
                openReportParamsDialog(report, window);
            } else {
                printReport(report, Collections.<String, Object>emptyMap());
            }
        }
    }

    private static void openReportParamsDialog(Report report, Window window) {
        window.openWindow("report$inputParameters", WindowManager.OpenType.DIALOG,
                Collections.<String, Object>singletonMap("report", report));
    }

    public static void runReport(Report report, Window window, final String paramAlias, final Object paramValue) {
        runReport(report, window, paramAlias, paramValue, null);
    }

    public static void runReport(Report report, Window window, final String paramAlias, final Object paramValue, @Nullable final String name) {
        if (report != null) {
            List<ReportInputParameter> params = report.getInputParameters();
            if (params != null && params.size() > 1) {
                openReportParamsDialog(report, window);
            } else {
                if (params != null && params.size() == 1) {
                    if (name == null)
                        printReport(report,
                                Collections.<String, Object>singletonMap(paramAlias, paramValue));
                    else
                        printReport(report, Collections.<String, Object>singletonMap(paramAlias, paramValue));
                } else {
                    if (name == null)
                        printReport(report, Collections.<String, Object>emptyMap());
                    else
                        printReport(report, Collections.<String, Object>emptyMap());
                }
            }
        }
    }

    public static void printReport(Report report, String defaultOutputFileName, Map<String, Object> params) {
        printReport(report, "", defaultOutputFileName, params);
    }

    public static void printReport(Report report, Map<String, Object> params) {
        printReport(report, report.getName(), params);
    }

    public static void printReport(Report report, String templateCode, String defaultOutputFileName, Map<String, Object> params) {
        try {
            if (StringUtils.isBlank(defaultOutputFileName))
                defaultOutputFileName = report.getName();

            ReportService srv = AppBeans.get(ReportService.NAME);

            ReportOutputDocument document;
            if (StringUtils.isEmpty(templateCode))
                document = srv.createReport(report, params);
            else
                document = srv.createReport(report, templateCode, params);

            byte[] byteArr = document.getContent();
            ExportFormat exportFormat = ReportPrintHelper.getExportFormat(document.getOutputType());

            ExportDisplay exportDisplay = AppConfig.createExportDisplay(null);
            String documentName = document.getDocumentName();
            exportDisplay.show(new ByteArrayDataProvider(byteArr), StringUtils.isNotBlank(documentName) ? documentName : defaultOutputFileName, exportFormat);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Report> checkRoles(User user, List<Report> reports) {
        List<Report> filter = new ArrayList<>();
        for (Report report : reports) {
            final List<Role> reportRoles = report.getRoles();
            if (reportRoles == null || reportRoles.size() == 0) {
                filter.add(report);
            } else {
                List<UserRole> userRoles = user.getUserRoles();
                Object requiredUserRole = CollectionUtils.find(userRoles, new Predicate() {
                    @Override
                    public boolean evaluate(Object object) {
                        UserRole userRole = (UserRole) object;
                        return reportRoles.contains(userRole.getRole()) ||
                            RoleType.SUPER.equals(userRole.getRole().getType());
                    }
                });
                if (requiredUserRole != null)
                    filter.add(report);
            }
        }
        return filter;
    }

    private static List<Report> checkScreens(List<Report> reports, final String screen) {
        List<Report> filter = new ArrayList<>();
        for (Report report : reports) {
            List<ReportScreen> reportScreens = report.getReportScreens();
            if (reportScreens == null || reportScreens.size() == 0)
                filter.add(report);
            else {
                Object reportScreen = CollectionUtils.find(reportScreens, new Predicate() {
                    @Override
                    public boolean evaluate(Object item) {
                        return StringUtils.equals(screen, ((ReportScreen)item).getScreenId());
                    }
                });
                if (reportScreen != null)
                    filter.add(report);
            }
        }
        return filter;
    }

    public static List<Report> applySecurityPolicies(User user, String screen, List<Report> reports) {
        List<Report> filter = checkRoles(user, reports);
        filter = checkScreens(filter, screen);
        return filter;
    }
}