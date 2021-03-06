/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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