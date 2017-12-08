/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.exception;

import com.haulmont.cuba.core.global.Logging;
import com.haulmont.cuba.core.global.SupportedByClient;

@SupportedByClient
@Logging(value = Logging.Type.NONE)
public class ReportCanceledException extends ReportingException {
    private static final long serialVersionUID = 1543263739485152663L;

    public ReportCanceledException() {
    }

    public ReportCanceledException(String message) {
        super(message);
    }

    public ReportCanceledException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportCanceledException(Throwable cause) {
        super(cause);
    }
}
