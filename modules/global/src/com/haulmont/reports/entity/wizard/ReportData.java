/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.entity.wizard;

import com.haulmont.chile.core.annotations.Composition;
import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportGroup;
import com.haulmont.reports.entity.ReportOutputType;

import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fedorchenko
 * @version $Id$
 */
@MetaClass(name = "report$WizardReportData")
@SystemLevel
public class ReportData extends AbstractNotPersistentEntity {
    @MetaProperty
    @Transient
    protected String name;
    @MetaProperty
    @Transient
    protected EntityTreeNode entityTreeRootNode;
    @MetaProperty
    @Transient
    protected Report generatedReport;
    @MetaProperty
    @Transient
    protected ReportGroup group;
    @MetaProperty
    @Transient
    protected Boolean isTabulatedReport;
    @MetaProperty
    @Transient
    protected String templateFileName;
    @MetaProperty
    @Transient
    protected ReportOutputType outputFileType;
    @MetaProperty
    @Composition
    @Transient
    @OneToMany(targetEntity = RegionProperty.class)
    protected List<ReportRegion> reportRegions = new ArrayList<>();

    public Report getGeneratedReport() {
        return generatedReport;
    }

    public void setGeneratedReport(Report generatedReport) {
        this.generatedReport = generatedReport;
    }

    public ReportGroup getGroup() {
        return group;
    }

    public void setGroup(ReportGroup group) {
        this.group = group;
    }

    public Boolean getIsTabulatedReport() {
        return isTabulatedReport;
    }

    public void setIsTabulatedReport(Boolean isTabulatedReport) {
        this.isTabulatedReport = isTabulatedReport;
    }

    public String getTemplateFileName() {
        return templateFileName;
    }

    public void setTemplateFileName(String templateFileName) {
        this.templateFileName = templateFileName;
    }

    public ReportOutputType getOutputFileType() {
        return outputFileType;
    }

    public void setOutputFileType(ReportOutputType outputFileType) {
        this.outputFileType = outputFileType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityTreeNode getEntityTreeRootNode() {
        return entityTreeRootNode;
    }

    public void setEntityTreeRootNode(EntityTreeNode entityTreeRootNode) {
        this.entityTreeRootNode = entityTreeRootNode;
    }

    public List<ReportRegion> getReportRegions() {
        return reportRegions;
    }

    public void setReportRegions(List<ReportRegion> reportRegions) {
        this.reportRegions = reportRegions;
    }

    @Transient
    public ReportData addRegion(ReportRegion region) {
        reportRegions.add(region);
        return this;
    }

    @Transient
    public ReportData addRegion(int index, ReportRegion region) {
        reportRegions.add(index, region);
        return this;
    }

    @Transient
    public void removeRegion(int index) {
        reportRegions.remove(index);
    }

    @Transient
    public void clearRegions() {
        reportRegions.clear();
    }

}
