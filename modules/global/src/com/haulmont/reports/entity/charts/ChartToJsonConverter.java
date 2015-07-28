/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.entity.charts;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class ChartToJsonConverter {
    protected String resultFileName;

    public ChartToJsonConverter() {
    }

    public ChartToJsonConverter(String resultFileName) {
        this.resultFileName = resultFileName;
    }

    public String convertSerialChart(SerialChartDescription description, List<Map<String, Object>> data) {
        HashMap<String, Object> chart = new HashMap<>();
        chart.put("type", "serial");
        chart.put("categoryField", description.getCategoryField());
        chart.put("chartScrollbar", Collections.emptyMap());
        chart.put("pathToImages", "VAADIN/resources/amcharts/images/");
        exportConfig(chart);

        if (Boolean.TRUE.equals(description.getShowLegend())) {
            Map<String, Object> legend = new HashMap<>();
            legend.put("useGraphSettings", true);
            legend.put("markerSize", 10);
            chart.put("legend", legend);
        }

        HashMap<Object, Object> valueAxis = new HashMap<>();
        valueAxis.put("gridColor", "#000");
        valueAxis.put("gridAlpha", 0.1);
        valueAxis.put("dashLength", 0);
        valueAxis.put("title", description.getValueAxisCaption());
        valueAxis.put("unit", " " + description.getValueAxisUnits());
        if (description.getValueStackType() != null) {
            valueAxis.put("stackType", description.getValueStackType().getId());
        }
        chart.put("valueAxes", Collections.singletonList(valueAxis));

        HashMap<Object, Object> categoryAxis = new HashMap<>();
        categoryAxis.put("title", description.getCategoryAxisCaption());
        categoryAxis.put("gridColor", "#000");
        categoryAxis.put("gridAlpha", 0.1);
        categoryAxis.put("labelRotation", description.getCategoryAxisLabelRotation());
        chart.put("categoryAxis", categoryAxis);

        ArrayList<Object> graphs = new ArrayList<>();
        chart.put("graphs", graphs);
        for (ChartSeries series : description.getSeries()) {
            HashMap<Object, Object> graph = new HashMap<>();
            graph.put("type", series.getType() != null ? series.getType().getId() : SeriesType.COLUMN);
            graph.put("valueField", series.getValueField());
            if (series.getType() == SeriesType.COLUMN || series.getType() == SeriesType.STEP) {
                graph.put("fillColorsField", series.getColorField());
                graph.put("fillAlphas", 0.5);
                graph.put("columnWidth", 0.4);
            } else {
                graph.put("lineColorField", series.getColorField());
                graph.put("lineAlpha", 1);
                graph.put("lineThickness", 2);
            }

            graph.put("balloonText", series.getName()+" : [[value]]");
            graph.put("title", series.getName());

            graphs.add(graph);
        }

        chart.put("dataProvider", data);

        Gson gson = new Gson();
        return gson.toJson(chart);
    }

    protected void exportConfig(HashMap<String, Object> chart) {
        HashMap<Object, Object> exportConfig = new HashMap<>();
        exportConfig.put("menuTop", "100px");
        ArrayList<Object> menuItems = new ArrayList<>();
        HashMap<Object, Object> menuItem = new HashMap<>();
        menuItem.put("format", "png");
        menuItem.put("icon", "VAADIN/resources/amcharts/images/export.png");
        menuItems.add(menuItem);
        exportConfig.put("menuItems", menuItems);
        if (StringUtils.isNotBlank(resultFileName)) {
            exportConfig.put("menuItemOutput", Collections.singletonMap("fileName", resultFileName));
        }
        chart.put("exportConfig", exportConfig);
    }

    public String convertPieChart(PieChartDescription description, List<Map<String, Object>> data) {
        HashMap<String, Object> chart = new HashMap<>();
        chart.put("type", "pie");
        chart.put("titleField", description.getTitleField());
        chart.put("valueField", description.getValueField());
        chart.put("pathToImages", "VAADIN/resources/amcharts/images/");
        exportConfig(chart);

        if (Boolean.TRUE.equals(description.getShowLegend())) {
            Map<String, Object> legend = new HashMap<>();
            legend.put("markerType", "circle");
            legend.put("position", "right");
            legend.put("autoMargins", false);
            legend.put("valueText", "[[value]] " + description.getUnits());
            legend.put("valueWidth", 100);

            chart.put("legend", legend);
        }

        chart.put("dataProvider", data);

        Gson gson = new Gson();
        return gson.toJson(chart);
    }
}
