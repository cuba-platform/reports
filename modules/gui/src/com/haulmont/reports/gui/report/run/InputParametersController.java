/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.gui.report.run;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.gui.ReportGuiManager;
import org.apache.commons.collections.CollectionUtils;

import javax.inject.Inject;
import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class InputParametersController extends AbstractWindow {
    public static final String REPORT_PARAMETER = "report";
    public static final String PARAMETERS_PARAMETER = "parameters";
    public static final String TEMPLATE_CODE_PARAMETER = "templateCode";
    public static final String OUTPUT_FILE_NAME_PARAMETER = "outputFileName";

    protected Report report;

    protected Map<String, Object> parameters;

    protected String templateCode;

    protected String outputFileName;

    @Inject
    protected Messages messages;

    @Inject
    protected Metadata metadata;

    @Inject
    protected GridLayout parametersGrid;

    @Inject
    protected ReportGuiManager reportGuiManager;

    @Inject
    protected ReportService reportService;

    protected HashMap<String, Field> parameterComponents = new HashMap<>();

    protected ParameterFieldCreator parameterFieldCreator = new ParameterFieldCreator(this);

    protected ParameterClassResolver parameterClassResolver = new ParameterClassResolver();

    protected PrintReportHandler printReportHandler;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        report = (Report) params.get(REPORT_PARAMETER);
        parameters = (Map<String, Object>) params.get(PARAMETERS_PARAMETER);
        templateCode = (String) params.get(TEMPLATE_CODE_PARAMETER);
        outputFileName = (String) params.get(OUTPUT_FILE_NAME_PARAMETER);

        if (parameters == null) {
            parameters = Collections.emptyMap();
        }

        if (report != null) {
            if (!report.getIsTmp()) {
                report = getDsContext().getDataSupplier().reload(report, ReportService.MAIN_VIEW_NAME);
            }
            if (CollectionUtils.isNotEmpty(report.getInputParameters())) {
                parametersGrid.setRows(report.getInputParameters().size());

                int currentGridRow = 0;
                for (ReportInputParameter parameter : report.getInputParameters()) {
                    createComponent(parameter, currentGridRow);
                    currentGridRow++;
                }
            }
        }
    }

    public void printReport() {
        if (report != null) {
            if (printReportHandler == null) {
                if (validateAll()) {
                    reportGuiManager.printReport(report, collectParameters(), templateCode, outputFileName, this);
                }
            } else {
                printReportHandler.handle();
            }
        }
    }

    public Map<String, Object> collectParameters() {
        Map<String, Object> parameters = new HashMap<>();
        for (String paramName : parameterComponents.keySet()) {
            Field parameterField = parameterComponents.get(paramName);
            Object value = parameterField.getValue();
            parameters.put(paramName, value);
        }
        return parameters;
    }

    protected void createComponent(ReportInputParameter parameter, int currentGridRow) {
        Field field = parameterFieldCreator.createField(parameter);
        field.setWidth("400px");

        Object value = parameters.get(parameter.getAlias());

        if (value == null && parameter.getDefaultValue() != null) {
            Class parameterClass = parameterClassResolver.resolveClass(parameter);
            if (parameterClass != null) {
                value = reportService.convertFromString(parameterClass, parameter.getDefaultValue());
            }
        }

        if (!(field instanceof TokenList)) {
            field.setValue(value);
        } else {
            CollectionDatasource datasource = (CollectionDatasource) field.getDatasource();
            if (value instanceof Collection) {
                Collection collection = (Collection) value;
                for (Object selected : collection) {
                    datasource.includeItem((Entity) selected);
                }
            }
        }

        Label label = parameterFieldCreator.createLabel(parameter, field);

        if (currentGridRow == 0) {
            field.requestFocus();
        }

        parameterComponents.put(parameter.getAlias(), field);
        parametersGrid.add(label, 0, currentGridRow);
        parametersGrid.add(field, 1, currentGridRow);
    }

    public void setPrintReportHandler(PrintReportHandler printReportHandler) {
        this.printReportHandler = printReportHandler;
    }

    public interface PrintReportHandler {
        public void handle();
    }
}