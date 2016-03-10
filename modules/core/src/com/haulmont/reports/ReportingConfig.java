/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.DefaultBoolean;
import com.haulmont.cuba.core.config.defaults.DefaultInteger;
import com.haulmont.cuba.core.config.defaults.DefaultString;
import com.haulmont.cuba.core.config.type.CommaSeparatedStringListTypeFactory;
import com.haulmont.cuba.core.config.type.Factory;

import java.util.List;

/**
 * Reporting configuration interface.
 */
@Source(type = SourceType.APP)
public interface ReportingConfig extends Config {

    /**
     * @return Path to the installed OpenOffice
     */
    @Property("reporting.openoffice.path")
    @DefaultString("/")
    String getOpenOfficePath();

    /**
     * @return The list of ports to start OpenOffice on.
     */
    @Property("reporting.openoffice.ports")
    @DefaultString("8100|8101|8102|8103")
    String getOpenOfficePorts();

    /**
     * @return Request to OpenOffice timeout in seconds.
     */
    @Property("reporting.openoffice.docFormatterTimeout")
    @DefaultInteger(20)
    Integer getDocFormatterTimeout();

    /**
     * @return Has to be true if using OpenOffice reporting formatter on a *nix server without X server running
     */
    @Property("reporting.displayDeviceUnavailable")
    @DefaultBoolean(false)
    boolean getDisplayDeviceUnavailable();

    /**
     * @return Directory with fonts for generate PDF from HTML
     */
    @Property("reporting.fontsDir")
    String getPdfFontsDirectory();

    /**
     * @return The option which enforces standard data extractor to put empty row in each band if no data has been selected
     * In summary this option says - would table linked with empty band have at least one empty row or not.
     */
    @Property("reporting.putEmptyRowIfNoDataSelected")
    @Source(type = SourceType.DATABASE)
    @DefaultBoolean(true)
    Boolean getPutEmptyRowIfNoDataSelected();

    void setPutEmptyRowIfNoDataSelected(Boolean putEmptyRowIfNoDataSelected);

    /**
     * @return Default limit used if parameter prototype object does not specify limit itself
     */
    @Property("reporting.parameterPrototypeQueryLimit")
    @Source(type = SourceType.DATABASE)
    @DefaultInteger(1000)
    Integer getParameterPrototypeQueryLimit();

    /**
     * Return entities that will not be available for report wizard.
     * Note that if <code>reporting.wizardEntitiesWhiteList</code> is not empty, this list will be ignored
     *
     * @return list of ignored entities
     */
    @Property("reporting.wizardEntitiesBlackList")
    @Source(type = SourceType.DATABASE)
    @DefaultString("")
    String getWizardEntitiesBlackList();

    void setWizardEntitiesBlackList(String wizardEntitiesBlackList);

    /**
     * Entities that will be available for report wizard. All others entities will be ignored.
     * Note that even if <code>cuba.reporting.wizardEntitiesBlackList</code> is not empty, this list will be used anyway.
     *
     * @return list of entities that available for reportWizard
     */
    @Property("reporting.wizardEntitiesWhiteList")
    @Source(type = SourceType.DATABASE)
    @DefaultString("")
    String getWizardEntitiesWhiteList();

    void setWizardEntitiesWhiteList(String wizardEntitiesWhiteList);

    /**
     * Entity properties that will not be available for report creation wizard. Format is like <code>BaseUuidEntity.id,BaseUuidEntity.createTs,ref$Car.id,...</code><br/>
     * Properties support inheritance, i.e. <code>BaseUuidEntity.id</code> will filter that field for all descendants, e.g. <code>ref$Car</code>.
     * To allow selection of a field for a concrete descendant (e.g. <code>ref$Car</code>), use
     * <code>reporting.wizardPropertiesExcludedBlackList</code> setting with value <code>ref$Car.id</code>.
     *
     * @return blacklisted properties that is not available
     */
    @Property("reporting.wizardPropertiesBlackList")
    @Source(type = SourceType.DATABASE)
    @DefaultString("")
    @Factory(factory = CommaSeparatedStringListTypeFactory.class)
    List<String> getWizardPropertiesBlackList();

    void setWizardPropertiesBlackList(List<String> wizardPropertiesBlackList);

    /**
     * Entity properties that will not to be excluded by <code>reporting.wizardPropertiesBlackList</code> setting
     * @see com.haulmont.reports.ReportingConfig#getWizardPropertiesBlackList()
     */
    @Property("reporting.wizardPropertiesExcludedBlackList")
    @Source(type = SourceType.DATABASE)
    @DefaultString("")
    @Factory(factory =  CommaSeparatedStringListTypeFactory.class)
    List<String> getWizardPropertiesExcludedBlackList();

    void setWizardPropertiesExcludedBlackList(List<String> wizardPropertiesExcludedBlackList);

    /**
     * Maximum depth of entity model that is used in report wizard and report dataset view editor.
     */
    @Property("reporting.entityTreeModelMaxDepth")
    @Source(type = SourceType.DATABASE)
    @DefaultInteger(3)
    Integer getEntityTreeModelMaxDeep();

    void setEntityTreeModelMaxDeep(Integer entityTreeModelMaxDeep);


    @Property("reporting.html.externalResourcesTimeoutSec")
    @DefaultInteger(5)
    Integer getHtmlExternalResourcesTimeoutSec();

    void setHtmlExternalResourcesTimeoutSec(Integer externalResourcesTimeoutSec);

    /**
     * Reporting uses CURL tool to generate reports from URL. This is the system path to the tool.
     */
    @Property("reporting.curl.path")
    @DefaultString("curl")
    String getCurlPath();
    void setCurlPath(String value);

    /**
     * Reporting uses CURL tool to generate reports from URL. This the string with parameters used while calling CURL.
     */
    @Property("reporting.curl.params")
    @DefaultString("")
    String getCurlParams();
    void setCurlParams(String value);

    @Property("reporting.curl.timeoutSec")
    @DefaultInteger(10)
    Integer getCurlTimeout();
    void setCurlTimeout(Integer value);
}
