/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports.gui;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.WindowManagerProvider;
import com.haulmont.cuba.gui.backgroundwork.BackgroundWorkWindow;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.executors.BackgroundTask;
import com.haulmont.cuba.gui.executors.TaskLifeCycle;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.security.entity.RoleType;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.entity.UserRole;
import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.*;
import com.haulmont.reports.gui.report.run.ShowChartController;
import com.haulmont.reports.gui.report.run.ShowReportTable;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.haulmont.reports.gui.report.run.InputParametersFrame.PARAMETERS_PARAMETER;
import static com.haulmont.reports.gui.report.run.InputParametersFrame.REPORT_PARAMETER;
import static com.haulmont.reports.gui.report.run.InputParametersWindow.*;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Component("cuba_ReportGuiManager")
public class ReportGuiManager {

    @Inject
    protected ReportService reportService;

    @Inject
    protected DataService dataService;

    @Inject
    protected Messages messages;

    @Inject
    protected Metadata metadata;

    @Inject
    protected Configuration configuration;

    @Inject
    protected WindowManagerProvider windowManagerProvider;

    @Inject
    protected WindowConfig windowConfig;

    @Inject
    protected QueryTransformerFactory queryTransformerFactory;

    @Inject
    protected UserSessionSource userSessionSource;

    /**
     * Open input parameters dialog if report has parameters otherwise print report
     *
     * @param report - target report
     * @param window - caller window
     */
    public void runReport(Report report, Frame window) {
        if (report == null) {
            throw new IllegalArgumentException("Can not run null report");
        }

        if (inputParametersRequired(report)) {
            openReportParamsDialog(window, report, null, null, null);
        } else {
            printReport(report, ParamsMap.empty(), window);
        }
    }

    /**
     * Open input parameters dialog if report has parameters otherwise print report.
     * The method allows to select target template code, pass single parameter to report, and set output file name.
     *
     * @param report         - target report
     * @param window         - caller window
     * @param parameter      - input parameter linked with passed parameter value
     * @param parameterValue -
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     */
    public void runReport(Report report, Frame window, final ReportInputParameter parameter, final Object parameterValue,
                          @Nullable String templateCode, @Nullable String outputFileName) {
        if (report == null) {
            throw new IllegalArgumentException("Can not run null report");
        }
        List<ReportInputParameter> params = report.getInputParameters();

        boolean reportHasMoreThanOneParameter = params != null && params.size() > 1;

        Object resultingParamValue = convertParameterIfNecessary(parameter, parameterValue, reportHasMoreThanOneParameter);

        boolean reportTypeIsSingleEntity = ParameterType.ENTITY == parameter.getType() && resultingParamValue instanceof Collection;
        boolean moreThanOneEntitySelected = resultingParamValue instanceof Collection && ((Collection) resultingParamValue).size() > 1;

        if (reportHasMoreThanOneParameter) {
            boolean bulkPrint = reportTypeIsSingleEntity && moreThanOneEntitySelected;
            openReportParamsDialog(window, report, ParamsMap.of(parameter.getAlias(), resultingParamValue), parameter, templateCode, outputFileName,
                    bulkPrint);
        } else {
            if (reportTypeIsSingleEntity) {
                Collection selectedEntities = (Collection) resultingParamValue;
                if (moreThanOneEntitySelected) {
                    bulkPrint(report, parameter.getAlias(), selectedEntities, window);
                } else if (selectedEntities.size() == 1) {
                    printReport(report, ParamsMap.of(parameter.getAlias(), selectedEntities.iterator().next()), templateCode, outputFileName, window);
                }
            } else {
                printReport(report, ParamsMap.of(parameter.getAlias(), resultingParamValue), templateCode, outputFileName, window);
            }
        }
    }

    /**
     * Print report synchronously
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     */
    public void printReport(Report report, Map<String, Object> params, @Nullable String templateCode, @Nullable String outputFileName) {
        printReportSync(report, params, templateCode, outputFileName, null);
    }

    /**
     * Print report synchronously
     *
     * @param report - target report
     * @param params - report parameters (map keys should match with parameter aliases)
     */
    public void printReport(Report report, Map<String, Object> params) {
        printReportSync(report, params, null, null, null);
    }

