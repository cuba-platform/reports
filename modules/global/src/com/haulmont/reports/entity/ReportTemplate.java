/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports.entity;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.yarg.formatters.CustomReport;

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
@SuppressWarnings("unused")
public class ReportTemplate extends BaseReportEntity implements com.haulmont.yarg.structure.ReportTemplate {

    private static final long serialVersionUID = 3692751073234357754L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPORT_ID")
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEMPLATE_FILE_ID")
    private FileDescriptor templateFileDescriptor;

    @Column(name = "OUTPUT_TYPE")
    private Integer reportOutputType;

    @Column(name = "CODE")
    private String code;

    @Column(name = "IS_DEFAULT")
    private Boolean defaultFlag;

    @Column(name = "IS_CUSTOM")
    private Boolean customFlag = false;

    @Column(name = "CUSTOM_CLASS")
    private String customClass;

    @Column(name = "OUTPUT_NAME_PATTERN")
    private String outputNamePattern;

    @Transient
    private byte[] content;

    @Transient
    private CustomReport customReport;

    public FileDescriptor getTemplateFileDescriptor() {
        return templateFileDescriptor;
    }

    public void setTemplateFileDescriptor(FileDescriptor templateFileDescriptor) {
        this.templateFileDescriptor = templateFileDescriptor;
    }

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

    public Boolean getDefaultFlag() {
        return defaultFlag;
    }

    public void setDefaultFlag(Boolean defaultFlag) {
        this.defaultFlag = defaultFlag;
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

    @Override
    public String getDocumentName() {
        return templateFileDescriptor.getName();
    }

    @Override
    public String getDocumentPath() {
        return templateFileDescriptor.getName();
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

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setCustomReport(CustomReport customReport) {
        this.customReport = customReport;
    }
}
