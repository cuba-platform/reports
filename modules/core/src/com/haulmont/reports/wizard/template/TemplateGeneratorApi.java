/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.wizard.template;

import com.haulmont.reports.exception.TemplateGenerationException;

/**
 */
public interface TemplateGeneratorApi {
    String NAME = "report_TemplateGenerator";
    byte[] generateTemplate() throws TemplateGenerationException;
}
