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
import com.haulmont.reports.entity.ParameterType;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportGroup;
import com.haulmont.reports.entity.ReportOutputType;

import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fedorchenko
 * @version $Id$
 */
@MetaClass(name = "report$WizardReportData")
@SystemLevel
public class ReportData extends AbstractNotPersistentEntity {
    public static class Parameter implements Serializable {
        public final String name;
        public final Class javaClass;
        public final ParameterType parameterType;

        public Parameter(String name, Class javaClass, ParameterType parameterType) {
            this.name = name;
            this.javaClass = javaClass;
            this.parameterType = parameterType;
        }
    }

    public static enum ReportType {
        SINGLE_ENTITY(false, true),
        LIST_OF_ENTITIES(true, true),
        LIST_OF_ENTITIES_WITH_QUERY(true, false);

        private boolean list;
        private boolean entity;

        ReportType(boolean list, boolean entity) {
            this.list = list;
            this.entity = entity;
        }

        public boolean isList() {
            return list;
        }

        public boolean isEntity() {
            return entity;
        }
    }

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
    protected ReportType reportType;

    @MetaProperty
    @Transient
    protected String templateFileName;

    @MetaProperty
    @Transient
    protected String outputNamePattern;

    @MetaProperty
    @Transient
    protected ReportOutputType outputFileType;

    @MetaProperty
    @Composition
    @Transient
    @OneToMany(targetEntity = RegionProperty.class)
    protected List<ReportRegion> reportRegions = new ArrayList<>();

    @Transient
    protected String query;

    @Transient
    protected List<Parameter> queryParameters;

    @Transient
    protected TemplateFileType templateFileType;

    @Transient
    byte[] templateContent;

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

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
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

    public String getOutputNamePattern() {
        return outputNamePattern;
    }

    public void setOutputNamePattern(String outputNamePattern) {
        this.outputNamePattern = outputNamePattern;
    }

    @Transient
    public void removeRegion(int index) {
        reportRegions.remove(index);
    }

    @Transient
    public void clearRegions() {
        reportRegions.clear();
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<Parameter> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(List<Parameter> queryParameters) {
        this.queryParameters = queryParameters;
    }

    public TemplateFileType getTemplateFileType() {
        return templateFileType;
    }

    public void setTemplateFileType(TemplateFileType templateFileType) {
        this.templateFileType = templateFileType;
    }

    public byte[] getTemplateContent() {
        return templateContent;
    }

    public void setTemplateContent(byte[] templateContent) {
        this.templateContent = templateContent;
    }
}
