/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.libintegration;

public interface CubaOfficeIntegrationMBean {
    public void setTimeoutInSeconds(Integer timeoutInSeconds);
    public void setDisplayDeviceAvailable(Boolean displayDeviceAvailable);
    public Integer getTimeoutInSeconds();
    public Boolean isDisplayDeviceAvailable();
    public String getAvailablePorts();
    public void hardReloadAccessPorts();
}
