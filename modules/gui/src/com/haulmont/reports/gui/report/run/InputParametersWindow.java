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
package com.haulmont.reports.gui.report.run;

import com.haulmont.bali.util.Preconditions;
import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.reports.exception.ReportParametersValidationException;
import com.haulmont.reports.gui.ReportGuiManager;
import com.haulmont.reports.gui.ReportParameterValidator;
import org.apache.commons.lang3.BooleanUtils;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

import static com.haulmont.reports.gui.report.run.InputParametersFrame.PARAMETERS_PARAMETER;

public class InputParametersWindow extends AbstractWindow {
    public static final String TEMPLATE_CODE_PARAMETER = "templateCode";
    public static final String OUTPUT_FILE_NAME_PARAMETER = "outputFileName";
    public static final String INPUT_PARAMETER = "inputParameter";
    public static final String BULK_PRINT = "bulkPrint";
    public static final String REPORT_PARAMETER = "report";

    protected String templateCode;

    protected String outputFileName;

    protected boolean bulkPrint;

    protected Report report;

    protected ReportInputParameter inputParameter;

    protected Collection selectedEntities;

    @Inject
    protected ReportGuiManager reportGuiManager;

    @Inject
    protected ClientConfig clientConfig;

    @Inject
    protected Button printReportBtn;

    @Inject
    protected InputParametersFrame inputParametersFrame;

    @Inject
    protected ReportParameterValidator reportParameterValidator;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

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

        report = (Report) params.get(REPORT_PARAMETER);

        Action printReportAction = printReportBtn.getAction();
        String commitShortcut = clientConfig.getCommitShortcut();
        printReportAction.setShortcut(commitShortcut);
        addAction(printReportAction);
    }

    @Override
    public void ready() {
        super.ready();
        inputParametersFrame.initTemplateAndOutputSelect();
    }

    public void printReport() {
        if (inputParametersFrame.getReport() != null) {
            if (validateAll()) {
                ReportTemplate template = inputParametersFrame.getReportTemplate();
                if (template != null) {
                    templateCode = template.getCode();
                }
                Report report = inputParametersFrame.getReport();
                Map<String, Object> parameters = inputParametersFrame.collectParameters();
                if (bulkPrint) {
                    reportGuiManager.bulkPrint(report, templateCode, inputParametersFrame.getOutputType(), inputParameter.getAlias(), selectedEntities, this, parameters);
                } else {
                    reportGuiManager.printReport(report, parameters, templateCode, outputFileName, inputParametersFrame.getOutputType(), this);
                }
            }
        }
    }

    @Override
    public boolean validateAll() {
        return super.validateAll() && crossValidateParameters();
    }

    protected boolean crossValidateParameters() {
        boolean isValid = true;
        if (BooleanUtils.isTrue(inputParametersFrame.getReport().getValidationOn())) {
            try {
                reportParameterValidator.crossValidateParameters(inputParametersFrame.getReport(),
                        inputParametersFrame.collectParameters());
            } catch (ReportParametersValidationException e) {
                NotificationType notificationType = NotificationType.valueOf(clientConfig.getValidationNotificationType());
                showNotification(messages.getMainMessage("validationFail.caption"), e.getMessage(), notificationType);
                isValid = false;
            }
        }

        return isValid;
    }

    public void cancel() {
        close(CLOSE_ACTION_ID);
    }
}