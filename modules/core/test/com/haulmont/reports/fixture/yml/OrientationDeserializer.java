/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.fixture.yml;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.haulmont.reports.entity.Orientation;
import com.haulmont.yarg.structure.BandOrientation;

import java.io.IOException;

/**
 * Jackson deserializer implementation for {@link Orientation}
 */
public class OrientationDeserializer extends JsonDeserializer<Orientation> {
    @Override
    public Orientation deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return Orientation.fromBandOrientation(BandOrientation.defaultIfNull(BandOrientation.fromId(p.getValueAsString())));
    }
}
