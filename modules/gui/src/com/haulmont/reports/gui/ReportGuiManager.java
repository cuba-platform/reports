/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.gui;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.backgroundwork.BackgroundWorkWindow;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.executors.BackgroundTask;
import com.haulmont.cuba.gui.executors.TaskLifeCycle;
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
import com.haulmont.yarg.reporting.ReportOutputDocument;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import javax.annotation.ManagedBean;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.haulmont.reports.gui.report.run.InputParametersController.*;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author degtyarjov
 * @version $Id$
 */
@ManagedBean("cuba_ReportGuiManager")
public class ReportGuiManager {

    @Inject
    protected ReportService reportService;

    @Inject
    protected Messages messages;

    @Inject
    protected Metadata metadata;

    public void runReport(Report report, IFrame window) {
        if (report == null) {
            throw new IllegalArgumentException("Can not run null report");
        }

        if (report.getInputParameters() != null && report.getInputParameters().size() > 0) {
            openReportParamsDialog(window, report, null, null, null);
        } else {
            printReport(report, Collections.<String, Object>emptyMap(), window);
        }
    }

    public void runReport(Report report, IFrame window, final ReportInputParameter parameter, final Object parameterValue, @Nullable String templateCode, @Nullable String
            outputFileName) {
        if (report == null) {
            throw new IllegalArgumentException("Can not run null report");
        }
        List<ReportInputParameter> params = report.getInputParameters();

        boolean reportHasMoreThanOneParameter = params != null && params.size() > 1;

        Object resultingParamValue = convertParameterIfNecessary(parameter, parameterValue, reportHasMoreThanOneParameter);

        if (reportHasMoreThanOneParameter) {
            openReportParamsDialog(window, report, Collections.singletonMap(parameter.getAlias(), resultingParamValue), templateCode, outputFileName);
        } else {
            if (ParameterType.ENTITY == parameter.getType() && resultingParamValue instanceof Collection) {
                Collection selectedEntities = (Collection) resultingParamValue;
                if (selectedEntities.size() > 1) {
                    bulkPrint(report, parameter.getAlias(), selectedEntities, window);
                } else if (selectedEntities.size() == 1) {
                    printReport(report, Collections.singletonMap(parameter.getAlias(), selectedEntities.iterator().next()), templateCode, outputFileName, window);
                }
            } else {
                printReport(report, Collections.singletonMap(parameter.getAlias(), resultingParamValue), templateCode, outputFileName, window);
            }
        }
    }


    @Nullable
    protected Object convertParameterIfNecessary(ReportInputParameter parameter, @Nullable Object paramValue, boolean
            reportHasMoreThanOneParameter) {
        Object resultingParamValue = paramValue;
        if (ParameterType.ENTITY == parameter.getType()) {
            if (paramValue instanceof Collection || paramValue instanceof ParameterPrototype) {
                resultingParamValue = handleCollectionParameter(paramValue, reportHasMoreThanOneParameter);
            }
        } else if (ParameterType.ENTITY_LIST == parameter.getType()) {
            if (!(paramValue instanceof Collection) && !(paramValue instanceof ParameterPrototype)) {
                resultingParamValue = Collections.singletonList(paramValue);
            }
        }

        return resultingParamValue;
    }


    @Nullable
    protected Object handleCollectionParameter(@Nullable Object paramValue, boolean reportHasMoreThanOneParameter) {
        Collection paramValueWithCollection = null;
        if (paramValue instanceof Collection) {
            paramValueWithCollection = (Collection) paramValue;
        } else if (paramValue instanceof ParameterPrototype) {
            ParameterPrototype prototype = (ParameterPrototype) paramValue;
            if (reportHasMoreThanOneParameter) {
                //if the case of several params we can not do bulk print, because the params should be filled, so we don't need to load more than 1 entity
                prototype.setMaxResults(1);
            }
            paramValueWithCollection = reportService.loadDataForParameterPrototype(prototype);
        }

        if (CollectionUtils.isEmpty(paramValueWithCollection)) {
            return null;
        }

        if (reportHasMoreThanOneParameter) {
            //if the case of several params we can not do bulk print, because the params should be filled, so we get only first object from the list
            return paramValueWithCollection.iterator().next();
        }

        return paramValueWithCollection;
    }

    /**
     * Print report synchronously
     */
    public void printReport(Report report, Map<String, Object> params, @Nullable String templateCode, @Nullable String outputFileName) {
        printReportSync(report, params, templateCode, outputFileName, null);
    }

    public void printReport(Report report, Map<String, Object> params) {
        printReportSync(report, params, null, null, null);
    }

    public void printReport(Report report, Map<String, Object> params, IFrame window) {
        printReport(report, params, null, null, window);
    }

