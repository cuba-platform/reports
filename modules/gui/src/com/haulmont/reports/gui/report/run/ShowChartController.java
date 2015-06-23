/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.report.run;

import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.gui.theme.ThemeConstants;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.gui.ReportGuiManager;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class ShowChartController extends AbstractWindow {
    public static final String JSON_CHART_SCREEN_ID = "chart$jsonChart";

    public static final String CHART_JSON_PARAMETER = "chartJson";
    public static final String REPORT_PARAMETER = "report";
    public static final String TEMPLATE_CODE_PARAMETER = "templateCode";

    @Inject
    protected GroupBoxLayout reportParamsBox;

    @Inject
    protected GroupBoxLayout chartBox;

    @Inject
    protected ReportGuiManager reportGuiManager;

    @Inject
    protected ThemeConstants themeConstants;

    @Inject
    protected LookupField reportLookup;

    @Inject
    private Label reportLookupLabel;

    @Inject
    private ComponentsFactory componentsFactory;

    protected InputParametersController inputParametersController;

    protected Report report;

    protected String templateCode;

    @Override
    public void init(final Map<String, Object> params) {
        super.init(params);
        getDialogParams().setWidth(themeConstants.getInt("cuba.gui.report.ShowChartController.width"))
                .setHeight(themeConstants.getInt("cuba.gui.report.ShowChartController.height"))
                .setResizable(true);
        String chartJson = (String) params.get(CHART_JSON_PARAMETER);
        report = (Report) params.get(REPORT_PARAMETER);
        templateCode = (String) params.get(TEMPLATE_CODE_PARAMETER);

        if (report != null) {
            reportLookup.setVisible(false);
            reportLookupLabel.setVisible(false);
            initFrames(chartJson);
        } else {
            showDiagramStubText();
        }

        reportLookup.addListener(new ValueListener() {
            @Override
            public void valueChanged(Object source, String property, @Nullable Object prevValue, @Nullable Object value) {
                report = (Report) value;
                initFrames(null);
            }
        });
    }

    protected void initFrames(String chartJson) {
        openChart(chartJson);
        openReportParameters();
    }

    private void openReportParameters() {
        reportParamsBox.removeAll();
        if (report != null) {
            inputParametersController = openFrame(reportParamsBox, "report$inputParameters",
                    Collections.<String, Object>singletonMap(InputParametersController.REPORT_PARAMETER, report));

            inputParametersController.setPrintReportHandler(new InputParametersController.PrintReportHandler() {
                @Override
                public void handle() {
                    try {
                        inputParametersController.validate();
                        Map<String, Object> parameters = inputParametersController.collectParameters();
                        ReportOutputDocument reportResult = reportGuiManager.getReportResult(report, parameters, templateCode);
                        openChart(new String(reportResult.getContent()));
                    } catch (ValidationException e) {
                        showNotification(getMessage("validationFail.caption"), e.getLocalizedMessage(), NotificationType.TRAY);
                    }
                }
            });
        }
    }

    protected void openChart(String chartJson) {
        chartBox.removeAll();
        if (chartJson != null) {
            openFrame(chartBox, JSON_CHART_SCREEN_ID,
                    Collections.<String, Object>singletonMap("Chart", chartJson));
        }

        showDiagramStubText();
    }

    private void showDiagramStubText() {
        if (CollectionUtils.isEmpty(chartBox.getComponents())) {
            Label label = componentsFactory.createComponent(Label.NAME);
            label.setValue(getMessage("showChart.caption"));
            label.setAlignment(Alignment.MIDDLE_CENTER);
            label.setStyleName("h1");
            chartBox.add(label);
        }
    }
}
