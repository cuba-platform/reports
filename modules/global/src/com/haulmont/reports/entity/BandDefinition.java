/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.entity;

import com.haulmont.chile.core.annotations.Composition;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportQuery;
import com.haulmont.yarg.structure.impl.BandOrientation;

import javax.persistence.*;
import java.util.List;

/**
 * @author degtyarjov
 * @version $Id$
 */
@Entity(name = "report$BandDefinition")
@Table(name = "REPORT_BAND_DEFINITION")
@NamePattern("%s|name")
@SystemLevel
public class BandDefinition extends BaseReportEntity implements ReportBand {
    private static final long serialVersionUID = 8658220979738705511L;

    @Column(name = "NAME")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_DEFINITION_ID")
    private BandDefinition parentBandDefinition;

    @ManyToOne
    @JoinColumn(name = "REPORT_ID")
    private Report report;

    @OneToMany(mappedBy = "parentBandDefinition")
    @Composition
    @OrderBy("position")
    private List<BandDefinition> childrenBandDefinitions;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "bandDefinition")
    @Composition
    private List<DataSet> dataSets;

    @Column(name = "ORIENTATION")
    private Integer orientation;

    @Column(name = "POSITION_")
    private Integer position;

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
