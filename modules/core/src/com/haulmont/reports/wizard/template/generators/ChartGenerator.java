/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.wizard.template.generators;

import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.reports.entity.charts.*;
import com.haulmont.reports.entity.wizard.RegionProperty;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.ReportRegion;
import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.reports.wizard.template.Generator;
import freemarker.template.TemplateException;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.util.List;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class ChartGenerator implements Generator {
    @Override
    public byte[] generate(ReportData reportData) throws TemplateGenerationException, TemplateException, IOException {
        if (reportData.getChartType() == ChartType.SERIAL) {
            return generateSerialChart(reportData);
        } else if (reportData.getChartType() == ChartType.PIE) {
            return generatePieChart(reportData);
        }
        return new byte[0];
    }

    protected byte[] generateSerialChart(ReportData reportData) {
        if (CollectionUtils.isNotEmpty(reportData.getReportRegions())) {
            ReportRegion reportRegion = reportData.getReportRegions().get(0);
            SerialChartDescription serialChartDescription = new SerialChartDescription();
            serialChartDescription.setBandName(reportRegion.getNameForBand());
            serialChartDescription.setValueAxisUnits("");
            serialChartDescription.setCategoryAxisCaption("");
            serialChartDescription.setValueAxisCaption("");

            List<RegionProperty> regionProperties = reportRegion.getRegionProperties();
            RegionProperty firstProperty = regionProperties.get(0);
            serialChartDescription.setCategoryField(firstProperty.getEntityTreeNode().getWrappedMetaProperty().getName());
            serialChartDescription.setCategoryAxisCaption(firstProperty.getLocalizedName());
            if (regionProperties.size() > 1) {
                for (int i = 1; i < regionProperties.size(); i++) {
                    RegionProperty regionProperty = regionProperties.get(i);
                    MetaProperty wrappedMetaProperty = regionProperty.getEntityTreeNode().getWrappedMetaProperty();
                    Class<?> javaType = wrappedMetaProperty.getJavaType();
                    if (Number.class.isAssignableFrom(javaType)) {
                        ChartSeries chartSeries = new ChartSeries();
                        chartSeries.setName(regionProperty.getLocalizedName());
                        chartSeries.setValueField(wrappedMetaProperty.getName());
                        chartSeries.setType(SeriesType.COLUMN);
                        serialChartDescription.getSeries().add(chartSeries);
                    }
                }
            }

            return AbstractChartDescription.toJsonString(serialChartDescription).getBytes();
        }

        return new byte[0];
    }

    protected byte[] generatePieChart(ReportData reportData) {
        ReportRegion reportRegion = reportData.getReportRegions().get(0);
        PieChartDescription pieChartDescription = new PieChartDescription();
        pieChartDescription.setBandName(reportRegion.getNameForBand());
//        pieChartDescription.setShowLegend(true);
        pieChartDescription.setUnits("");

        List<RegionProperty> regionProperties = reportRegion.getRegionProperties();
        RegionProperty firstProperty = regionProperties.get(0);
        pieChartDescription.setTitleField(firstProperty.getEntityTreeNode().getWrappedMetaProperty().getName());
        if (regionProperties.size() > 1) {
            for (int i = 1; i < regionProperties.size(); i++) {
                RegionProperty regionProperty = regionProperties.get(i);
                MetaProperty wrappedMetaProperty = regionProperty.getEntityTreeNode().getWrappedMetaProperty();
                Class<?> javaType = wrappedMetaProperty.getJavaType();
                if (Number.class.isAssignableFrom(javaType)) {
                    pieChartDescription.setValueField(wrappedMetaProperty.getName());
                    break;
                }
            }
        }

        return AbstractChartDescription.toJsonString(pieChartDescription).getBytes();
    }
}
