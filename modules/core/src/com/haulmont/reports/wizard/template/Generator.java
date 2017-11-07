/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.wizard.template;

import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.exception.TemplateGenerationException;
import freemarker.template.TemplateException;

import java.io.IOException;

public interface Generator {
    byte[] generate(ReportData reportData) throws TemplateGenerationException, TemplateException, IOException;
}