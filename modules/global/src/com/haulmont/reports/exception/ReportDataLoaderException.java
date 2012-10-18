/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
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