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
