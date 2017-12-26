/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.fixture.yml;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haulmont.reports.entity.BandDefinition;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

/**
 * Util class for {@link BandDefinition} extraction from YAML configuration file
 */
public final class YmlBandUtil {
    private YmlBandUtil() {
    }

    static ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public static BandDefinition bandFrom(File file) throws IOException {
        return mapper.readValue(file, YmlBandDefinition.class);
    }
}