    /**
     * Print report synchronously or asynchronously, depending on configurations
     *
     * @param report - target report
     * @param params - report parameters (map keys should match with parameter aliases)
     * @param window - caller window
     */
    public void printReport(Report report, Map<String, Object> params, Frame window) {
        printReport(report, params, null, null, window);
    }

    /**
     * Print report synchronously or asynchronously, depending on configurations
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     * @param window         - caller window
     */
    public void printReport(Report report, Map<String, Object> params, @Nullable String templateCode, @Nullable String outputFileName, @Nullable Frame window) {
        printReport(report, params, templateCode, outputFileName, null, window);
    }

    /**
     * Print report synchronously or asynchronously, depending on configurations
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     * @param outputType     - output type for file
     * @param window         - caller window
     */
    public void printReport(Report report, Map<String, Object> params, @Nullable String templateCode, @Nullable String outputFileName, @Nullable ReportOutputType outputType, Frame window) {

        Configuration configuration = AppBeans.get(Configuration.NAME);
        ReportingClientConfig reportingClientConfig = configuration.getConfig(ReportingClientConfig.class);

        if (window != null && reportingClientConfig.getUseBackgroundReportProcessing()) {
            printReportBackground(report, params, templateCode, outputFileName, outputType, window);
        } else {
            printReportSync(report, params, templateCode, outputFileName, outputType, window);
        }
    }

    /**
     * Print report synchronously
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     * @param window         - caller window
     */
    public void printReportSync(Report report, Map<String, Object> params, @Nullable String templateCode, @Nullable String outputFileName, @Nullable Frame window) {
        printReportSync(report, params, templateCode, outputFileName, null, window);
    }

    /**
     * Print report synchronously
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     * @param outputType     - output type for file
     * @param window         - caller window
     */
    public void printReportSync(Report report, Map<String, Object> params, @Nullable String templateCode,
                                @Nullable String outputFileName, @Nullable ReportOutputType outputType, @Nullable Frame window) {
        ReportOutputDocument document = getReportResult(report, params, templateCode, outputType);

        showReportResult(document, params, templateCode, outputFileName, window);
    }

    /**
     * Generate ReportOutputDocument
     *
     * @param report       - target report
     * @param params       - report parameters (map keys should match with parameter aliases)
     * @param templateCode - target template code
     * @return resulting ReportOutputDocument
     */
    public ReportOutputDocument getReportResult(Report report, Map<String, Object> params, @Nullable String templateCode) {
        return getReportResult(report, params, templateCode, null);
    }

    /**
     * Generate ReportOutputDocument
     *
     * @param report       - target report
     * @param params       - report parameters (map keys should match with parameter aliases)
     * @param templateCode - target template code
     * @param outputType   - target output type
     * @return resulting ReportOutputDocument
     */
    public ReportOutputDocument getReportResult(Report report, Map<String, Object> params, @Nullable String templateCode, @Nullable ReportOutputType outputType) {
        ReportOutputDocument document;
        if (StringUtils.isBlank(templateCode) && outputType == null) {
            document = reportService.createReport(report, params);
        } else if (!StringUtils.isBlank(templateCode) && outputType == null) {
            document = reportService.createReport(report, templateCode, params);
        } else if (!StringUtils.isBlank(templateCode) && outputType != null) {
            document = reportService.createReport(report, templateCode, params, outputType);
        } else {
            document = reportService.createReport(report, params, outputType);
        }
        return document;
    }

    protected void showReportResult(ReportOutputDocument document, Map<String, Object> params,
                                    @Nullable String templateCode, @Nullable String outputFileName, @Nullable Frame window) {
        showReportResult(document, params, templateCode, outputFileName, null, window);
    }

