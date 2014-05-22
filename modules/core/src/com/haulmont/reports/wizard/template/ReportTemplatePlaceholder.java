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
    protected static final String HAS_CONTENT = "?has_content";
    protected static final String AND = " && ";
    protected static final String TABLE_MASK = "${%s}";
    protected static final String COMMON_MASK = "${%s.%s}";
    protected static final String HTML_VALUE_MASK = "%s.fields('%s')%s";
    protected static final String HTML_COMMON_MASK = "%4$s${(%1$s.fields('%2$s')%3$s)!?string!}%5$s"; //like ${Task[0].fields('id')!?string!}
    protected static final String HTML_DATE_MASK = "<#if %5$s %1$s.fields('%2$s')%4$s?has_content>${%1$s.fields('%2$s')%4$s?string(\"%3$s\")}</#if>";// /like <#if Task[0].fields('updateTs')?has_content>${Task[0].fields('updateTs')?string("dd.MM.yyyy hh:mm")}</#if>
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
            fieldName = StringUtils.removeStart(regionProperty.getEntityTreeNode().getHierarchicalNameExceptRoot(), reportRegion.getRegionPropertiesRootNode().getName() + ".");
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
            String[] partsFieldName = fieldName.split("\\.");
            if (partsFieldName.length > 1) {
                fieldName = partsFieldName[0];
            }
            return String.format(HTML_DATE_MASK, bandName, fieldName, dateMask, generatePathForEntityField(partsFieldName), generateConditions(partsFieldName, bandName, fieldName, false));
        } else {
            String[] partsFieldName = fieldName.split("\\.");
            if (partsFieldName.length > 1) {
                fieldName = partsFieldName[0];
            }
            String condition = generateConditions(partsFieldName, bandName, fieldName, true);
            return String.format(HTML_COMMON_MASK, bandName, fieldName, generatePathForEntityField(partsFieldName), condition, condition.length() > 0 ? "</#if>" : "");
        }
    }

    protected String generatePathForEntityField(String[] partsFieldName) {
        StringBuilder pathForEntityField = new StringBuilder();
        if (partsFieldName.length > 1) {
            for (int i = 1; i < partsFieldName.length; i++) {
                pathForEntityField.append(".").append(partsFieldName[i]);
            }
        }
        return pathForEntityField.toString();
    }

    protected String generateConditions(String[] partsFieldName, String bandName, String fieldName, boolean addIf) {
        StringBuilder pathForEntityField = new StringBuilder();
        StringBuilder condition = new StringBuilder();
        if (partsFieldName.length > 1) {
            condition.append(String.format(HTML_VALUE_MASK, bandName, fieldName, pathForEntityField.toString())).append(HAS_CONTENT).append(AND);
            for (int i = 1; i < partsFieldName.length; i++) {
                pathForEntityField.append(".").append(partsFieldName[i]);
                if (i < partsFieldName.length - 1)
                    condition.append(" ").append(String.format(HTML_VALUE_MASK, bandName, fieldName, pathForEntityField.toString())).append(HAS_CONTENT).append(AND);
            }
            if (condition.length() > 4 && addIf) {
                condition.delete(condition.length() - 4, condition.length());
                condition.insert(0, "<#if ");
                condition.append(">");
            }
        }
        return condition.toString();
    }
}
