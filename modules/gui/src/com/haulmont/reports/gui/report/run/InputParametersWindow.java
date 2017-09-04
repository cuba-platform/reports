/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports.gui.report.run;

import com.haulmont.bali.util.Preconditions;
import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.gui.ReportGuiManager;
import org.apache.commons.lang.BooleanUtils;

import javax.inject.Inject;
import java.util.*;

import static com.haulmont.reports.gui.report.run.InputParametersFrame.PARAMETERS_PARAMETER;

public class InputParametersWindow extends AbstractWindow {
    public static final String TEMPLATE_CODE_PARAMETER = "templateCode";
    public static final String OUTPUT_FILE_NAME_PARAMETER = "outputFileName";
    public static final String INPUT_PARAMETER = "inputParameter";
    public static final String BULK_PRINT = "bulkPrint";

    protected String templateCode;

    protected String outputFileName;

    protected boolean bulkPrint;

    protected ReportInputParameter inputParameter;

    protected Collection selectedEntities;

    @Inject
    protected ReportGuiManager reportGuiManager;

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
        bulkPrint = BooleanUtils.isTrue((Boolean) params.get(BULK_PRINT));
        inputParameter = (ReportInputParameter) params.get(INPUT_PARAMETER);

        if (bulkPrint) {
            Preconditions.checkNotNullArgument(inputParameter, String.format("%s is null for bulk print", INPUT_PARAMETER));
            //noinspection unchecked
            Map<String, Object> parameters = (Map<String, Object>) params.get(PARAMETERS_PARAMETER);
            selectedEntities = (Collection) parameters.get(inputParameter.getAlias());
        }

        Action printReportAction = printReportBtn.getAction();
        String commitShortcut = clientConfig.getCommitShortcut();
        printReportAction.setShortcut(commitShortcut);
        addAction(printReportAction);
    }

    public void printReport() {
        if (inputParametersFrame.getReport() != null) {
            if (validateAll()) {
                Report report = inputParametersFrame.getReport();
                Map<String, Object> parameters = inputParametersFrame.collectParameters();
                if (bulkPrint) {
                    reportGuiManager.bulkPrint(report, inputParameter.getAlias(), selectedEntities, this, parameters);
                } else {
                    reportGuiManager.printReport(report, parameters, templateCode, outputFileName, this);
                }
            }
        }
    }

    public void cancel() {
        close(CLOSE_ACTION_ID);
    }
}