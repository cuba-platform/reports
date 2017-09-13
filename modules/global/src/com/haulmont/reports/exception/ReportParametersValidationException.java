/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.exception;

import com.haulmont.cuba.core.global.SupportedByClient;

@SupportedByClient
public class ReportParametersValidationException extends ReportingException {

    public ReportParametersValidationException(String message) {
        super(message);
    }
}
