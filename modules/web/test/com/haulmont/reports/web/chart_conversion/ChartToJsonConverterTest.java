/*
 * Copyright (c) 2008-2022 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.reports.web.chart_conversion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.haulmont.reports.entity.charts.ChartSeries;
import com.haulmont.reports.entity.charts.ChartToJsonConverter;
import com.haulmont.reports.entity.charts.SerialChartDescription;
import com.haulmont.reports.entity.charts.SeriesType;
import org.apache.groovy.util.Maps;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ChartToJsonConverterTest {

    @Test
    public void testLocalDateCategoryValue() throws IOException, URISyntaxException {
        ChartToJsonConverter converter = new ChartToJsonConverter();

        List<Map<String, Object>> data = new ArrayList<>();
        data.add(Maps.of("value", 1, "date", LocalDate.of(2022, 1, 11)));
        data.add(Maps.of("value", 2, "date", LocalDate.of(2022, 1, 12)));
        data.add(Maps.of("value", 3, "date", LocalDate.of(2022, 1, 13)));

        String json = converter.convertSerialChart(generateSerialChartDescription(), data);
        String expectedJson = readFile("localdate-serialchart.json");
        Assert.assertEquals(prettyJson(expectedJson), prettyJson(json));
    }

    @Test
    public void testLocalDateTimeCategoryValue() throws IOException, URISyntaxException {
        ChartToJsonConverter converter = new ChartToJsonConverter();

        List<Map<String, Object>> data = new ArrayList<>();
        data.add(Maps.of("value", 1, "date", LocalDateTime.of(2022, 1, 11, 1, 12, 33)));
        data.add(Maps.of("value", 2, "date", LocalDateTime.of(2022, 1, 12, 2, 13, 34)));
        data.add(Maps.of("value", 3, "date", LocalDateTime.of(2022, 1, 13, 3, 14, 35)));

        String json = converter.convertSerialChart(generateSerialChartDescription(), data);
        String expectedJson = readFile("localdatetime-serialchart.json");
        Assert.assertEquals(prettyJson(expectedJson), prettyJson(json));
    }

    protected SerialChartDescription generateSerialChartDescription() {
        SerialChartDescription serialChartDescription = new SerialChartDescription();
        serialChartDescription.setId(UUID.randomUUID());
        serialChartDescription.setBandName("testBand");
        serialChartDescription.setCategoryField("date");
        serialChartDescription.setSeries(Collections.singletonList(generateChartSeries()));
        return serialChartDescription;
    }

    protected ChartSeries generateChartSeries() {
        ChartSeries chartSeries = new ChartSeries();
        chartSeries.setId(UUID.randomUUID());
        chartSeries.setType(SeriesType.COLUMN);
        chartSeries.setValueField("value");
        chartSeries.setOrder(1);
        return chartSeries;
    }

    public static String readFile(String fileName) throws IOException, URISyntaxException {
        URL resource = ChartToJsonConverterTest.class
                .getResource("/com/haulmont/reports/web/chart_conversion/" + fileName);
        byte[] encoded = Files.readAllBytes(Paths.get(resource.toURI()));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static String prettyJson(String json) {
        JsonElement parsedJson = JsonParser.parseString(json);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(parsedJson);
    }
}
