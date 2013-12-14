/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.reports.ReportingConfig;
import com.haulmont.yarg.loaders.factory.ReportLoaderFactory;
import com.haulmont.yarg.reporting.DataExtractorImpl;

import javax.inject.Inject;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class CubaDataExtractorImpl extends DataExtractorImpl {
    @Inject
    protected Configuration configuration;

    public CubaDataExtractorImpl(ReportLoaderFactory loaderFactory) {
        super(loaderFactory);
    }

    @Override
    public boolean getPutEmptyRowIfNoDataSelected() {
        return Boolean.TRUE.equals(configuration.getConfig(ReportingConfig.class).getPutEmptyRowIfNoDataSelected());
    }
}