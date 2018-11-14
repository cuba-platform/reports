/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.entity;

import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.LocaleHelper;
import com.haulmont.cuba.core.entity.StandardEntity;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity(name = "report$ReportGroup")
@Table(name = "REPORT_GROUP")
@NamePattern("#getLocName|title,localeNames")
@SuppressWarnings("unused")
public class ReportGroup extends StandardEntity {

    private static final long serialVersionUID = 5399528790289039413L;

    @Column(name = "TITLE", unique = true, nullable = false)
    private String title;

    @Column(name = "CODE")
    private String code;

    @Column(name = "LOCALE_NAMES")
    private String localeNames;

    @Transient
    private String localeName;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLocaleNames() {
        return localeNames;
    }

    public void setLocaleNames(String localeNames) {
        this.localeNames = localeNames;
    }

    @MetaProperty
    public String getLocName() {
        if (localeName == null) {
            localeName = LocaleHelper.getLocalizedName(localeNames);
            if (localeName == null)
                localeName = title;
        }
        return localeName;
    }

    @MetaProperty(related = "code")
    public Boolean getSystemFlag() {
        return StringUtils.isNotEmpty(code);
    }
}