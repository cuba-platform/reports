/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.entity.charts;

import com.google.gson.Gson;

import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class ChartToJsonConverter {
    public String convertSerialChart(SerialChartDescription description, List<Map<String, Object>> data) {
        HashMap<String, Object> chart = new HashMap<>();
        chart.put("type", "serial");
        chart.put("categoryField", description.getCategoryField());
        chart.put("chartScrollbar", new HashMap<>());

        HashMap<Object, Object> valueAxis = new HashMap<>();
        valueAxis.put("gridColor", "#FFFFFF");
        valueAxis.put("gridAlpha", 0.2);
        valueAxis.put("dashLength", 0);
        valueAxis.put("title", description.getValueAxisCaption());
        valueAxis.put("unit", " " + description.getValueAxisUnits());
        if (description.getValueStackType() != null) {
            valueAxis.put("stackType", description.getValueStackType().getId());
        }
        chart.put("valueAxes", Collections.singletonList(valueAxis));

        HashMap<Object, Object> categoryAxis = new HashMap<>();
        categoryAxis.put("title", description.getCategoryAxisCaption());
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

            graph.put("balloonText", series.getName());

            graphs.add(graph);
        }

        chart.put("dataProvider", data);

        Gson gson = new Gson();
        return gson.toJson(chart);
    }

    public String convertPieChart(PieChartDescription description, List<Map<String, Object>> data) {
        HashMap<String, Object> chart = new HashMap<>();
        chart.put("type", "pie");
        chart.put("titleField", description.getTitleField());
        chart.put("valueField", description.getValueField());

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
