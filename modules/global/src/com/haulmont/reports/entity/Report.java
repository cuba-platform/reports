/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.entity;

import com.haulmont.chile.core.annotations.Composition;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.annotation.Listeners;
import com.haulmont.cuba.core.entity.annotation.OnDelete;
import com.haulmont.cuba.core.global.DeletePolicy;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportFieldFormat;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.util.*;

/**
 * <Attention>This entity should be detached for correct work. If you do not detach it please use logic as in com.haulmont.reports.listener.ReportDetachListener#onBeforeDetach(com.haulmont.reports.entity.Report, com.haulmont.cuba.core.EntityManager)</Attention>
 *
 * @author degtyarjov
 * @version $Id$
 */
@Entity(name = "report$Report")
@Table(name = "REPORT_REPORT")
@NamePattern("%s|locName,name,localeNames")
@Listeners("report_ReportDetachListener")
@SuppressWarnings("unused")
public class Report extends BaseReportEntity implements com.haulmont.yarg.structure.Report {
    private static final long serialVersionUID = -2817764915661205093L;

    @Column(name = "NAME")
    protected String name;

    @Column(name = "LOCALE_NAMES")
    protected String localeNames;

    @Column(name = "CODE")
    protected String code;

    @ManyToOne
    @JoinColumn(name = "GROUP_ID")
    protected ReportGroup group;

    @OneToOne
    @JoinColumn(name = "DEFAULT_TEMPLATE_ID")
    protected ReportTemplate defaultTemplate;

    @Column(name = "REPORT_TYPE")
    protected Integer reportType;

    @Column(name = "XML")
    protected String xml;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "report")
    @Composition
    protected List<ReportTemplate> templates;

    @Transient
    protected BandDefinition rootBandDefinition;

    @Transient @MetaProperty
    protected Set<BandDefinition> bands = new HashSet<>();

    @Transient @MetaProperty @Composition
    protected List<ReportInputParameter> inputParameters = new ArrayList<>();

    @Transient @MetaProperty @Composition
    protected List<ReportValueFormat> valuesFormats = new ArrayList<>();

    @Transient @MetaProperty
    protected List<ReportScreen> reportScreens = new ArrayList<>();

    @Transient @MetaProperty
    protected Set<Role> roles = new HashSet<>();

    @Transient
    protected String localeName;

    @Transient
    protected Boolean isTmp = Boolean.FALSE;

    public Boolean getIsTmp() {
        return isTmp;
    }

    public void setIsTmp(Boolean isTmp) {
        this.isTmp = isTmp;
    }

    @MetaProperty
    public BandDefinition getRootBandDefinition() {
        if (rootBandDefinition == null && bands != null && bands.size() > 0) {
            rootBandDefinition = (BandDefinition) CollectionUtils.find(bands, new Predicate() {
                @Override
                public boolean evaluate(Object object) {
                    BandDefinition band = (BandDefinition) object;
                    return band.getParentBandDefinition() == null;
                }
            });
        }
        return rootBandDefinition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ReportInputParameter> getInputParameters() {
        return inputParameters;
    }

    public void setInputParameters(List<ReportInputParameter> inputParameters) {
        if (inputParameters == null) inputParameters = Collections.emptyList();
        this.inputParameters = inputParameters;
    }

    public List<ReportValueFormat> getValuesFormats() {
        return valuesFormats;
    }

    public void setValuesFormats(List<ReportValueFormat> valuesFormats) {
        if (valuesFormats == null) valuesFormats = Collections.emptyList();
        this.valuesFormats = valuesFormats;
    }

    public ReportType getReportType() {
        return reportType != null ? ReportType.fromId(reportType) : null;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType != null ? reportType.getId() : null;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        if (roles == null) roles = Collections.emptySet();
        this.roles = roles;
    }

    public List<ReportScreen> getReportScreens() {
        return reportScreens;
    }

    public void setReportScreens(List<ReportScreen> reportScreens) {
        if (reportScreens == null) reportScreens = Collections.emptyList();
        this.reportScreens = reportScreens;
    }

    public List<ReportTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(List<ReportTemplate> templates) {
        this.templates = templates;
    }

    public ReportTemplate getDefaultTemplate() {
        return defaultTemplate;
    }

    public void setDefaultTemplate(ReportTemplate defaultTemplate) {
        this.defaultTemplate = defaultTemplate;
    }

    public ReportTemplate getTemplateByCode(String templateCode) {
        ReportTemplate template = null;
        if (templates != null) {
            Iterator<ReportTemplate> iter = templates.iterator();
            while (iter.hasNext() && template == null) {
                ReportTemplate temp = iter.next();
                if (StringUtils.equalsIgnoreCase(temp.getCode(), templateCode)) {
                    template = temp;
                }
            }
        }
        return template;
    }

    public ReportGroup getGroup() {
        return group;
    }

    public void setGroup(ReportGroup group) {
        this.group = group;
    }

    public Set<BandDefinition> getBands() {
        return bands;
    }

    public void setBands(Set<BandDefinition> bands) {
        if (bands == null) bands = Collections.emptySet();
        this.bands = bands;
    }

    public String getLocaleNames() {
        return localeNames;
    }

    public void setLocaleNames(String localeNames) {
        this.localeNames = localeNames;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    @MetaProperty
    public String getLocName() {
        if (localeName == null) {
            localeName = LocaleHelper.getLocalizedName(localeNames);
            if (localeName == null)
                localeName = name;
        }
        return localeName;
    }

    @Override
    public Map<String, com.haulmont.yarg.structure.ReportTemplate> getReportTemplates() {
        Map<String, com.haulmont.yarg.structure.ReportTemplate> templateMap = new HashMap<>();
        for (ReportTemplate template : templates) {
            templateMap.put(template.getCode(), template);
        }

        return templateMap;
    }

    @Override
    public List<com.haulmont.yarg.structure.ReportParameter> getReportParameters() {
        return (List) inputParameters;
    }

    @Override
    public List<ReportFieldFormat> getReportFieldFormats() {
        return (List) valuesFormats;
    }

    @Override
    public ReportBand getRootBand() {
        return getRootBandDefinition();
    }
}