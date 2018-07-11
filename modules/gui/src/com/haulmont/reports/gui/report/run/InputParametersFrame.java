/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.report.run;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.entity.ReportOutputType;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.reports.gui.ReportGuiManager;
import com.haulmont.reports.gui.ReportPrintHelper;
import com.haulmont.reports.gui.report.validators.ReportParamFieldValidator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import javax.inject.Inject;
import java.util.*;

import static com.haulmont.reports.gui.report.run.InputParametersWindow.BULK_PRINT;
import static com.haulmont.reports.gui.report.run.InputParametersWindow.INPUT_PARAMETER;

public class InputParametersFrame extends AbstractFrame {
    public static final String REPORT_PARAMETER = "report";
    public static final String PARAMETERS_PARAMETER = "parameters";

    protected Report report;
    protected Map<String, Object> parameters;
    protected boolean bulkPrint;
    protected ReportInputParameter inputParameter;

    @Inject
    protected LookupField<ReportTemplate> templateField;

    @Inject
    protected LookupField<ReportOutputType> outputTypeField;

    @Inject
    protected Label outputTypeLbl;

    @Inject
    protected Label templateLbl;

    @Inject
    protected GridLayout parametersGrid;

    @Inject
    protected CollectionDatasource<ReportTemplate, UUID> templateReportsDs;

    @Inject
    protected ReportService reportService;

    @Inject
    protected DataSupplier dataSupplier;

    @Inject
    protected ReportGuiManager reportGuiManager;

    protected HashMap<String, Field> parameterComponents = new HashMap<>();

    protected ParameterFieldCreator parameterFieldCreator = new ParameterFieldCreator(this);

    protected ParameterClassResolver parameterClassResolver = new ParameterClassResolver();

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        report = (Report) params.get(REPORT_PARAMETER);
        if (report != null && !report.getIsTmp()) {
            report = dataSupplier.reload(report, ReportService.MAIN_VIEW_NAME);
        }
        //noinspection unchecked
        parameters = (Map<String, Object>) params.get(PARAMETERS_PARAMETER);
        if (parameters == null) {
            parameters = Collections.emptyMap();
        }
        bulkPrint = BooleanUtils.isTrue((Boolean) params.get(BULK_PRINT));
        inputParameter = (ReportInputParameter) params.get(INPUT_PARAMETER);

        if (report != null) {
            if (CollectionUtils.isNotEmpty(report.getInputParameters())) {
                parametersGrid.setRows(report.getInputParameters().size() + 2);
                int currentGridRow = 2;
                for (ReportInputParameter parameter : report.getInputParameters()) {
                    if (bulkPrint && Objects.equals(inputParameter, parameter)) {
                        continue;
                    }
                    createComponent(parameter, currentGridRow, BooleanUtils.isNotTrue(parameter.getHidden()));
                    currentGridRow++;
                }
            }
            if (report.getTemplates() != null && report.getTemplates().size() > 1) {
                if (!report.getIsTmp()) {
                    templateReportsDs.refresh(ParamsMap.of("reportId", report.getId()));
                }
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

    protected void createComponent(ReportInputParameter parameter, int currentGridRow, boolean visible) {
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

        if (BooleanUtils.isTrue(parameter.getValidationOn())) {
            field.addValidator(new ReportParamFieldValidator(parameter));
        }

        Label label = parameterFieldCreator.createLabel(parameter, field);
        label.setStyleName("c-report-parameter-caption");

        if (currentGridRow == 0) {
            field.requestFocus();
        }

        label.setVisible(visible);
        field.setVisible(visible);

        parameterComponents.put(parameter.getAlias(), field);
        parametersGrid.add(label, 0, currentGridRow);
        parametersGrid.add(field, 1, currentGridRow);
    }

    public void initTemplateAndOutputSelect() {
        if (report != null) {
            if (report.getTemplates() != null && report.getTemplates().size() > 1) {
                templateField.setValue(report.getDefaultTemplate());
                setTemplateVisible(true);
            }
            templateField.addValueChangeListener(e -> updateOutputTypes());
            updateOutputTypes();
        }
    }

    protected void updateOutputTypes() {
        if (!reportGuiManager.containsAlterableTemplate(report)) {
            setOutputTypeVisible(false);
            return;
        }

        ReportTemplate template;
        if (report.getTemplates() != null && report.getTemplates().size() > 1) {
            template = templateField.getValue();
        }
        else {
            template = report.getDefaultTemplate();
        }

        if (template != null && reportGuiManager.supportAlterableForTemplate(template)) {
            List<ReportOutputType> outputTypes = ReportPrintHelper.getInputOutputTypesMapping().get(template.getExt());
            if (outputTypes != null && !outputTypes.isEmpty()) {
                outputTypeField.setOptionsList(outputTypes);
                if (outputTypeField.getValue() == null) {
                    outputTypeField.setValue(template.getReportOutputType());
                }
                setOutputTypeVisible(true);
            } else {
                outputTypeField.setValue(null);
                setOutputTypeVisible(false);
            }
        } else {
            outputTypeField.setValue(null);
            setOutputTypeVisible(false);
        }
    }

    protected void setOutputTypeVisible(boolean visible) {
        outputTypeLbl.setVisible(visible);
        outputTypeField.setVisible(visible);
    }

    protected void setTemplateVisible(boolean visible) {
        templateLbl.setVisible(visible);
        templateField.setVisible(visible);
    }

    public Report getReport() {
        return report;
    }

    public ReportTemplate getReportTemplate() {
        return templateField.getValue();
    }

    public ReportOutputType getOutputType() {
        return outputTypeField.getValue();
    }
}