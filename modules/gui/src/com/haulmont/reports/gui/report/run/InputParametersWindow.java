/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports.gui.report.run;

import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.reports.gui.ReportGuiManager;

import javax.inject.Inject;
import java.util.Map;

public class InputParametersWindow extends AbstractWindow {
    public static final String TEMPLATE_CODE_PARAMETER = "templateCode";
    public static final String OUTPUT_FILE_NAME_PARAMETER = "outputFileName";

    protected String templateCode;

    protected String outputFileName;

    @Inject
    protected ReportGuiManager reportGuiManager;

    @Inject
    protected Configuration configuration;

    @Inject
    protected ClientConfig clientConfig;

    @Inject
    protected Button printReportBtn;

    @Inject
    private InputParametersFrame inputParametersFrame;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        //if there is no strong external requirements for width - set auto width, so long parameter names will fit well
        if (getDialogOptions().getWidth() == null) {
            getDialogOptions().setWidthAuto();
        }

        //noinspection unchecked
        templateCode = (String) params.get(TEMPLATE_CODE_PARAMETER);
        outputFileName = (String) params.get(OUTPUT_FILE_NAME_PARAMETER);

        Action printReportAction = printReportBtn.getAction();
        String commitShortcut = clientConfig.getCommitShortcut();
        printReportAction.setShortcut(commitShortcut);
        addAction(printReportAction);
    }

    public void printReport() {
        if (inputParametersFrame.getReport() != null) {
            if (validateAll()) {
                reportGuiManager.printReport(inputParametersFrame.getReport(), inputParametersFrame.collectParameters(),
                        templateCode, outputFileName, this);
            }
        }
    }

    public void cancel() {
        close(CLOSE_ACTION_ID);
    }
}