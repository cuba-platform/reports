/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.reports.libintegration;

import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.AbstractFormatter;
import com.haulmont.yarg.formatters.impl.HtmlFormatter;

public class CubaFormatterFactory extends DefaultFormatterFactory {
    public CubaFormatterFactory() {
        super();
        FormatterCreator ftlCreator = new FormatterCreator() {
            @Override
            public ReportFormatter create(FormatterFactoryInput factoryInput) {
                HtmlFormatter htmlFormatter = new CubaHtmlFormatter(factoryInput);
                htmlFormatter.setDefaultFormatProvider(defaultFormatProvider);
                return htmlFormatter;
            }
        };
        formattersMap.put("ftl", ftlCreator);
        formattersMap.put("html", ftlCreator);
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
}
