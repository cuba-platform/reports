/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.exception;

import com.haulmont.cuba.core.global.SupportedByClient;

/**
 * @author artamonov
 * @version $Id$
 */
@SupportedByClient
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
