/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.report.run;

import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.BoxLayout;
import com.haulmont.cuba.gui.components.GroupBoxLayout;
import com.haulmont.cuba.gui.components.ValidationException;
import com.haulmont.cuba.gui.theme.ThemeConstants;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.gui.ReportGuiManager;
import com.haulmont.yarg.reporting.ReportOutputDocument;

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
    protected BoxLayout chartBox;

    @Inject
    protected ReportGuiManager reportGuiManager;

    @Inject
    protected ThemeConstants themeConstants;

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

        inputParametersController = openFrame(reportParamsBox, "report$inputParameters",
                Collections.<String, Object>singletonMap(InputParametersController.REPORT_PARAMETER, report));
        openFrame(chartBox, JSON_CHART_SCREEN_ID, Collections.<String, Object>singletonMap("Chart", chartJson));

        inputParametersController.setPrintReportHandler(new InputParametersController.PrintReportHandler() {
            @Override
            public void handle() {
                try {
                    inputParametersController.validate();
                    Map<String, Object> parameters = inputParametersController.collectParameters();
                    ReportOutputDocument reportResult = reportGuiManager.getReportResult(report, parameters, templateCode);
                    chartBox.removeAll();
                    openFrame(chartBox, JSON_CHART_SCREEN_ID,
                            Collections.<String, Object>singletonMap("Chart", new String(reportResult.getContent())));
                } catch (ValidationException e) {
                    showNotification(getMessage("validationFail.caption"), e.getLocalizedMessage(), NotificationType.TRAY);
                }
            }
        });
    }
}
