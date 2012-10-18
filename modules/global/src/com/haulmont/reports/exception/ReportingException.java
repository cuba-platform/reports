/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports.exception;

/**
 * @author artamonov
 * @version $Id$
 */
public class ReportingException extends RuntimeException {

    public ReportingException() {
    }

    public ReportingException(String message) {
        super(message);
    }
       
    public ReportingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportingException(Throwable cause) {
        super(cause);
    }
}
