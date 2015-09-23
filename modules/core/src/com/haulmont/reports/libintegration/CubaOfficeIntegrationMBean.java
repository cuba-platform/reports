/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.libintegration;

/**
 * @author degtyarjov
 * @version $Id$
 */
public interface CubaOfficeIntegrationMBean {

    void setTimeoutInSeconds(Integer timeoutInSeconds);
    Integer getTimeoutInSeconds();

    void setDisplayDeviceAvailable(Boolean displayDeviceAvailable);
    Boolean isDisplayDeviceAvailable();

    void setTemporaryDirPath(String temporaryDirPath);
    String getTemporaryDirPath();

    String getAvailablePorts();
    void hardReloadAccessPorts();
}