    public void printReport(Report report, Map<String, Object> params, @Nullable String templateCode, @Nullable String outputFileName, @Nullable IFrame window) {

        Configuration configuration = AppBeans.get(Configuration.NAME);
        ReportingClientConfig reportingClientConfig = configuration.getConfig(ReportingClientConfig.class);

        if (window != null && reportingClientConfig.getUseBackgroundReportProcessing()) {
            printReportBackground(report, params, templateCode, outputFileName, window);
        } else {
            printReportSync(report, params, templateCode, outputFileName, window);
        }
    }

    /**
     * Print report synchronously
     */
    public void printReportSync(Report report, Map<String, Object> params, @Nullable String templateCode, @Nullable String outputFileName, @Nullable IFrame window) {
        ReportOutputDocument document;
        if (StringUtils.isBlank(templateCode)) {
            document = reportService.createReport(report, params);
        } else {
            document = reportService.createReport(report, templateCode, params);
        }

        byte[] byteArr = document.getContent();
        ExportFormat exportFormat = ReportPrintHelper.getExportFormat(document.getReportOutputType());
        ExportDisplay exportDisplay = AppConfig.createExportDisplay(window);
        String documentName = isNotBlank(outputFileName) ? outputFileName : document.getDocumentName();
        exportDisplay.show(new ByteArrayDataProvider(byteArr), documentName, exportFormat);
    }

    /**
     * Print report in background task with window, supports cancel
     */
    public void printReportBackground(Report report, final Map<String, Object> params, final @Nullable String templateCode, final @Nullable String outputFileName, IFrame window) {

        Configuration configuration = AppBeans.get(Configuration.NAME);
        ReportingClientConfig reportingClientConfig = configuration.getConfig(ReportingClientConfig.class);

        final Report targetReport = getReportForPrinting(report);

        long timeout = reportingClientConfig.getBackgroundReportProcessingTimeoutMs();
        BackgroundTask<Void, ReportOutputDocument> task = new BackgroundTask<Void, ReportOutputDocument>(timeout, TimeUnit.MILLISECONDS, window) {

            @Override
            public ReportOutputDocument run(TaskLifeCycle<Void> taskLifeCycle) throws Exception {
                ReportOutputDocument result;
                if (StringUtils.isBlank(templateCode)) {
                    result = reportService.createReport(targetReport, params);
                } else {
                    result = reportService.createReport(targetReport, templateCode, params);
                }
                return result;
            }

            @Override
            public void done(ReportOutputDocument document) {
                byte[] byteArr = document.getContent();
                ExportFormat exportFormat = ReportPrintHelper.getExportFormat(document.getReportOutputType());
                ExportDisplay exportDisplay = AppConfig.createExportDisplay(null);
                String documentName = isNotBlank(outputFileName) ? outputFileName : document.getDocumentName();
                exportDisplay.show(new ByteArrayDataProvider(byteArr), documentName, exportFormat);
            }
        };

        String caption = messages.getMessage(getClass(), "runReportBackgroundTitle");
        String description = messages.getMessage(getClass(), "runReportBackgroundMessage");

        BackgroundWorkWindow.show(task, caption, description, true);
    }

    public List<Report> getAvailableReports(@Nullable String screenId, @Nullable User user, @Nullable MetaClass inputValueMetaClass) {

        LoadContext lContext = new LoadContext(Report.class);
        lContext.setView(new View(Report.class).addProperty("name").addProperty("localeNames").addProperty("xml"));
        if (inputValueMetaClass != null) {//select only reports having entity parameter
            lContext.setQueryString("select r from report$Report r where r.xml like :paramMask");
            lContext.getQuery().setParameter("paramMask", "%<entityMetaClass>%</entityMetaClass>%");
        } else {
            lContext.setQueryString("select r from report$Report r");
        }

        List<Report> reports = AppBeans.get(DataService.class).loadList(lContext);
        reports = filterReportsByEntityParameters(inputValueMetaClass, reports);
        reports = applySecurityPolicies(screenId, user, reports);
        return reports;
    }

    public void bulkPrint(Report report, String alias, Collection selectedEntities, @Nullable IFrame window) {
        Configuration configuration = AppBeans.get(Configuration.NAME);
        ReportingClientConfig reportingClientConfig = configuration.getConfig(ReportingClientConfig.class);
        if (window != null && reportingClientConfig.getUseBackgroundReportProcessing()) {
            bulkPrintBackground(report, alias, selectedEntities, window);
        } else {
            bulkPrintSync(report, alias, selectedEntities, window);
        }
    }

    /**
     * Print report synchronously for all selected entities
     */
    public void bulkPrint(Report report, String alias, Collection selectedEntities) {
        bulkPrintSync(report, alias, selectedEntities, null);
    }

    /**
     * Print report synchronously for all selected entities
     */
    public void bulkPrintSync(Report report, String alias, Collection selectedEntities, @Nullable IFrame window) {
        List<Map<String, Object>> paramsList = new ArrayList<>();
        for (Object selectedEntity : selectedEntities) {
            paramsList.add(Collections.singletonMap(alias, selectedEntity));
        }

        ReportOutputDocument reportOutputDocument = reportService.bulkPrint(report, paramsList);
        ExportDisplay exportDisplay = AppConfig.createExportDisplay(window);
        String documentName = reportOutputDocument.getDocumentName();
        exportDisplay.show(new ByteArrayDataProvider(reportOutputDocument.getContent()), documentName, ExportFormat.ZIP);
    }

