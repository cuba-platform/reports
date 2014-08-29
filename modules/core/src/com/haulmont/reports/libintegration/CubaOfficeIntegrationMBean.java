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

    public void setTimeoutInSeconds(Integer timeoutInSeconds);
    public Integer getTimeoutInSeconds();

    public void setDisplayDeviceAvailable(Boolean displayDeviceAvailable);
    public Boolean isDisplayDeviceAvailable();

    void setTemporaryDirPath(String temporaryDirPath);
    String getTemporaryDirPath();

    public String getAvailablePorts();
    public void hardReloadAccessPorts();
}
