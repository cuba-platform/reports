/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.wizard.template;

import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.TemplateFileType;
import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.reports.wizard.template.generators.*;
import org.springframework.context.annotation.Scope;

import org.springframework.stereotype.Component;

/**
 */
@Component(TemplateGeneratorApi.NAME)
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
            case CHART:
                generator = new ChartGenerator();
                break;
            default:
                throw new TemplateGenerationException(templateFileType + " format is unsupported yet");
        }
        return generator;
    }

}