    /**
     * Print report in background task for all selected entities
     */
    public void bulkPrintBackground(Report report, String alias, Collection selectedEntities, final IFrame window) {
        Configuration configuration = AppBeans.get(Configuration.NAME);
        ReportingClientConfig reportingClientConfig = configuration.getConfig(ReportingClientConfig.class);

        final Report targetReport = getReportForPrinting(report);

        long timeout = reportingClientConfig.getBackgroundReportProcessingTimeoutMs();

        final List<Map<String, Object>> paramsList = new ArrayList<>();
        for (Object selectedEntity : selectedEntities) {
            paramsList.add(Collections.singletonMap(alias, selectedEntity));
        }

        BackgroundTask<Void, ReportOutputDocument> task = new BackgroundTask<Void, ReportOutputDocument>(timeout, TimeUnit.MILLISECONDS, window) {

            @Override
            public ReportOutputDocument run(TaskLifeCycle<Void> taskLifeCycle) throws Exception {
                ReportOutputDocument result = reportService.bulkPrint(targetReport, paramsList);
                return result;
            }

            @Override
            public void done(ReportOutputDocument result) {
                ExportDisplay exportDisplay = AppConfig.createExportDisplay(window);
                String documentName = result.getDocumentName();
                exportDisplay.show(new ByteArrayDataProvider(result.getContent()), documentName, ExportFormat.ZIP);
            }
        };

        String caption = messages.getMessage(getClass(), "runReportBackgroundTitle");
        String description = messages.getMessage(getClass(), "runReportBackgroundMessage");

        BackgroundWorkWindow.show(task, caption, description, true);
    }

    public boolean parameterMatchesMetaClass(ReportInputParameter parameter, MetaClass metaClass) {
        if (isNotBlank(parameter.getEntityMetaClass())) {
            MetaClass parameterMetaClass = metadata.getClassNN(parameter.getEntityMetaClass());
            return (metaClass.equals(parameterMetaClass) || metaClass.getAncestors().contains(parameterMetaClass));
        } else {
            return false;
        }
    }


    /**
     * Prepare report for printing, use only Id from old instance
     */
    protected Report getReportForPrinting(Report report) {
        final Report targetReport = new Report();
        targetReport.setId(report.getId());

        return targetReport;
    }

    protected List<Report> filterReportsByEntityParameters(@Nullable MetaClass inputValueMetaClass, List<Report> reports) {
        if (inputValueMetaClass == null) {
            return reports;
        }

        List<Report> reportsForEntity = new ArrayList<>();
        for (Report report : reports) {
            for (ReportInputParameter parameter : report.getInputParameters()) {
                if (parameterMatchesMetaClass(parameter, inputValueMetaClass)) {
                    reportsForEntity.add(report);
                    break;
                }
            }
        }

        return reportsForEntity;
    }

    protected List<Report> applySecurityPolicies(@Nullable String screen, @Nullable User user, List<Report> reports) {
        List<Report> filter = checkRoles(user, reports);
        filter = checkScreens(screen, filter);
        return filter;
    }

    protected void openReportParamsDialog(IFrame window, Report report, @Nullable Map<String, Object> parameters, @Nullable String templateCode, @Nullable String outputFileName) {

        Map<String, Object> params = new HashMap<>();
        params.put(REPORT_PARAMETER, report);
        params.put(PARAMETERS_PARAMETER, parameters);
        params.put(TEMPLATE_CODE_PARAMETER, templateCode);
        params.put(OUTPUT_FILE_NAME_PARAMETER, outputFileName);

        window.openWindow("report$inputParameters", WindowManager.OpenType.DIALOG, params);
    }

    protected List<Report> checkRoles(@Nullable User user, List<Report> reports) {
        if (user != null) {
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
                            return reportRoles.contains(userRole.getRole()) || RoleType.SUPER.equals(userRole.getRole().getType());
                        }
                    });
                    if (requiredUserRole != null) {
                        filter.add(report);
                    }
                }
            }
            return filter;
        } else {
            return reports;
        }
    }

    protected List<Report> checkScreens(@Nullable final String screen, List<Report> reports) {
        if (screen != null) {
            List<Report> filter = new ArrayList<>();
            for (Report report : reports) {
                List<ReportScreen> reportScreens = report.getReportScreens();
                if (reportScreens == null || reportScreens.size() == 0) {
                    filter.add(report);
                } else {
                    Object reportScreen = CollectionUtils.find(reportScreens, new Predicate() {
                        @Override
                        public boolean evaluate(Object item) {
                            return StringUtils.equals(screen, ((ReportScreen) item).getScreenId());
                        }
                    });
                    if (reportScreen != null) {
                        filter.add(report);
                    }
                }
            }
            return filter;
        } else {
            return reports;
        }
    }
}