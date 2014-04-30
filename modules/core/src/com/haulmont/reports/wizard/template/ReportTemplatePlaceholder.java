/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.wizard.template;

import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.reports.entity.wizard.RegionProperty;
import com.haulmont.reports.entity.wizard.ReportRegion;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Temporal;
import java.util.Date;

/**
 * @author fedorchenko
 * @version $Id$
 */
public class ReportTemplatePlaceholder {
    protected static final String TABLE_MASK = "${%s}";
    protected static final String COMMON_MASK = "${%s.%s}";
    protected static final String HTML_COMMON_MASK = "${%s.fields('%s')!?string!}"; //like ${Task[0].fields('id')!?string!}
    protected static final String HTML_DATE_MASK = "<#if %1$s.fields('%2$s')?has_content>${%1$s.fields('%2$s')?string(\"%3$s\")}</#if>";// /like <#if Task[0].fields('updateTs')?has_content>${Task[0].fields('updateTs')?string("dd.MM.yyyy hh:mm")}</#if>
    protected Log log = LogFactory.getLog(getClass());

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

    public String getHtmlPlaceholderValue(ReportRegion reportRegion, RegionProperty regionProperty) {
        String bandName;
        String fieldName = regionProperty.getEntityTreeNode().getHierarchicalNameExceptRoot();
        if (reportRegion.isTabulatedRegion()) {
            bandName = "row";
            if (BooleanUtils.isNotFalse(reportRegion.getReportData().getIsTabulatedReport())) {
               fieldName = StringUtils.removeStart(regionProperty.getEntityTreeNode().getHierarchicalNameExceptRoot(), reportRegion.getRegionPropertiesRootNode().getName() + ".");
            }
        } else {
            bandName = reportRegion.getNameForBand() + "[0]";
        }
        MetaProperty wrappedMetaProperty = regionProperty.getEntityTreeNode().getWrappedMetaProperty();
        Temporal temporal = wrappedMetaProperty.getAnnotatedElement().getAnnotation(Temporal.class);
        if (temporal != null || wrappedMetaProperty.getJavaType().isAssignableFrom(Date.class)) {
            if (temporal != null && !wrappedMetaProperty.getJavaType().isAssignableFrom(Date.class)) {
                log.warn("Temporal annotated class property " + reportRegion.getNameForBand() + "." + wrappedMetaProperty.getName() + " is not assignable from java.util.Date class");
            }
            String dateMask;
            Messages messages = AppBeans.get(Messages.NAME);
            if (temporal != null) {
                switch (temporal.value()) {
                    case DATE:
                        dateMask = messages.getMessage(messages.getMainMessagePack(), "dateFormat");
                        break;
                    case TIME:
                        dateMask = messages.getMessage(messages.getMainMessagePack(), "timeFormat");
                        break;
                    default:
                        dateMask = messages.getMessage(messages.getMainMessagePack(), "dateTimeFormat");
                }
            } else {
                dateMask = messages.getMessage(messages.getMainMessagePack(), "dateTimeFormat");
            }
            return String.format(HTML_DATE_MASK, bandName, fieldName, dateMask);
        } else {
            return String.format(HTML_COMMON_MASK, bandName, fieldName);
        }
    }
}