    protected void showReportResult(ReportOutputDocument document, Map<String, Object> params,
                                    @Nullable String templateCode, @Nullable String outputFileName, @Nullable ReportOutputType outputType, @Nullable Frame window) {
        if (document.getReportOutputType().getId().equals(CubaReportOutputType.chart.getId())) {
            Map<String, Object> screenParams = new HashMap<>();
            screenParams.put(ShowChartController.CHART_JSON_PARAMETER, new String(document.getContent(), StandardCharsets.UTF_8));
            screenParams.put(ShowChartController.REPORT_PARAMETER, document.getReport());
            screenParams.put(ShowChartController.TEMPLATE_CODE_PARAMETER, templateCode);
            screenParams.put(ShowChartController.PARAMS_PARAMETER, params);

            if (window != null) {
                window.openWindow("report$showChart", OpenType.DIALOG, screenParams);
            } else {
                WindowInfo windowInfo = windowConfig.getWindowInfo("report$showChart");
                windowManagerProvider.get().openWindow(windowInfo, OpenType.DIALOG, screenParams);
            }
        } else if (document.getReportOutputType().getId().equals(CubaReportOutputType.table.getId())) {
            Map<String, Object> screenParams = new HashMap<>();
            screenParams.put(ShowReportTable.TABLE_DATA_PARAMETER, document.getContent());
            screenParams.put(ShowReportTable.REPORT_PARAMETER, document.getReport());
            screenParams.put(ShowReportTable.TEMPLATE_CODE_PARAMETER, templateCode);
            screenParams.put(ShowReportTable.PARAMS_PARAMETER, params);

            if (window != null) {
                window.openWindow("report$showReportTable", OpenType.DIALOG, screenParams);
            } else {
                WindowInfo windowInfo = windowConfig.getWindowInfo("report$showReportData");
                windowManagerProvider.get().openWindow(windowInfo, OpenType.DIALOG, screenParams);
            }
        } else {
            byte[] byteArr = document.getContent();
            com.haulmont.yarg.structure.ReportOutputType finalOutputType = (outputType != null) ? outputType.getOutputType() : document.getReportOutputType();
            ExportFormat exportFormat = ReportPrintHelper.getExportFormat(finalOutputType);
            ExportDisplay exportDisplay = AppConfig.createExportDisplay(window);
            String documentName = isNotBlank(outputFileName) ? outputFileName : document.getDocumentName();
            exportDisplay.show(new ByteArrayDataProvider(byteArr), documentName, exportFormat);
        }
    }

    /**
     * Print report in background task with window, supports cancel
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     * @param window         - caller window
     */
    public void printReportBackground(Report report, final Map<String, Object> params,
                                      final @Nullable String templateCode, final @Nullable String outputFileName, final Frame window) {
        printReportBackground(report, params, templateCode, outputFileName, null, window);
    }

    /**
     * Print report in background task with window, supports cancel
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     * @param outputType     - output type for file
     * @param window         - caller window
     */
    public void printReportBackground(Report report, final Map<String, Object> params, final @Nullable String templateCode,
                                      final @Nullable String outputFileName, final @Nullable ReportOutputType outputType, final Frame window) {
        ReportingClientConfig reportingClientConfig = configuration.getConfig(ReportingClientConfig.class);

        Report targetReport = getReportForPrinting(report);

        long timeout = reportingClientConfig.getBackgroundReportProcessingTimeoutMs();
        final UUID userSessionId = userSessionSource.getUserSession().getId();
        BackgroundTask<Void, ReportOutputDocument> task = new BackgroundTask<Void, ReportOutputDocument>(timeout, TimeUnit.MILLISECONDS, window) {

            @SuppressWarnings("UnnecessaryLocalVariable")
            @Override
            public ReportOutputDocument run(TaskLifeCycle<Void> taskLifeCycle) throws Exception {
                ReportOutputDocument result = getReportResult(targetReport, params, templateCode, outputType);
                return result;
            }

            @Override
            public void done(ReportOutputDocument document) {
                showReportResult(document, params, templateCode, outputFileName, outputType, window);
            }

            @Override
            public void canceled() {
                super.canceled();
                reportService.cancelReportExecution(userSessionId, report.getId());
            }
        };

        String caption = messages.getMessage(ReportGuiManager.class, "runReportBackgroundTitle");
        String description = messages.getMessage(ReportGuiManager.class, "runReportBackgroundMessage");

        BackgroundWorkWindow.show(task, caption, description, true);
    }

