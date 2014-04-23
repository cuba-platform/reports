/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.wizard.template;

import com.haulmont.reports.exception.TemplateGenerationException;

/**
 * @author fedorchenko
 * @version $Id$
 */
public interface TemplateGeneratorApi {
    String NAME = "report_TemplateGenerator";
    byte[] generateTemplate() throws TemplateGenerationException;
}
