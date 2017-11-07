/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.libintegration;

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