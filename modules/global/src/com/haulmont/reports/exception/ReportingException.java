/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
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
