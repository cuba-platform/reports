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

package com.haulmont.reports.gui.report.validators;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DevelopmentException;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.ValidationException;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.exception.ReportParametersValidationException;
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
            } catch (ReportParametersValidationException e) {
                throw new ValidationException(e.getMessage());
            }
        }
    }
}
