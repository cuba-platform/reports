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

/**
 * @author krivopustov
 * @version $Id$
 */
@Source(type = SourceType.APP)
public interface ReportingConfig extends Config {

    /**
     * @return Path to the installed OpenOffice
     */
    @Property("cuba.reporting.openoffice.path")
    @DefaultString("/")
    String getOpenOfficePath();

    /**
     * @return The list of ports to start OpenOffice on.
     */
    @Property("cuba.reporting.openoffice.ports")
    @DefaultString("8100|8101|8102|8103")
    String getOpenOfficePorts();

    /**
     * @return Request to OpenOffice timeout in seconds.
     */
    @Property("cuba.reporting.openoffice.docFormatterTimeout")
    @DefaultInteger(20)
    Integer getDocFormatterTimeout();

    /**
     * @return Has to be true if using OpenOffice reporting formatter on a *nix server without X server running
     */
    @Property("cuba.reporting.displayDeviceUnavailable")
    @DefaultBoolean(false)
    boolean getDisplayDeviceUnavailable();

    /**
     * @return Directory with fonts for generate PDF from HTML
     */
    @Property("cuba.reporting.fontsDir")
    String getPdfFontsDirectory();

    /**
     * @return The option which enforces standard data extractor to put empty row in each band if no data has been selected
     * In summary this option says - would table linked with empty band have at least one empty row or not.
     */
    @Property("cuba.reporting.putEmptyRowIfNoDataSelected")
    @DefaultBoolean(true)
    Boolean getPutEmptyRowIfNoDataSelected();

    void setPutEmptyRowIfNoDataSelected(Boolean putEmptyRowIfNoDataSelected);

    /**
     * @return Default limit used if parameter prototype object does not specify limit itself
     */
    @Property("cuba.reporting.parameterPrototype.queryLimit")
    @DefaultInteger(1000)
    Integer getParameterPrototypeQueryLimit();

    /**
     * Return entities that will not to be available for report wizard
     * Note that if <b>'cuba.reporting.wizardEntitiesBlackList'<b/> is not null, this list will be ignored
     *
     * @return list of ignored entities
     */
    @Property("cuba.reporting.wizardEntitiesBlackList")
    @DefaultString("")
    String getWizardEntitiesBlackList();

    void setWizardEntitiesBlackList(String wizardEntitiesBlackList);

    /**
     * Entities that will be available for report wizard. All others entities will be ignored
     * Note that if <b>'cuba.reporting.wizardEntitiesBlackList'<b/> is not null, this list will be used anyway.
     *
     * @return list of entities that available for reportWizard
     */
    @Property("cuba.reporting.wizardEntitiesWhiteList")
    @DefaultString("")
    String getWizardEntitiesWhiteList();

    void setWizardEntitiesWhiteList(String wizardEntitiesWhiteList);

    /**
     * Entity properties that will not be available for report creation wizard. Format is like 'BaseUuidEntity.id,BaseUuidEntity.createTs,ref$Car.id,...'<br/>
     * Properties support <b>inheritance</b>, i.e. BaseUuidEntity.id will filter that field for all descendants i.e. ref$Car.
     * To allow selection that field for concrete descendant of that instance (e.g. ref$Car) use
     * <b>'cuba.reporting.wizardPropertiesExcludedBlackList'</b> setting with value 'ref$Car.id'
     *
     * @return blacklisted properties that is not available
     */
    @Property("cuba.reporting.wizardPropertiesBlackList")
    @DefaultString("")
    String getWizardPropertiesBlackList();

    void setWizardPropertiesBlackList(String wizardPropertiesBlackList);

    /**
     * Entity properties that will not to be excluded by 'cuba.reporting.wizardPropertiesBlackList' setting
     * @see com.haulmont.reports.ReportingConfig#getWizardPropertiesBlackList()
     */
    @Property("cuba.reporting.wizardPropertiesExcludedBlackList")
    @DefaultString("")
    String getWizardPropertiesExcludedBlackList();

    void setWizardPropertiesExcludedBlackList(String wizardPropertiesExcludedBlackList);

    /**
     * Maximum deep of builded entity model that is used in report wizard or report dataset view editor
     *
     * @return deep value
     */
    @Property("cuba.reporting.entityTreeModelMaxDeep")
    @DefaultInteger(3)
    Integer getEntityTreeModelMaxDeep();

    void setEntityTreeModelMaxDeep(Integer entityTreeModelMaxDeep);


    @Property("cuba.reporting.html.externalResourcesTimeoutSec")
    @DefaultInteger(5)
    Integer getHtmlExternalResourcesTimeoutSec();

    void setHtmlExternalResourcesTimeoutSec(Integer externalResourcesTimeoutSec);
}
