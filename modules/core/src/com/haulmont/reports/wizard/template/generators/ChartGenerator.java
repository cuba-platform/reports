/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.wizard.template.generators;

import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.reports.entity.charts.AbstractChartDescription;
import com.haulmont.reports.entity.charts.ChartSeries;
import com.haulmont.reports.entity.charts.SerialChartDescription;
import com.haulmont.reports.entity.charts.SeriesType;
import com.haulmont.reports.entity.wizard.RegionProperty;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.ReportRegion;
import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.reports.wizard.template.Generator;
import freemarker.template.TemplateException;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class ChartGenerator implements Generator {
    @Override
    public byte[] generate(ReportData reportData) throws TemplateGenerationException, TemplateException, IOException {
        if (CollectionUtils.isNotEmpty(reportData.getReportRegions())) {
            ReportRegion reportRegion = reportData.getReportRegions().get(0);
            SerialChartDescription serialChartDescription = new SerialChartDescription();
            serialChartDescription.setBandName(reportRegion.getNameForBand());
            serialChartDescription.setValueAxisUnits("");
            serialChartDescription.setCategoryAxisCaption("");
            serialChartDescription.setValueAxisCaption("");

            for (RegionProperty regionProperty : reportRegion.getRegionProperties()) {
                MetaProperty wrappedMetaProperty = regionProperty.getEntityTreeNode().getWrappedMetaProperty();
                Class<?> javaType = wrappedMetaProperty.getJavaType();
                if (String.class.isAssignableFrom(javaType)) {
                    serialChartDescription.setCategoryField(wrappedMetaProperty.getName());
                    serialChartDescription.setCategoryAxisCaption(regionProperty.getLocalizedName());
                } else if (Number.class.isAssignableFrom(javaType)) {
                    ChartSeries chartSeries = new ChartSeries();
                    chartSeries.setName(regionProperty.getLocalizedName());
                    chartSeries.setValueField(wrappedMetaProperty.getName());
                    chartSeries.setType(SeriesType.COLUMN);

                    serialChartDescription.getSeries().add(chartSeries);
                }
            }

            return AbstractChartDescription.toJsonString(serialChartDescription).getBytes();

        }

        return new byte[0];
    }
}
