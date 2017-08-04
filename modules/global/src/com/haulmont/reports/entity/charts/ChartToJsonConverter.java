/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.entity.charts;

import com.google.gson.*;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.reports.entity.charts.serialization.DateSerializer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.*;

public class ChartToJsonConverter {

    protected final static Gson gson;

    static {
        // GSON is thread safe so we can use shared GSON instance
        gson = createGsonBuilder().create();
    }

    public static Gson getSharedGson() {
        return gson;
    }

    /**
     * Returns default GSON builder for configuration serializer.
     */
    public static GsonBuilder createGsonBuilder() {
        GsonBuilder builder = new GsonBuilder();
        setDefaultProperties(builder);
        return builder;
    }

    private static void setDefaultProperties(GsonBuilder builder) {
        builder.registerTypeAdapter(Date.class, new DateSerializer());
    }

    protected String resultFileName;

    public ChartToJsonConverter() {
    }

    public ChartToJsonConverter(String resultFileName) {
        this.resultFileName = resultFileName;
    }


    public String convertSerialChart(SerialChartDescription description, List<Map<String, Object>> data) {
        HashMap<String, Object> chart = new HashMap<>();
        List<String> fields = new ArrayList<>();

        chart.put("type", "serial");
        chart.put("categoryField", description.getCategoryField());
        addField(fields, description.getCategoryField());
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
        valueAxis.put("unit", join(" ", description.getValueAxisUnits()));
        if (description.getValueStackType() != null) {
            valueAxis.put("stackType", description.getValueStackType().getId());
        }
        chart.put("valueAxes", Collections.singletonList(valueAxis));

        HashMap<Object, Object> categoryAxis = new HashMap<>();
        categoryAxis.put("title", description.getCategoryAxisCaption());
        categoryAxis.put("gridColor", "#000");
        categoryAxis.put("gridAlpha", 0.1);
        categoryAxis.put("labelRotation", description.getCategoryAxisLabelRotation());
        if (isByDate(description.getCategoryField(), data)) {
            categoryAxis.put("parseDates", true);
        }
        chart.put("categoryAxis", categoryAxis);

        ArrayList<Object> graphs = new ArrayList<>();
        chart.put("graphs", graphs);
        for (ChartSeries series : description.getSeries()) {
            HashMap<Object, Object> graph = new HashMap<>();
            graph.put("type", series.getType() != null ? series.getType().getId() : SeriesType.COLUMN);
            graph.put("valueField", series.getValueField());
            addField(fields, series.getValueField());
            if (series.getType() == SeriesType.COLUMN || series.getType() == SeriesType.STEP) {
                graph.put("fillColorsField", series.getColorField());
                addField(fields, series.getColorField());
                graph.put("fillAlphas", 0.5);
                graph.put("columnWidth", 0.4);
            } else {
                graph.put("lineColorField", series.getColorField());
                addField(fields, series.getColorField());
                graph.put("lineAlpha", 1);
                graph.put("lineThickness", 2);
            }

            graph.put("balloonText", join(series.getName(), series.getName() != null ? " : " : "", "[[value]]"));
            graph.put("title", series.getName());

            graphs.add(graph);
        }

        JsonElement jsonTree = gson.toJsonTree(chart);
        jsonTree.getAsJsonObject().add("dataProvider", serializeData(data, fields));

        return gson.toJson(jsonTree);
    }

    private boolean isByDate(String categoryField, List<Map<String, Object>> data) {
        if (CollectionUtils.isNotEmpty(data)) {
            Map<String, Object> map = data.get(0);
            Object categoryFieldValue = map.get(categoryField);
            if (categoryFieldValue instanceof Date) {
                return true;
            }
        }

        return false;
    }

    protected void exportConfig(HashMap<String, Object> chart) {
        HashMap<Object, Object> export = new HashMap<>();
        export.put("enabled", true);
        if (StringUtils.isNotBlank(resultFileName)) {
            export.put("fileName", resultFileName);
        }
        chart.put("export", export);
    }

    public String convertPieChart(PieChartDescription description, List<Map<String, Object>> data) {
        HashMap<String, Object> chart = new HashMap<>();
        List<String> fields = new ArrayList<>();

        chart.put("type", "pie");
        chart.put("titleField", description.getTitleField());
        addField(fields, description.getTitleField());
        chart.put("valueField", description.getValueField());
        addField(fields, description.getValueField());
        chart.put("pathToImages", "VAADIN/resources/amcharts/images/");
        exportConfig(chart);

        if (Boolean.TRUE.equals(description.getShowLegend())) {
            Map<String, Object> legend = new HashMap<>();
            legend.put("markerType", "circle");
            legend.put("position", "right");
            legend.put("autoMargins", false);
            legend.put("valueText", join("[[value]] ", description.getUnits()));
            legend.put("valueWidth", 100);

            chart.put("legend", legend);
        }

        JsonElement jsonTree = gson.toJsonTree(chart);
        jsonTree.getAsJsonObject().add("dataProvider", serializeData(data, fields));

        return gson.toJson(jsonTree);
    }

    private JsonElement serializeData(List<Map<String, Object>> data, List<String> fields) {
        JsonArray dataArray = new JsonArray();
        for (Map<String, Object> map : data) {
            JsonObject itemElement = new JsonObject();
            for (String field : fields) {
                Object value = map.get(field);
                addProperty(itemElement, field, value);
            }
            dataArray.add(itemElement);
        }
        return dataArray;
    }

    protected void addProperty(JsonObject jsonObject, String property, Object value) {
        if (value instanceof Entity) {
            value = ((Entity) value).getInstanceName();
        }

        jsonObject.add(property, gson.toJsonTree(value));
    }

    protected void addField(List<String> fields, @Nullable String field) {
        if (field != null) {
            fields.add(field);
        }
    }

    protected String join(Object... objects) {
        return StringUtils.join(objects, "");
    }
}
