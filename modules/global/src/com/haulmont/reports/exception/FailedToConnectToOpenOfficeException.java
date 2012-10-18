/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.exception;

/**
 * @author fontanenko
 * @version $Id$
 */
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