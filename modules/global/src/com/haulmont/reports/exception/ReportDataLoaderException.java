/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.exception;

/**
 * @author artamonov
 * @version $Id$
 */
public class ReportDataLoaderException extends ReportingException {

    public ReportDataLoaderException() {
    }

    public ReportDataLoaderException(String message) {
        super(message);
    }

    public ReportDataLoaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportDataLoaderException(Throwable cause) {
        super(cause);
    }
}