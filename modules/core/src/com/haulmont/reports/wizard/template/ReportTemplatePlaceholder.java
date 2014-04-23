/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.wizard.template;

import com.haulmont.reports.entity.wizard.ReportRegion;
import com.haulmont.reports.wizard.template.TemplateGeneratorApi;
import org.apache.commons.lang.StringUtils;

import javax.annotation.ManagedBean;

/**
 * @author fedorchenko
 * @version $Id$
 */
public class ReportTemplatePlaceholder {
    protected static final String TABLE_MASK = "${%s}";
    protected static final String COMMON_MASK = "${%s.%s}";
    protected static final String HTML_MASK = "${%sfields('%s')%s}";

    /**
     * used in doc table fields and sheet reports
     *
     * @return
     */
    public String getPlaceholderValue(String value, ReportRegion reportRegion) {
        return String.format(TABLE_MASK, StringUtils.removeStart(value, reportRegion.getRegionPropertiesRootNode().getName() + "."));
    }

    /**
     * used in common fields
     *
     * @param reportRegion
     * @return
     */
    public String getPlaceholderValueWithBandName(String value, ReportRegion reportRegion) {
        return String.format(COMMON_MASK, reportRegion.getNameForBand(), StringUtils.removeStart(value,
                reportRegion.getRegionPropertiesRootNode().getName() + "."));
    }

    /**
     * Used in common fields
     *
     * @param reportRegion
     * @return
     */
    public String getHtmlPlaceholderValueWithBandName(String value, ReportRegion reportRegion) {
        return String.format(HTML_MASK,
                reportRegion.getNameForBand() + "[0].",
                StringUtils.removeStart(value, reportRegion.getRegionPropertiesRootNode().getName() + "."),
                "!?string!");
    }

    /**
     * Used in freemarker table fields
     *
     * @return
     */
    public String getHtmlTablePlaceholderValue(String value, ReportRegion reportRegion) {
        return String.format(HTML_MASK,
                "row.",
                StringUtils.removeStart(value, reportRegion.getRegionPropertiesRootNode().getName() + "."),
                "!?string!");
    }
}
