/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.DefaultBoolean;
import com.haulmont.cuba.core.config.defaults.DefaultLong;

/**
 */
@Source(type = SourceType.DATABASE)
public interface ReportingClientConfig extends Config {

    @Property("reporting.useBackgroundReportProcessing")
    @DefaultBoolean(false)
    boolean getUseBackgroundReportProcessing();
    void setUseBackgroundReportProcessing(boolean useBackgroundReportProcessing);

    @Property("reporting.backgroundReportProcessingTimeoutMs")
    @DefaultLong(10000)
    long getBackgroundReportProcessingTimeoutMs();
    void setBackgroundReportProcessingTimeoutMs(long backgroundReportProcessingTimeoutMs);
}