/*
 * Copyright (c) 2008-2019 Haulmont.
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

package com.haulmont.reports.wizard.template.generators;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.haulmont.reports.entity.wizard.RegionProperty;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.ReportRegion;
import com.haulmont.reports.wizard.template.Generator;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class TableGenerator implements Generator {

    @Override
    public byte[] generate(ReportData reportData) {
        String templateContent = generateTemplate(reportData);
        return templateContent.getBytes(StandardCharsets.UTF_8);
    }

    protected String generateTemplate(ReportData reportData) {
        Gson gson = new Gson();
        JsonObject jsonTabs = new JsonObject();

        for (ReportRegion reportRegion : reportData.getReportRegions()) {
            List<String> tabs = reportRegion.getRegionProperties()
                    .stream()
                    .map(RegionProperty::getName)
                    .collect(Collectors.toList());
            jsonTabs.addProperty(reportRegion.getName(), gson.toJson(tabs));
        }

        JsonObject jsonResult = new JsonObject();
        jsonResult.add("bands", jsonTabs);
        return jsonResult.toString();
    }
}
