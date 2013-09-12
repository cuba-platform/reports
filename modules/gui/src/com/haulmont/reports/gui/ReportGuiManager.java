/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: degtyarjov
 * Created: 11.09.13 12:01
 *
 * $Id$
 */
package com.haulmont.reports.gui;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.LoadContext;
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
import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.ParameterType;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.entity.ReportScreen;
import com.haulmont.reports.exception.ReportingException;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import javax.annotation.ManagedBean;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

@ManagedBean
public class ReportGuiManager {

    @Inject
    protected ReportService reportService;

    public void runReport(Report report, Window window) {
        if (report != null) {
            if (report.getInputParameters() != null && report.getInputParameters().size() > 0) {
                openReportParamsDialog(window, report, null);
            } else {
                printReport(report, Collections.<String, Object>emptyMap());
            }
        }
    }

    public void runReport(Report report, Window window, final ReportInputParameter parameter, final Object paramValue) {
        if (report != null) {
            List<ReportInputParameter> params = report.getInputParameters();
            if (params != null && params.size() > 1) {
                if (ParameterType.ENTITY == parameter.getType()) {
                    Collection selected = (Collection) paramValue;
                    if (CollectionUtils.isNotEmpty(selected)) {
                        openReportParamsDialog(window, report, Collections.singletonMap(parameter.getAlias(), selected.iterator().next()));
                    } else {
                        openReportParamsDialog(window, report, Collections.singletonMap(parameter.getAlias(), null));
                    }
                } else {
                    openReportParamsDialog(window, report, Collections.singletonMap(parameter.getAlias(), paramValue));
                }
            } else if (params == null || params.size() <= 1) {
                if (ParameterType.ENTITY == parameter.getType()) {
                    if (paramValue instanceof Collection) {
                        Collection selectedEntities = (Collection) paramValue;
                        if (selectedEntities.size() == 1) {
                            printReport(report, Collections.<String, Object>singletonMap(parameter.getAlias(), selectedEntities.iterator().next()));
                        } else if (selectedEntities.size() > 1) {
                            bulkPrint(report, parameter.getAlias(), selectedEntities);
                        } else if (selectedEntities.size() == 0) {
                            printReport(report, Collections.<String, Object>singletonMap(parameter.getAlias(), null));
                        }
                    } else if (paramValue instanceof ParameterPrototype) {
                        throw new ReportingException("[Entity] parameter type does not support prototype loaders");
                    }
                } else if (ParameterType.ENTITY_LIST == parameter.getType()) {
                    printReport(report, Collections.<String, Object>singletonMap(parameter.getAlias(), paramValue));
                }
            }
        }
    }

    public void printReport(Report report, Map<String, Object> params) {
        printReport(report, null, params);
    }

    public void printReport(Report report, @Nullable String templateCode, Map<String, Object> params) {
        try {
            ReportOutputDocument document;
            if (StringUtils.isBlank(templateCode)) {
                document = reportService.createReport(report, params);
            } else {
                document = reportService.createReport(report, templateCode, params);
            }

            byte[] byteArr = document.getContent();
            ExportFormat exportFormat = ReportPrintHelper.getExportFormat(document.getReportOutputType());

            ExportDisplay exportDisplay = AppConfig.createExportDisplay(null);
            String documentName = document.getDocumentName();
            exportDisplay.show(new ByteArrayDataProvider(byteArr), documentName, exportFormat);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Report> getAvailableReports(String screenId, User user, MetaClass metaClass, boolean listReportsOnly) {
        LoadContext lContext = new LoadContext(Report.class);
        lContext.setView(ReportService.MAIN_VIEW_NAME);
        lContext.setQueryString("select r from report$Report r");

        List<Report> reports = AppBeans.get(DataService.class).loadList(lContext);
        reports = filterReportsByEntityParameters(metaClass, reports, listReportsOnly);
        reports = applySecurityPolicies(screenId, user, reports);
        return reports;
    }

    private void bulkPrint(Report report, String alias, Collection selectedEntities) {
        List<Map<String, Object>> paramsList = new ArrayList<>();
        for (Object selectedEntity : selectedEntities) {
            paramsList.add(Collections.singletonMap(alias, selectedEntity));
        }

        ReportOutputDocument reportOutputDocument = reportService.bulkPrint(report, paramsList);
        ExportDisplay exportDisplay = AppConfig.createExportDisplay(null);
        String documentName = reportOutputDocument.getDocumentName();
        exportDisplay.show(new ByteArrayDataProvider(reportOutputDocument.getContent()), documentName, ExportFormat.ZIP);
    }

    private List<Report> filterReportsByEntityParameters(MetaClass metaClass, List<Report> reports, boolean listReportsOnly) {
        List<Report> reportsForEntity = new ArrayList<>();
        for (Report report : reports) {
            for (ReportInputParameter parameter : report.getInputParameters()) {
                if (parameter.getEntityMetaClass().equals(metaClass.getName()) && (!listReportsOnly || ParameterType.ENTITY_LIST == parameter.getType())) {
                    reportsForEntity.add(report);
                    break;
                }
            }
        }
        return reportsForEntity;
    }

    public List<Report> applySecurityPolicies(String screen, User user, List<Report> reports) {
        List<Report> filter = checkRoles(user, reports);
        filter = checkScreens(filter, screen);
        return filter;
    }

    private void openReportParamsDialog(Window window, Report report, Map<String, Object> parameters) {
        Map<String, Object> params = new HashMap<>();
        params.put("report", report);
        params.put("parameters", parameters);

        window.openWindow("report$inputParameters", WindowManager.OpenType.DIALOG, params);
    }

    private List<Report> checkRoles(User user, List<Report> reports) {
        List<Report> filter = new ArrayList<>();
        for (Report report : reports) {
            final Set<Role> reportRoles = report.getRoles();
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

    private List<Report> checkScreens(List<Report> reports, final String screen) {
        List<Report> filter = new ArrayList<>();
        for (Report report : reports) {
            List<ReportScreen> reportScreens = report.getReportScreens();
            if (reportScreens == null || reportScreens.size() == 0)
                filter.add(report);
            else {
                Object reportScreen = CollectionUtils.find(reportScreens, new Predicate() {
                    @Override
                    public boolean evaluate(Object item) {
                        return StringUtils.equals(screen, ((ReportScreen) item).getScreenId());
                    }
                });
                if (reportScreen != null)
                    filter.add(report);
            }
        }
        return filter;
    }
}
