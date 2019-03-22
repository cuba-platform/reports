/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.reports.wizard.template;

import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.TemplateFileType;
import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.reports.wizard.template.generators.*;
import org.springframework.context.annotation.Scope;

import org.springframework.stereotype.Component;

@Component(TemplateGeneratorApi.NAME)
@Scope("prototype")
public class TemplateGenerator implements TemplateGeneratorApi {

    protected final ReportData reportData;
    protected final TemplateFileType templateFileType;

    public TemplateGenerator(ReportData reportData, TemplateFileType templateFileType) {
        this.reportData = reportData;
        this.templateFileType = templateFileType;
    }

    @Override
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
            case CSV:
                generator = new CsvGenerator();
                break;
            case TABLE:
                generator = new TableGenerator();
                break;
            default:
                throw new TemplateGenerationException(templateFileType + " format is unsupported yet");
        }
        return generator;
    }
}