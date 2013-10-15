/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.exception;

/**
 * @author devyatkin
 * @version $Id$
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
