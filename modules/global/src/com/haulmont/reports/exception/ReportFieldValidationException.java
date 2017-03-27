/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.exception;

import com.haulmont.cuba.core.global.SupportedByClient;

@SupportedByClient
public class ReportFieldValidationException extends ReportingException {

    public ReportFieldValidationException(String message) {
        super(message);
    }
}
