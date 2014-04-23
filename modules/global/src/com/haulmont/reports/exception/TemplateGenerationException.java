/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.exception;

import com.haulmont.cuba.core.global.SupportedByClient;

/**
 * @author fedorchenko
 * @version $Id$
 */
@SupportedByClient
public class TemplateGenerationException extends Exception {
    public TemplateGenerationException(String message) {
        super(message);
    }

    public TemplateGenerationException(Exception e) {
        super(e);
    }
}
