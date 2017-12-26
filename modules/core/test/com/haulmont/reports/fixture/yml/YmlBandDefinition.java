/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
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
