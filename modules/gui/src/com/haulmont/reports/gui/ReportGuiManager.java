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
package com.haulmont.reports.gui;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.WindowManagerProvider;
import com.haulmont.cuba.gui.backgroundwork.BackgroundWorkWindow;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.executors.BackgroundTask;
import com.haulmont.cuba.gui.executors.TaskLifeCycle;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.screen.FrameOwner;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.ScreenContext;
import com.haulmont.cuba.gui.screen.UiControllerUtils;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.*;
import com.haulmont.reports.gui.report.run.ShowChartController;
import com.haulmont.reports.gui.report.run.ShowPivotTableController;
import com.haulmont.reports.gui.report.run.ShowReportTable;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.haulmont.reports.gui.report.run.InputParametersFrame.PARAMETERS_PARAMETER;
import static com.haulmont.reports.gui.report.run.InputParametersFrame.REPORT_PARAMETER;
import static com.haulmont.reports.gui.report.run.InputParametersWindow.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
    protected ReportSecurityManager reportSecurityManager;

    @Inject
    protected UserSessionSource userSessionSource;

    /**
     * Open input parameters dialog if report has parameters otherwise print report
     *
     * @param report - target report
     * @param screen - caller window
     */
    public void runReport(Report report, FrameOwner screen) {
        if (report == null) {
            throw new IllegalArgumentException("Can not run null report");
        }

        if (report.getInputParameters() != null && report.getInputParameters().size() > 0
                || inputParametersRequiredByTemplates(report)) {
            openReportParamsDialog(screen, report, null, null, null);
        } else {
            printReport(report, ParamsMap.empty(), screen);
        }
    }

    /**
     * Open input parameters dialog if report has parameters otherwise print report.
     * The method allows to select target template code, pass single parameter to report, and set output file name.
     *
     * @param report         target report
     * @param screen         caller window
     * @param parameter      input parameter linked with passed parameter value
     * @param parameterValue parameter value
     * @param templateCode   target template code
     * @param outputFileName name for output file
     */
    public void runReport(Report report, FrameOwner screen, final ReportInputParameter parameter, final Object parameterValue,
                          @Nullable String templateCode, @Nullable String outputFileName) {
        if (report == null) {
            throw new IllegalArgumentException("Can not run null report");
        }
        List<ReportInputParameter> params = report.getInputParameters();

        boolean reportHasMoreThanOneParameter = params != null && params.size() > 1;
        boolean inputParametersRequiredByTemplates = inputParametersRequiredByTemplates(report);

        Object resultingParamValue = convertParameterIfNecessary(parameter, parameterValue,
                reportHasMoreThanOneParameter || inputParametersRequiredByTemplates);

        boolean reportTypeIsSingleEntity = ParameterType.ENTITY == parameter.getType() && resultingParamValue instanceof Collection;
        boolean moreThanOneEntitySelected = resultingParamValue instanceof Collection && ((Collection) resultingParamValue).size() > 1;

        if (reportHasMoreThanOneParameter || inputParametersRequiredByTemplates) {
            boolean bulkPrint = reportTypeIsSingleEntity && moreThanOneEntitySelected;
            openReportParamsDialog(screen, report,
                    ParamsMap.of(parameter.getAlias(), resultingParamValue),
                    parameter, templateCode, outputFileName,bulkPrint);
        } else {
            if (reportTypeIsSingleEntity) {
                Collection selectedEntities = (Collection) resultingParamValue;
                if (moreThanOneEntitySelected) {
                    bulkPrint(report, parameter.getAlias(), selectedEntities, screen);
                } else if (selectedEntities.size() == 1) {
                    printReport(report, ParamsMap.of(parameter.getAlias(), selectedEntities.iterator().next()), templateCode, outputFileName, screen);
                }
            } else {
                printReport(report, ParamsMap.of(parameter.getAlias(), resultingParamValue), templateCode, outputFileName, screen);
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
     * @param screen - caller window
     */
    public void printReport(Report report, Map<String, Object> params, FrameOwner screen) {
        printReport(report, params, null, null, screen);
    }

    /**
     * Print report synchronously or asynchronously, depending on configurations
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     * @param screen         - caller window
     */
    public void printReport(Report report, Map<String, Object> params, @Nullable String templateCode,
                            @Nullable String outputFileName, @Nullable FrameOwner screen) {
        printReport(report, params, templateCode, outputFileName, null, screen);
    }

    /**
     * Print report synchronously or asynchronously, depending on configurations
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     * @param outputType     - output type for file
     * @param screen         - caller window
     */
    public void printReport(Report report, Map<String, Object> params, @Nullable String templateCode,
                            @Nullable String outputFileName, @Nullable ReportOutputType outputType, FrameOwner screen) {

        Configuration configuration = AppBeans.get(Configuration.NAME);
        ReportingClientConfig reportingClientConfig = configuration.getConfig(ReportingClientConfig.class);

        if (screen != null && reportingClientConfig.getUseBackgroundReportProcessing()) {
            printReportBackground(report, params, templateCode, outputFileName, outputType, screen);
        } else {
            printReportSync(report, params, templateCode, outputFileName, outputType, screen);
        }
    }

    /**
     * Print report synchronously
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     * @param screen         - caller window
     */
    public void printReportSync(Report report, Map<String, Object> params, @Nullable String templateCode,
                                @Nullable String outputFileName, @Nullable FrameOwner screen) {
        printReportSync(report, params, templateCode, outputFileName, null, screen);
    }

    /**
     * Print report synchronously
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     * @param outputType     - output type for file
     * @param screen         - caller window
     */
    public void printReportSync(Report report, Map<String, Object> params, @Nullable String templateCode,
                                @Nullable String outputFileName, @Nullable ReportOutputType outputType,
                                @Nullable FrameOwner screen) {
        ReportOutputDocument document = getReportResult(report, params, templateCode, outputType);

        showReportResult(document, params, templateCode, outputFileName, screen);
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
                                    @Nullable String templateCode, @Nullable String outputFileName, @Nullable FrameOwner screen) {
        showReportResult(document, params, templateCode, outputFileName, null, screen);
    }

    protected void showReportResult(ReportOutputDocument document, Map<String, Object> params,
                                    @Nullable String templateCode, @Nullable String outputFileName,
                                    @Nullable ReportOutputType outputType, @Nullable FrameOwner screen) {

        WindowManager wm = windowManagerProvider.get();

        if (document.getReportOutputType().getId().equals(CubaReportOutputType.chart.getId())) {
            Map<String, Object> screenParams = new HashMap<>();
            screenParams.put(ShowChartController.CHART_JSON_PARAMETER, new String(document.getContent(), StandardCharsets.UTF_8));
            screenParams.put(ShowChartController.REPORT_PARAMETER, document.getReport());
            screenParams.put(ShowChartController.TEMPLATE_CODE_PARAMETER, templateCode);
            screenParams.put(ShowChartController.PARAMS_PARAMETER, params);

            WindowInfo windowInfo = windowConfig.getWindowInfo("report$showChart");

            if (screen != null) {
                ScreenContext screenContext = UiControllerUtils.getScreenContext(screen);

                WindowManager screens = (WindowManager) screenContext.getScreens();
                screens.openWindow(windowInfo, OpenType.DIALOG, screenParams);
            } else {
                wm.openWindow(windowInfo, OpenType.DIALOG, screenParams);
            }
        } else if (document.getReportOutputType().getId().equals(CubaReportOutputType.pivot.getId())) {
            Map<String, Object> screenParams = ParamsMap.of(
                    ShowPivotTableController.PIVOT_TABLE_DATA_PARAMETER, document.getContent(),
                    ShowPivotTableController.REPORT_PARAMETER, document.getReport(),
                    ShowPivotTableController.TEMPLATE_CODE_PARAMETER, templateCode,
                    ShowPivotTableController.PARAMS_PARAMETER, params);

            WindowInfo windowInfo = windowConfig.getWindowInfo("report$showPivotTable");

            if (screen != null) {
                ScreenContext screenContext = UiControllerUtils.getScreenContext(screen);

                WindowManager screens = (WindowManager) screenContext.getScreens();
                screens.openWindow(windowInfo, OpenType.DIALOG, screenParams);
            } else {
                wm.openWindow(windowInfo, OpenType.DIALOG, screenParams);
            }
        } else if (document.getReportOutputType().getId().equals(CubaReportOutputType.table.getId())) {
            Map<String, Object> screenParams = new HashMap<>();
            screenParams.put(ShowReportTable.TABLE_DATA_PARAMETER, document.getContent());
            screenParams.put(ShowReportTable.REPORT_PARAMETER, document.getReport());
            screenParams.put(ShowReportTable.TEMPLATE_CODE_PARAMETER, templateCode);
            screenParams.put(ShowReportTable.PARAMS_PARAMETER, params);

            WindowInfo windowInfo = windowConfig.getWindowInfo("report$showReportTable");

            if (screen != null) {
                ScreenContext screenContext = UiControllerUtils.getScreenContext(screen);

                WindowManager screens = (WindowManager) screenContext.getScreens();
                screens.openWindow(windowInfo, OpenType.DIALOG, screenParams);
            } else {
                wm.openWindow(windowInfo, OpenType.DIALOG, screenParams);
            }
        } else {
            ExportDisplay exportDisplay;
            byte[] byteArr = document.getContent();
            com.haulmont.yarg.structure.ReportOutputType finalOutputType =
                    (outputType != null) ? outputType.getOutputType() : document.getReportOutputType();

            ExportFormat exportFormat = ReportPrintHelper.getExportFormat(finalOutputType);

            if(screen != null){
                Screen hostScreen = UiControllerUtils.getScreen(screen);
                exportDisplay = AppConfig.createExportDisplay(hostScreen.getWindow());
            } else {
                exportDisplay = AppConfig.createExportDisplay(null);
            }
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
     * @param screen         - caller window
     */
    public void printReportBackground(Report report, Map<String, Object> params,
                                      @Nullable String templateCode, @Nullable String outputFileName, FrameOwner screen) {
        printReportBackground(report, params, templateCode, outputFileName, null, screen);
    }

    /**
     * Print report in background task with window, supports cancel
     *
     * @param report         - target report
     * @param params         - report parameters (map keys should match with parameter aliases)
     * @param templateCode   - target template code
     * @param outputFileName - name for output file
     * @param outputType     - output type for file
     * @param screen         - caller window
     */
    public void printReportBackground(Report report, final Map<String, Object> params, @Nullable String templateCode,
                                      @Nullable String outputFileName, @Nullable ReportOutputType outputType, FrameOwner screen) {
        ReportingClientConfig reportingClientConfig = configuration.getConfig(ReportingClientConfig.class);

        Report targetReport = getReportForPrinting(report);

        long timeout = reportingClientConfig.getBackgroundReportProcessingTimeoutMs();
        UUID userSessionId = userSessionSource.getUserSession().getId();

        Screen hostScreen = UiControllerUtils.getScreen(screen);

        BackgroundTask<Void, ReportOutputDocument> task =
                new BackgroundTask<Void, ReportOutputDocument>(timeout, TimeUnit.MILLISECONDS, hostScreen) {

            @SuppressWarnings("UnnecessaryLocalVariable")
            @Override
            public ReportOutputDocument run(TaskLifeCycle<Void> taskLifeCycle) {
                ReportOutputDocument result = getReportResult(targetReport, params, templateCode, outputType);
                return result;
            }

            @Override
            public void done(ReportOutputDocument document) {
                showReportResult(document, params, templateCode, outputFileName, outputType, screen);
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
        lc.setQueryString("select r from report$Report r where r.system <> true");
        reportSecurityManager.applySecurityPolicies(lc, screenId, user);
        reportSecurityManager.applyPoliciesByEntityParameters(lc, inputValueMetaClass);
        return dataService.loadList(lc);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Synchronously or asynchronously, depending on configurations
     *
     * @param report               - target report
     * @param templateCode         - target template code
     * @param outputType           - output type for file
     * @param alias                - parameter alias
     * @param selectedEntities     - list of selected entities
     * @param screen               - caller window
     * @param additionalParameters - user-defined parameters
     */
    public void bulkPrint(Report report, @Nullable String templateCode, @Nullable ReportOutputType outputType, String alias,
                          Collection selectedEntities, @Nullable FrameOwner screen, @Nullable Map<String, Object> additionalParameters) {
        ReportingClientConfig reportingClientConfig = configuration.getConfig(ReportingClientConfig.class);
        if (screen != null && reportingClientConfig.getUseBackgroundReportProcessing()) {
            bulkPrintBackground(report, templateCode, outputType, alias, selectedEntities, screen, additionalParameters);
        } else {
            bulkPrintSync(report, templateCode, outputType, alias, selectedEntities, screen, additionalParameters);
        }
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Synchronously or asynchronously, depending on configurations
     *
     * @param report               target report
     * @param alias                parameter alias
     * @param selectedEntities     list of selected entities
     * @param screen               caller window
     * @param additionalParameters user-defined parameters
     */
    public void bulkPrint(Report report, String alias, Collection selectedEntities, @Nullable FrameOwner screen,
                          @Nullable Map<String, Object> additionalParameters) {
        bulkPrint(report, null, null, alias, selectedEntities, screen, additionalParameters);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as inputParameter with certain alias.
     * Synchronously or asynchronously, depending on configurations
     *
     * @param report           - target report
     * @param alias            - inputParameter alias
     * @param selectedEntities - list of selected entities
     * @param screen           - caller window
     */
    public void bulkPrint(Report report, String alias, Collection selectedEntities, @Nullable FrameOwner screen) {
        bulkPrint(report, alias, selectedEntities, screen, null);
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
     * @param report               - target report
     * @param alias                - parameter alias
     * @param selectedEntities     - list of selected entities
     * @param screen               - caller window
     * @param additionalParameters - user-defined parameters
     */
    public void bulkPrintSync(Report report, String alias, Collection selectedEntities, @Nullable FrameOwner screen,
                              Map<String, Object> additionalParameters) {
        bulkPrintSync(report, null, null, alias, selectedEntities, screen, additionalParameters);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Synchronously.
     *
     * @param report               - target report
     * @param templateCode         - target template code
     * @param outputType           - output type for file
     * @param alias                - parameter alias
     * @param selectedEntities     - list of selected entities
     * @param screen               - caller window
     * @param additionalParameters - user-defined parameters
     */
    public void bulkPrintSync(Report report, @Nullable String templateCode, @Nullable ReportOutputType outputType,
                              String alias, Collection selectedEntities, @Nullable FrameOwner screen,
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

        Screen hostScreen = UiControllerUtils.getScreen(screen);

        ReportOutputDocument reportOutputDocument = reportService.bulkPrint(report, templateCode, outputType, paramsList);
        ExportDisplay exportDisplay = AppConfig.createExportDisplay(hostScreen.getWindow());
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
     * @param screen           - caller window
     */
    public void bulkPrintSync(Report report, String alias, Collection selectedEntities, @Nullable FrameOwner screen) {
        bulkPrintSync(report, alias, selectedEntities, screen, null);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Asynchronously.
     *
     * @param report               - target report
     * @param alias                - parameter alias
     * @param selectedEntities     - list of selected entities
     * @param window               - caller window
     * @param additionalParameters - user-defined parameters
     */
    public void bulkPrintBackground(Report report, String alias, Collection selectedEntities, FrameOwner window,
                                    Map<String, Object> additionalParameters) {
        bulkPrintBackground(report, null, null, alias, selectedEntities, window, additionalParameters);
    }

    /**
     * Print certain reports for list of entities and pack result files into ZIP.
     * Each entity is passed  to report as parameter with certain alias.
     * Asynchronously.
     *
     * @param report               - target report
     * @param templateCode         - target template code
     * @param outputType           - output type for file
     * @param alias                - parameter alias
     * @param selectedEntities     - list of selected entities
     * @param screen               - caller window
     * @param additionalParameters - user-defined parameters
     */
    public void bulkPrintBackground(Report report, @Nullable String templateCode, @Nullable ReportOutputType outputType,
                                    String alias, Collection selectedEntities, FrameOwner screen,
                                    Map<String, Object> additionalParameters) {
        ReportingClientConfig reportingClientConfig = configuration.getConfig(ReportingClientConfig.class);

        Report targetReport = getReportForPrinting(report);

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

        Screen hostScreen = UiControllerUtils.getScreen(screen);

        BackgroundTask<Void, ReportOutputDocument> task =
                new BackgroundTask<Void, ReportOutputDocument>(timeout, TimeUnit.MILLISECONDS, hostScreen) {
            @SuppressWarnings("UnnecessaryLocalVariable")
            @Override
            public ReportOutputDocument run(TaskLifeCycle<Void> taskLifeCycle) {
                ReportOutputDocument result = reportService.bulkPrint(targetReport, templateCode, outputType, paramsList);
                return result;
            }

            @Override
            public void done(ReportOutputDocument result) {
                ExportDisplay exportDisplay = AppConfig.createExportDisplay(hostScreen.getWindow());
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
     * @param screen           - caller window
     */
    public void bulkPrintBackground(Report report, String alias, Collection selectedEntities, FrameOwner screen) {
        bulkPrintBackground(report, alias, selectedEntities, screen, null);
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

    protected void openReportParamsDialog(FrameOwner screen, Report report, @Nullable Map<String, Object> parameters,
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

        ScreenContext screenContext = UiControllerUtils.getScreenContext(screen);

        WindowManager wm = (WindowManager) screenContext.getScreens();
        WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo("report$inputParameters");

        wm.openWindow(windowInfo, OpenType.DIALOG, params);
    }

    protected void openReportParamsDialog(FrameOwner screen, Report report, @Nullable Map<String, Object> parameters,
                                          @Nullable String templateCode, @Nullable String outputFileName) {
        openReportParamsDialog(screen, report, parameters, null, templateCode, outputFileName, false);
    }

    @Nullable
    protected Object convertParameterIfNecessary(ReportInputParameter parameter, @Nullable Object paramValue,
                                                 boolean useForInputParametersForm) {
        Object resultingParamValue = paramValue;
        if (ParameterType.ENTITY == parameter.getType()) {
            if (paramValue instanceof Collection || paramValue instanceof ParameterPrototype) {
                resultingParamValue = handleCollectionParameter(paramValue,
                        useForInputParametersForm);
            }
        } else if (ParameterType.ENTITY_LIST == parameter.getType()) {
            if (!(paramValue instanceof Collection) && !(paramValue instanceof ParameterPrototype)) {
                resultingParamValue = Collections.singletonList(paramValue);
            } else if (paramValue instanceof ParameterPrototype && useForInputParametersForm) {
                resultingParamValue = handleCollectionParameter(paramValue, false);
            }
        }

        return resultingParamValue;
    }

    @Nullable
    protected Object handleCollectionParameter(@Nullable Object paramValue, boolean convertToSingleItem) {
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

        if (convertToSingleItem && paramValueWithCollection.size() == 1) {
            //if the case of several params we can not do bulk print, because the params should be filled, so we get only first object from the list
            return paramValueWithCollection.iterator().next();
        }

        return paramValueWithCollection;
    }

    public boolean inputParametersRequiredByTemplates(Report report) {
        return report.getTemplates() != null && report.getTemplates().size() > 1 || containsAlterableTemplate(report);
    }

    public boolean containsAlterableTemplate(Report report) {
        if (report.getTemplates() == null)
            return false;
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