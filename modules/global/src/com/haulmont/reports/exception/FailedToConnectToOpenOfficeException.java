/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports.exception;

import com.haulmont.cuba.core.global.SupportedByClient;

@SupportedByClient
public class FailedToConnectToOpenOfficeException extends ReportingException {
    private static final long serialVersionUID = -131512314730709035L;

    public FailedToConnectToOpenOfficeException() {
    }

    public FailedToConnectToOpenOfficeException(String message) {
        super(message);
    }

    public FailedToConnectToOpenOfficeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedToConnectToOpenOfficeException(Throwable cause) {
        super(cause);
    }
}