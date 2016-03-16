/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.exception;

/**
 */
public class FailedToLoadTemplateClassException extends ReportingException {

    protected String className;

    public FailedToLoadTemplateClassException(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
