/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.AbstractFormatter;
import com.haulmont.yarg.formatters.impl.DocxFormatter;
import com.haulmont.yarg.formatters.impl.HtmlFormatter;
import com.haulmont.yarg.formatters.impl.XlsxFormatter;

public class CubaFormatterFactory extends DefaultFormatterFactory {
    protected boolean useOfficeForDocumentConversion = true;

    public CubaFormatterFactory() {
        super();
        FormatterCreator ftlCreator = factoryInput -> {
            HtmlFormatter htmlFormatter = new CubaHtmlFormatter(factoryInput);
            htmlFormatter.setDefaultFormatProvider(defaultFormatProvider);
            return htmlFormatter;
        };
        formattersMap.put("ftl", ftlCreator);
        formattersMap.put("html", ftlCreator);

        FormatterCreator docxCreator = factoryInput -> {
            DocxFormatter docxFormatter = new DocxFormatter(factoryInput);
            docxFormatter.setDefaultFormatProvider(defaultFormatProvider);
            if (useOfficeForDocumentConversion) {
                docxFormatter.setDocumentConverter(documentConverter);
            }
            return docxFormatter;
        };

        formattersMap.put("docx", docxCreator);
        formattersMap.put("chart", ChartFormatter::new);

        FormatterCreator xlsxCreator = factoryInput -> {
            XlsxFormatter xlsxFormatter = new CubaXlsxFormatter(factoryInput);
            xlsxFormatter.setDefaultFormatProvider(defaultFormatProvider);
            xlsxFormatter.setDocumentConverter(documentConverter);
            return xlsxFormatter;
        };
        formattersMap.put("xlsx", xlsxCreator);

        formattersMap.put("table", CubaTableFormatter::new);
    }

    @Override
    public ReportFormatter createFormatter(FormatterFactoryInput factoryInput) {
        ReportFormatter formatter = super.createFormatter(factoryInput);
        if (formatter instanceof AbstractFormatter) {
            AbstractFormatter abstractFormatter = (AbstractFormatter) formatter;
            abstractFormatter.getContentInliners().add(new FileStorageContentInliner());
        }

        return formatter;
    }

    public boolean isUseOfficeForDocumentConversion() {
        return useOfficeForDocumentConversion;
    }

    public void setUseOfficeForDocumentConversion(boolean useOfficeForDocumentConversion) {
        this.useOfficeForDocumentConversion = useOfficeForDocumentConversion;
    }
}