/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.yarg.formatters.CustomReport;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Template for {@link Report}
 *
 * @author artamonov
 * @version $Id$
 */
@Entity(name = "report$ReportTemplate")
@Table(name = "REPORT_TEMPLATE")
@SystemLevel
@NamePattern("(%s) %s|code,name")
@SuppressWarnings("unused")
public class ReportTemplate extends BaseReportEntity implements com.haulmont.yarg.structure.ReportTemplate {

    private static final long serialVersionUID = 3692751073234357754L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPORT_ID", nullable = false)
    protected Report report;

    @Column(name = "OUTPUT_TYPE")
    protected Integer reportOutputType;

    @Column(name = "CODE")
    protected String code;

    @Column(name = "IS_CUSTOM")
    protected Boolean customFlag = false;

    @Column(name = "CUSTOM_CLASS")
    protected String customClass;

    @Column(name = "OUTPUT_NAME_PATTERN")
    protected String outputNamePattern;

    @Column(name = "NAME", length = 500)
    protected String name;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "CONTENT")
    protected byte[] content;

    @Transient
    protected CustomReport customReport;

    public ReportOutputType getReportOutputType() {
        return reportOutputType != null ? ReportOutputType.fromId(reportOutputType) : null;
    }

    public void setReportOutputType(ReportOutputType reportOutputType) {
        this.reportOutputType = reportOutputType != null ? reportOutputType.getId() : null;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public Boolean getCustomFlag() {
        return customFlag;
    }

    public void setCustomFlag(Boolean customFlag) {
        this.customFlag = customFlag;
    }

    public String getCustomClass() {
        return customClass;
    }

    public void setCustomClass(String customClass) {
        this.customClass = customClass;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExt(){
        return StringUtils.substringAfterLast(name, ".");
    }

    @Override
    public String getDocumentName() {
        return name;
    }

    @Override
    public String getDocumentPath() {
        return name;
    }

    @Override
    public InputStream getDocumentContent() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public com.haulmont.yarg.structure.ReportOutputType getOutputType() {
        return getReportOutputType().getOutputType();
    }

    public void setOutputNamePattern(String outputNamePattern) {
        this.outputNamePattern = outputNamePattern;
    }

    @Override
    public String getOutputNamePattern() {
        return outputNamePattern;
    }

    @Override
    public boolean isCustom() {
        return Boolean.TRUE.equals(customFlag);
    }

    @Override
    public CustomReport getCustomReport() {
        return customReport;
    }

    public void setCustomReport(CustomReport customReport) {
        this.customReport = customReport;
    }
}
