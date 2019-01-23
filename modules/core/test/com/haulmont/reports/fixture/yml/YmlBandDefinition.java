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

package com.haulmont.reports.fixture.yml;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.haulmont.reports.entity.BandDefinition;
import com.haulmont.reports.entity.Orientation;
import com.haulmont.yarg.structure.ReportQuery;

import java.util.List;

/**
 * Jackson YAML format {@link BandDefinition} description
 */
public class YmlBandDefinition extends BandDefinition {

    @Override
    @JsonProperty("orientation")
    @JsonDeserialize(using = OrientationDeserializer.class)
    public Orientation getOrientation() {
        return super.getOrientation();
    }

    @Override
    @JsonProperty("parent")
    @JsonBackReference("parent")
    public BandDefinition getParentBandDefinition() {
        return super.getParentBandDefinition();
    }

    @Override
    @JsonProperty("children")
    @JsonManagedReference("parent")
    @JsonDeserialize(contentAs = YmlBandDefinition.class)
    public List<BandDefinition> getChildrenBandDefinitions() {
        return super.getChildrenBandDefinitions();
    }

    @Override
    @JsonProperty("queries")
    @JsonDeserialize(contentAs = YmlDataSet.class)
    public List<ReportQuery> getReportQueries() {
        return super.getReportQueries();
    }
}
