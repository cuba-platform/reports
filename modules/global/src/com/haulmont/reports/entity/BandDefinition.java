/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.entity;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * @author degtyarjov
 * @version $Id$
 */
@MetaClass(name = "report$BandDefinition")
@NamePattern("%s|name")
@SystemLevel
public class BandDefinition extends AbstractNotPersistentEntity implements ReportBand {
    private static final long serialVersionUID = 8658220979738705511L;

    @MetaProperty
    protected String name;

    @MetaProperty
    protected BandDefinition parentBandDefinition;

    @MetaProperty
    protected Report report;

    @MetaProperty
    protected List<BandDefinition> childrenBandDefinitions = new ArrayList<>();

    @MetaProperty
    protected List<DataSet> dataSets = new ArrayList<>();

    @MetaProperty
    protected Integer orientation;

    @MetaProperty
    protected Integer position;

    public BandDefinition getParentBandDefinition() {
        return parentBandDefinition;
    }

    public void setParentBandDefinition(BandDefinition parentBandDefinition) {
        this.parentBandDefinition = parentBandDefinition;
    }

    public List<BandDefinition> getChildrenBandDefinitions() {
        return childrenBandDefinitions;
    }

    public void setChildrenBandDefinitions(List<BandDefinition> childrenBandDefinitions) {
        this.childrenBandDefinitions = childrenBandDefinitions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DataSet> getDataSets() {
        return dataSets;
    }

    public void setDataSets(List<DataSet> dataSets) {
        this.dataSets = dataSets;
    }

    public Orientation getOrientation() {
        return orientation != null ? Orientation.fromId(orientation) : null;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation != null ? orientation.getId() : null;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position != null && position > 0 ? position : 0;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    @Override
    public ReportBand getParent() {
        return parentBandDefinition;
    }

    @Override
    public List<ReportBand> getChildren() {
        return (List) childrenBandDefinitions;
    }

    @Override
    public List<ReportQuery> getReportQueries() {
        return (List) dataSets;
    }

    @Override
    public BandOrientation  getBandOrientation() {
        return getOrientation() == Orientation.HORIZONTAL ? BandOrientation.HORIZONTAL : BandOrientation.VERTICAL;
    }
}
