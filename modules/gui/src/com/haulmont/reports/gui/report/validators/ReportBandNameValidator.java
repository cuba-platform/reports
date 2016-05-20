/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.report.validators;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.ValidationException;
import org.apache.commons.lang.StringUtils;

public class ReportBandNameValidator implements Field.Validator {
    protected Messages messages = AppBeans.get(Messages.class);

    @Override
    public void validate(Object value) throws ValidationException {
        String stringValue = (String) value;

        if (StringUtils.isNotEmpty(stringValue) && !stringValue.matches("[\\w]*")) {
            String incorrectBandName = messages.getMessage(ReportBandNameValidator.class, "incorrectBandName");
            throw new ValidationException(incorrectBandName);
        }
    }
}
