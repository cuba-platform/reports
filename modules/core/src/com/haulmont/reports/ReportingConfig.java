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
}
