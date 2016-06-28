/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

/**
 *
 */
package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.reports.ReportingConfig;
import com.haulmont.yarg.loaders.factory.ReportLoaderFactory;
import com.haulmont.yarg.reporting.DataExtractorImpl;

import javax.inject.Inject;

public class CubaDataExtractor extends DataExtractorImpl {
    @Inject
    protected Configuration configuration;

    public CubaDataExtractor(ReportLoaderFactory loaderFactory) {
        super(loaderFactory);
    }

    @Override
    public boolean getPutEmptyRowIfNoDataSelected() {
        return Boolean.TRUE.equals(configuration.getConfig(ReportingConfig.class).getPutEmptyRowIfNoDataSelected());
    }
}