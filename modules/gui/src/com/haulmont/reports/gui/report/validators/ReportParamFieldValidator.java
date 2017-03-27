/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.report.validators;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DevelopmentException;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.ValidationException;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.exception.ReportFieldValidationException;
import com.haulmont.reports.gui.ReportParameterValidator;

public class ReportParamFieldValidator implements Field.Validator {
    protected ReportParameterValidator reportParameterValidator = AppBeans.get(ReportParameterValidator.NAME);

    private ReportInputParameter inputParameter;

    public ReportParamFieldValidator(ReportInputParameter inputParameter) {
        if (inputParameter == null) {
            throw new DevelopmentException("ReportInputParameter is not defined");
        }

        this.inputParameter = inputParameter;
    }

    @Override
    public void validate(Object value) throws ValidationException {
        if (value != null) {
            try {
                reportParameterValidator.validateParameterValue(inputParameter, value);
            } catch (ReportFieldValidationException e) {
                throw new ValidationException(e.getMessage());
            }
        }
    }
}
