/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.wizard.template;

import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.TemplateFileType;
import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.reports.wizard.template.generators.DocxGenerator;
import com.haulmont.reports.wizard.template.generators.HtmlGenerator;
import com.haulmont.reports.wizard.template.generators.XlsxGenerator;
import org.springframework.context.annotation.Scope;

import javax.annotation.ManagedBean;

/**
 * @author fedorchenko
 * @version $Id$
 */
@ManagedBean(TemplateGeneratorApi.NAME)
@Scope("prototype")
public class TemplateGenerator implements TemplateGeneratorApi {

    protected final ReportData reportData;
    protected final TemplateFileType templateFileType;

    public TemplateGenerator(ReportData reportData, TemplateFileType templateFileType) {
        this.reportData = reportData;
        this.templateFileType = templateFileType;
    }

    public byte[] generateTemplate() throws TemplateGenerationException {
        byte[] template;
        try {
            template = createGenerator(templateFileType).generate(reportData);
        } catch (Exception e) {
            throw new TemplateGenerationException(e);
        }
        return template;

    }

    protected Generator createGenerator(TemplateFileType templateFileType) throws TemplateGenerationException {
        Generator generator;
        switch (templateFileType) {
            case DOCX:
                generator = new DocxGenerator();
                break;
            case XLSX:
                generator = new XlsxGenerator();
                break;
            case HTML:
                generator = new HtmlGenerator();
                break;
            default:
                throw new TemplateGenerationException(templateFileType + " format is unsupported yet");
        }
        return generator;
    }

}
