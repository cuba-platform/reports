/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.libintegration;

import com.google.common.base.Strings;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.app.execution.ResourceCanceledException;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.global.filter.ParametersHelper;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.ParameterType;
import com.haulmont.reports.entity.PredefinedTransformation;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.yarg.exception.ReportingException;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.ReportParameter;
import com.haulmont.yarg.structure.ReportTemplate;
import com.haulmont.yarg.util.groovy.Scripting;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CubaReporting extends Reporting {
    public static final String REPORT_FILE_NAME_KEY = "__REPORT_FILE_NAME";

    protected Scripting scripting;

    protected ReportService reportService;

    public void setScripting(Scripting scripting) {
        this.scripting = scripting;
    }

    public void setReportService(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    protected String resolveOutputFileName(Report report, ReportTemplate reportTemplate, BandData rootBand) {
        String generatedReportFileName = (String) rootBand.getData().get(REPORT_FILE_NAME_KEY);
        if (StringUtils.isNotBlank(generatedReportFileName)) {
            return generatedReportFileName;
        } else {
            return super.resolveOutputFileName(report, reportTemplate, rootBand);
        }
    }

    @Override
    protected Map<String, Object> handleParameters(com.haulmont.yarg.structure.Report report, Map<String, Object> params) {
        Map<String, Object> handledParams = new HashMap<String, Object>(super.handleParameters(report, params));
        for (ReportParameter reportParameter : report.getReportParameters()) {
            if (reportParameter instanceof ReportInputParameter) {
                ReportInputParameter reportInputParameter = (ReportInputParameter) reportParameter;

                String paramName = reportParameter.getAlias();
                Object paramValue = handledParams.get(paramName);

                if (BooleanUtils.isTrue(reportInputParameter.getDefaultDateIsCurrent())) {
                    handleDateTimeRelatedParameterAsNow(paramName, paramValue, reportInputParameter.getType(), handledParams);
                }

                if (paramValue == null) {
                    continue;
                }

                if (reportInputParameter.getPredefinedTransformation() != null) {
                    handledParams.put(paramName, handlePredefinedTransformation(paramValue, reportInputParameter.getPredefinedTransformation()));
                } else if (!Strings.isNullOrEmpty(reportInputParameter.getTransformationScript())) {
                    handledParams.put(paramName, handleScriptTransformation(paramValue, reportInputParameter.getTransformationScript(), handledParams));
                }
            }
        }
        return handledParams;
    }

    protected void handleDateTimeRelatedParameterAsNow(String paramName, Object paramValue, ParameterType parameterType,
                                                       Map<String, Object> handledParams) {
         if (Objects.isNull(paramValue)) {
             paramValue = reportService.adjustDate(parameterType);
             handledParams.put(paramName, paramValue);
         }
    }

    protected Object handlePredefinedTransformation(Object value, PredefinedTransformation transformation) {
        switch (transformation) {
            case CONTAINS:
                return wrapValueForLike(QueryUtils.escapeForLike((String)value), true, true);
            case STARTS_WITH:
                return wrapValueForLike(QueryUtils.escapeForLike((String)value), false, true);
            case ENDS_WITH:
                return wrapValueForLike(QueryUtils.escapeForLike((String)value), true, false);
        }
        return value;
    }

    protected Object handleScriptTransformation(Object paramValue, String script, Map<String, Object> params) {
        Map<String, Object> scriptParams = new HashMap<>();
        scriptParams.put("params", params);
        scriptParams.put("paramValue", paramValue);
        scriptParams.put("persistence", AppBeans.get(Persistence.class));
        scriptParams.put("metadata", AppBeans.get(Metadata.class));
        script = StringUtils.trim(script);
        if (script.endsWith(".groovy")) {
            script = AppBeans.get(Resources.class).getResourceAsString(script);
        }
        return scripting.evaluateGroovy(script, scriptParams);
    }

    protected String wrapValueForLike(Object value, boolean before, boolean after) {
        return ParametersHelper.CASE_INSENSITIVE_MARKER + (before ? "%" : "") + value + (after ? "%" : "");
    }

    @Override
    protected void logException(ReportingException e) {
        if (ExceptionUtils.getRootCause(e) instanceof ResourceCanceledException) {
            logger.info("Report is canceled by user request");
        } else {
            super.logException(e);
        }
    }
}
