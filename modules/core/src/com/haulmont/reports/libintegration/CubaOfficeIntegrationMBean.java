/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: degtyarjov
 * Created: 24.10.13 13:21
 *
 * $Id$
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
