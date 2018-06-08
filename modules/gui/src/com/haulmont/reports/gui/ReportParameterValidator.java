/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.global.*;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.exception.ReportParametersValidationException;
import com.haulmont.reports.exception.ReportingException;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.runtime.MethodClosure;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Component(ReportParameterValidator.NAME)
public class ReportParameterValidator {
    public static final String NAME = "report_ReportParameterValidator";

    @Inject
    protected Metadata metadata;
    @Inject
    protected DataManager dataManager;
    @Inject
    protected Security security;
    @Inject
    protected Scripting scripting;
    @Inject
    protected UserSessionSource userSessionSource;

    /**
     * Checking validation for an input parameter field before running the report.
     *
     * @param parameter data info which describes report's parameter
     * @param value     parameter's value
     */
    public void validateParameterValue(ReportInputParameter parameter, Object value) {
        String groovyScript = parameter.getValidationScript();
        Map<String, Object> scriptContext = createScriptContext(ParamsMap.of("value", value));
        runValidationScript(groovyScript, scriptContext);
    }

    /**
     * Performs cross field parameters validation before running the report.
     *
     * @param report report instance
     * @param reportParameters  map of parameters values taken from components
     */
    public void crossValidateParameters(Report report, Map<String, Object> reportParameters) {
        String groovyScript = report.getValidationScript();
        Map<String, Object> scriptContext = createScriptContext(ParamsMap.of("params", reportParameters));
        runValidationScript(groovyScript, scriptContext);
    }

    protected void runValidationScript(String groovyScript, Map<String, Object> scriptContext) {
        if (StringUtils.isNotBlank(groovyScript)) {
            try {
                scripting.evaluateGroovy(groovyScript, scriptContext);
            } catch (ReportParametersValidationException e) {
                throw e;
            } catch (Exception e) {
                String message = "Error applying field validation Groovy script. \n" + e.toString();
                throw new ReportingException(message);
            }
        }
    }

    protected Map<String, Object> createScriptContext(Map<String, Object> contextParameters) {
        Map<String, Object> context = new HashMap<>();
        context.putAll(contextParameters);
        addCommonContext(context);
        return context;
    }

    protected void addCommonContext(Map<String, Object> context) {
        context.put("userSession", userSessionSource.getUserSession());
        context.put("dataManager", dataManager);
        context.put("security", security);
        context.put("metadata", metadata);
        context.put("invalid", new MethodClosure(this, "invalidThrowMethod"));
    }

    // Used for invalid("") syntax
    @SuppressWarnings("unused")
    protected void invalidThrowMethod(String message) {
        throw new ReportParametersValidationException(message);
    }
}
