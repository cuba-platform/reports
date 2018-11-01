/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.report.run;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.theme.ThemeConstants;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.gui.ReportGuiManager;
import com.haulmont.yarg.reporting.ReportOutputDocument;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ShowChartController extends AbstractWindow {
    public static final String JSON_CHART_SCREEN_ID = "chart$jsonChart";

    public static final String CHART_JSON_PARAMETER = "chartJson";
    public static final String REPORT_PARAMETER = "report";
    public static final String TEMPLATE_CODE_PARAMETER = "templateCode";
    public static final String PARAMS_PARAMETER = "reportParams";

    @Inject
    protected GroupBoxLayout reportParamsBox;

    @Inject
    protected GroupBoxLayout chartBox;

    @Inject
    protected ReportGuiManager reportGuiManager;

    @Inject
    protected ThemeConstants themeConstants;

    @Inject
    protected LookupField<Report> reportLookup;

    @Inject
    protected ComponentsFactory componentsFactory;

    @Inject
    protected Button printReportBtn;

    @Inject
    protected BoxLayout parametersFrameHolder;

    @Inject
    protected HBoxLayout reportSelectorBox;

    @Inject
    protected WindowConfig windowConfig;

    protected InputParametersFrame inputParametersFrame;

    protected Report report;

    protected String templateCode;

    @Override
    public void init(final Map<String, Object> params) {
        super.init(params);

        getDialogOptions()
                .setWidth(themeConstants.get("cuba.gui.report.ShowChartController.width"))
                .setHeight(themeConstants.get("cuba.gui.report.ShowChartController.height"))
                .setResizable(true);

        String chartJson = (String) params.get(CHART_JSON_PARAMETER);
        report = (Report) params.get(REPORT_PARAMETER);
        templateCode = (String) params.get(TEMPLATE_CODE_PARAMETER);
        @SuppressWarnings("unchecked")
        Map<String, Object> reportParameters = (Map<String, Object>) params.get(PARAMS_PARAMETER);

        if (!windowConfig.hasWindow(JSON_CHART_SCREEN_ID)) {
            showChartsNotIncluded();
            return;
        }

        if (report != null) {
            reportSelectorBox.setVisible(false);

            initFrames(chartJson, reportParameters);
        } else {
            showDiagramStubText();
        }

        reportLookup.addValueChangeListener(e -> {
            report = (Report) e.getValue();
            initFrames(null, null);
        });
    }

    protected void initFrames(String chartJson, Map<String, Object> reportParameters) {
        openChart(chartJson);
        openReportParameters(reportParameters);
    }

    private void openReportParameters(Map<String, Object> reportParameters) {
        parametersFrameHolder.removeAll();

        if (report != null) {
            Map<String, Object> params = ParamsMap.of(
                    InputParametersFrame.REPORT_PARAMETER, report,
                    InputParametersFrame.PARAMETERS_PARAMETER, reportParameters
            );

            inputParametersFrame = (InputParametersFrame) openFrame(parametersFrameHolder,
                    "report$inputParametersFrame", params);

            reportParamsBox.setVisible(true);
        } else {
            reportParamsBox.setVisible(false);
        }
    }

    protected void openChart(String chartJson) {
        chartBox.removeAll();
        if (chartJson != null) {
            openFrame(chartBox, JSON_CHART_SCREEN_ID, ParamsMap.of(CHART_JSON_PARAMETER, chartJson));
        }

        showDiagramStubText();
    }

    protected void showDiagramStubText() {
        if (chartBox.getOwnComponents().isEmpty()) {
            Label label = componentsFactory.createComponent(Label.class);
            label.setValue(getMessage("showChart.caption"));
            label.setAlignment(Alignment.MIDDLE_CENTER);
            label.setStyleName("h1");
            chartBox.add(label);
        }
    }

    protected void showChartsNotIncluded() {
        reportLookup.setEditable(false);
        chartBox.removeAll();
        Label label = componentsFactory.createComponent(Label.class);
        label.setValue(getMessage("showChart.noChartComponent"));
        label.setAlignment(Alignment.MIDDLE_CENTER);
        label.setStyleName("h1");
        chartBox.add(label);
    }

    public void printReport() {
        if (inputParametersFrame != null && inputParametersFrame.getReport() != null) {
            if (validateAll()) {
                Map<String, Object> parameters = inputParametersFrame.collectParameters();
                Report report = inputParametersFrame.getReport();

                ReportOutputDocument reportResult = reportGuiManager.getReportResult(report, parameters, templateCode);
                openChart(new String(reportResult.getContent(), StandardCharsets.UTF_8));
            }
        }
    }
}