    /**
     * Return list of reports, available for certain screen, user and input parameter
     *
     * @param screenId            - id of the screen
     * @param user                - caller user
     * @param inputValueMetaClass - meta class of report input parameter
     */
    public List<Report> getAvailableReports(@Nullable String screenId, @Nullable User user, @Nullable MetaClass inputValueMetaClass) {
        LoadContext<Report> lc = new LoadContext<>(Report.class);
        lc.setLoadDynamicAttributes(true);
        lc.setView(new View(Report.class)
                .addProperty("name")
                .addProperty("localeNames")
                .addProperty("description")
                .addProperty("code")
                .addProperty("group", metadata.getViewRepository().getView(ReportGroup.class, View.LOCAL)));
        lc.setQueryString("select r from report$Report r");
        applySecurityPolicies(lc, screenId, user);
        filterReportsByEntityParameters(lc, inputValueMetaClass);
        return dataService.loadList(lc);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Synchronously or asynchronously, depending on configurations
     *
     * @param report           - target report
     * @param templateCode     - target template code
     * @param outputType       - output type for file
     * @param alias            - parameter alias
     * @param selectedEntities - list of selected entities
     * @param window           - caller window
     * @param additionalParameters - user-defined parameters
     */
    public void bulkPrint(Report report, @Nullable String templateCode, @Nullable ReportOutputType outputType, String alias,
                          Collection selectedEntities, @Nullable Frame window, @Nullable Map<String, Object> additionalParameters) {
        ReportingClientConfig reportingClientConfig = configuration.getConfig(ReportingClientConfig.class);
        if (window != null && reportingClientConfig.getUseBackgroundReportProcessing()) {
            bulkPrintBackground(report, templateCode, outputType, alias, selectedEntities, window, additionalParameters);
        } else {
            bulkPrintSync(report, templateCode, outputType, alias, selectedEntities, window, additionalParameters);
        }
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Synchronously or asynchronously, depending on configurations
     *
     * @param report           - target report
     * @param alias            - parameter alias
     * @param selectedEntities - list of selected entities
     * @param window           - caller window
     * @param additionalParameters - user-defined parameters
     */
    public void bulkPrint(Report report, String alias, Collection selectedEntities, @Nullable Frame window,
                          @Nullable Map<String, Object> additionalParameters) {
        bulkPrint(report, null, null, alias, selectedEntities, window, additionalParameters);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as inputParameter with certain alias.
     * Synchronously or asynchronously, depending on configurations
     *
     * @param report           - target report
     * @param alias            - inputParameter alias
     * @param selectedEntities - list of selected entities
     * @param window           - caller window
     */
    public void bulkPrint(Report report, String alias, Collection selectedEntities, @Nullable Frame window) {
        bulkPrint(report, alias, selectedEntities, window, null);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Synchronously.
     *
     * @param report           - target report
     * @param alias            - parameter alias
     * @param selectedEntities - list of selected entities
     */
    public void bulkPrint(Report report, String alias, Collection selectedEntities) {
        bulkPrintSync(report, alias, selectedEntities, null);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Synchronously.
     *
     * @param report           - target report
     * @param alias            - parameter alias
     * @param selectedEntities - list of selected entities
     * @param window           - caller window
     * @param additionalParameters - user-defined parameters
     */
    public void bulkPrintSync(Report report, String alias, Collection selectedEntities, @Nullable Frame window,
                              Map<String, Object> additionalParameters) {
        bulkPrintSync(report, null, null, alias, selectedEntities, window, additionalParameters);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Synchronously.
     *
     * @param report           - target report
     * @param templateCode     - target template code
     * @param outputType       - output type for file
     * @param alias            - parameter alias
     * @param selectedEntities - list of selected entities
     * @param window           - caller window
     * @param additionalParameters - user-defined parameters
     */
    public void bulkPrintSync(Report report, @Nullable String templateCode, @Nullable ReportOutputType outputType,
                              String alias, Collection selectedEntities, @Nullable Frame window,
                              Map<String, Object> additionalParameters) {
        List<Map<String, Object>> paramsList = new ArrayList<>();
        for (Object selectedEntity : selectedEntities) {
            Map<String, Object> map = new HashMap<>();
            map.put(alias, selectedEntity);
            if (additionalParameters != null) {
                map.putAll(additionalParameters);
            }
            paramsList.add(map);
        }

        ReportOutputDocument reportOutputDocument = reportService.bulkPrint(report, templateCode, outputType, paramsList);
        ExportDisplay exportDisplay = AppConfig.createExportDisplay(window);
        String documentName = reportOutputDocument.getDocumentName();
        exportDisplay.show(new ByteArrayDataProvider(reportOutputDocument.getContent()), documentName, ExportFormat.ZIP);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as inputParameter with certain alias.
     * Synchronously.
     *
     * @param report           - target report
     * @param alias            - inputParameter alias
     * @param selectedEntities - list of selected entities
     * @param window           - caller window
     */
    public void bulkPrintSync(Report report, String alias, Collection selectedEntities, @Nullable Frame window) {
        bulkPrintSync(report, alias, selectedEntities, window, null);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Asynchronously.
     *
     * @param report           - target report
     * @param alias            - parameter alias
     * @param selectedEntities - list of selected entities
     * @param window           - caller window
     * @param additionalParameters - user-defined parameters
     */
    public void bulkPrintBackground(Report report, String alias, Collection selectedEntities, final Frame window,
                                    Map<String, Object> additionalParameters) {
        bulkPrintBackground(report, null, null, alias, selectedEntities, window, additionalParameters);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Asynchronously.
     *
     * @param report           - target report
     * @param templateCode     - target template code
     * @param outputType       - output type for file
     * @param alias            - parameter alias
     * @param selectedEntities - list of selected entities
     * @param window           - caller window
     * @param additionalParameters - user-defined parameters
     */
    public void bulkPrintBackground(Report report, @Nullable String templateCode, @Nullable ReportOutputType outputType, String alias, Collection selectedEntities, final Frame window,
                                    Map<String, Object> additionalParameters) {
        ReportingClientConfig reportingClientConfig = configuration.getConfig(ReportingClientConfig.class);

        final Report targetReport = getReportForPrinting(report);

        long timeout = reportingClientConfig.getBackgroundReportProcessingTimeoutMs();

        List<Map<String, Object>> paramsList = new ArrayList<>();
        for (Object selectedEntity : selectedEntities) {
            Map<String, Object> map = new HashMap<>();
            map.put(alias, selectedEntity);
            if (additionalParameters != null) {
                map.putAll(additionalParameters);
            }
            paramsList.add(map);
        }

        BackgroundTask<Void, ReportOutputDocument> task = new BackgroundTask<Void, ReportOutputDocument>(timeout, TimeUnit.MILLISECONDS, window) {
            @SuppressWarnings("UnnecessaryLocalVariable")
            @Override
            public ReportOutputDocument run(TaskLifeCycle<Void> taskLifeCycle) throws Exception {
                ReportOutputDocument result = reportService.bulkPrint(targetReport, templateCode, outputType, paramsList);
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

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Asynchronously.
     *
     * @param report           - target report
     * @param alias            - parameter alias
     * @param selectedEntities - list of selected entities
     * @param window           - caller window
     */
    public void bulkPrintBackground(Report report, String alias, Collection selectedEntities, final Frame window) {
        bulkPrintBackground(report, alias, selectedEntities, window, null);
    }

    /**
     * Check if the meta class is applicable for the input parameter
     */
    public boolean parameterMatchesMetaClass(ReportInputParameter parameter, MetaClass metaClass) {
        if (isNotBlank(parameter.getEntityMetaClass())) {
            MetaClass parameterMetaClass = metadata.getClassNN(parameter.getEntityMetaClass());
            return (metaClass.equals(parameterMetaClass) || metaClass.getAncestors().contains(parameterMetaClass));
        } else {
            return false;
        }
    }

    /**
     * Defensive copy
     */
    protected Report getReportForPrinting(Report report) {
        Report copy = metadata.getTools().copy(report);
        copy.setIsTmp(report.getIsTmp());
        return copy;
    }

    /**
     * Apply constraints for query to select reports which have input parameter with class matching inputValueMetaClass
     */
    protected void filterReportsByEntityParameters(LoadContext lc, @Nullable MetaClass inputValueMetaClass) {
        if (inputValueMetaClass != null) {
            QueryTransformer transformer = queryTransformerFactory.transformer(lc.getQuery().getQueryString());
            StringBuilder parameterTypeCondition = new StringBuilder("r.inputEntityTypesIdx like :type escape '\\'");
            lc.getQuery().setParameter("type", wrapIdxParameterForSearch(inputValueMetaClass.getName()));
            List<MetaClass> ancestors = inputValueMetaClass.getAncestors();
            for (int i = 0; i < ancestors.size(); i++) {
                MetaClass metaClass = ancestors.get(i);
                String paramName = "type" + (i + 1);
                parameterTypeCondition.append(" or r.inputEntityTypesIdx like :").append(paramName).append(" escape '\\'");
                lc.getQuery().setParameter(paramName, wrapIdxParameterForSearch(metaClass.getName()));
            }
            transformer.addWhereAsIs(String.format("(%s)", parameterTypeCondition.toString()));
            lc.getQuery().setQueryString(transformer.getResult());
        }
    }

    /**
     * Apply security constraints for query to select reports available by roles and screen restrictions
     */
    protected void applySecurityPolicies(LoadContext lc, @Nullable String screen, @Nullable User user) {
        QueryTransformer transformer = queryTransformerFactory.transformer(lc.getQuery().getQueryString());
        if (screen != null) {
            transformer.addWhereAsIs("r.screensIdx like :screen escape '\\'");
            lc.getQuery().setParameter("screen", wrapIdxParameterForSearch(screen));
        }
        if (user != null) {
            List<UserRole> userRoles = user.getUserRoles();
            boolean superRole = userRoles.stream().anyMatch(userRole -> userRole.getRole().getType() == RoleType.SUPER);
            if (!superRole) {
                StringBuilder roleCondition = new StringBuilder("r.rolesIdx is null");
                for (int i = 0; i < userRoles.size(); i++) {
                    UserRole ur = userRoles.get(i);
                    String paramName = "role" + (i + 1);
                    roleCondition.append(" or r.rolesIdx like :").append(paramName).append(" escape '\\'");
                    lc.getQuery().setParameter(paramName, wrapIdxParameterForSearch(ur.getRole().getId().toString()));
                }
                transformer.addWhereAsIs(roleCondition.toString());
            }
        }
        lc.getQuery().setQueryString(transformer.getResult());
    }

    protected void openReportParamsDialog(Frame window, Report report, @Nullable Map<String, Object> parameters,
                                          @Nullable ReportInputParameter inputParameter, @Nullable String templateCode,
                                          @Nullable String outputFileName,
                                          boolean bulkPrint) {
        Map<String, Object> params = ParamsMap.of(
                REPORT_PARAMETER, report,
                PARAMETERS_PARAMETER, parameters,
                INPUT_PARAMETER, inputParameter,
                TEMPLATE_CODE_PARAMETER, templateCode,
                OUTPUT_FILE_NAME_PARAMETER, outputFileName,
                BULK_PRINT, bulkPrint
        );
        window.openWindow("report$inputParameters", OpenType.DIALOG, params);
    }

    protected void openReportParamsDialog(Frame window, Report report, @Nullable Map<String, Object> parameters,
                                          @Nullable String templateCode, @Nullable String outputFileName) {
        openReportParamsDialog(window, report, parameters, null, templateCode, outputFileName, false);
    }

    @Nullable
    protected Object convertParameterIfNecessary(ReportInputParameter parameter, @Nullable Object paramValue,
                                                 boolean reportHasMoreThanOneParameter) {
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
            paramValueWithCollection = reportService.loadDataForParameterPrototype(prototype);
        }

        if (CollectionUtils.isEmpty(paramValueWithCollection)) {
            return null;
        }

        if (reportHasMoreThanOneParameter && paramValueWithCollection.size() == 1) {
            //if the case of several params we can not do bulk print, because the params should be filled, so we get only first object from the list
            return paramValueWithCollection.iterator().next();
        }

        return paramValueWithCollection;
    }

    protected String wrapIdxParameterForSearch(String value) {
        return "%," + QueryUtils.escapeForLike(value) + ",%";
    }

    public boolean inputParametersRequired(Report report) {
        return (report.getInputParameters() != null && report.getInputParameters().size() > 0) ||
                (report.getTemplates() != null && report.getTemplates().size() > 1) ||
                containsAlterableTemplate(report);

    }

    public boolean containsAlterableTemplate(Report report) {
        for (ReportTemplate template : report.getTemplates()) {
            if (supportAlterableForTemplate(template)) {
                return true;
            }
        }
        return false;
    }

    public boolean supportAlterableForTemplate(ReportTemplate template) {
        if (BooleanUtils.isTrue(template.getCustom())) {
            return false;
        }
        if (template.getReportOutputType() == ReportOutputType.CHART || template.getReportOutputType() == ReportOutputType.TABLE) {
            return false;
        }
        return BooleanUtils.isTrue(template.getAlterable());
    }
}