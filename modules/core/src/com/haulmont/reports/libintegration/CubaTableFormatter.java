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

import com.haulmont.bali.datastruct.Pair;
import com.haulmont.chile.core.model.Instance;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.KeyValueEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.MessageTools;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import com.haulmont.reports.app.EntityMap;
import com.haulmont.reports.entity.CubaTableData;
import com.haulmont.yarg.exception.ReportFormattingException;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.AbstractFormatter;
import com.haulmont.yarg.structure.BandData;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.*;

import static com.haulmont.reports.app.EntityMap.INSTANCE_NAME_KEY;
import static com.haulmont.reports.entity.wizard.ReportRegion.HEADER_BAND_PREFIX;

public class CubaTableFormatter extends AbstractFormatter {
    protected MessageTools messageTools;
    protected Metadata metadata;

    public CubaTableFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
        messageTools = AppBeans.get(MessageTools.class);
        metadata = AppBeans.get(Metadata.class);
    }

    @Override
    public void renderDocument() {
        CubaTableData dto = transformData(rootBand);
        byte[] serializedData = SerializationSupport.serialize(dto);
        try {
            IOUtils.write(serializedData, outputStream);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while rendering chart", e);
        }
    }

    protected CubaTableData transformData(BandData rootBand) {
        Map<String, List<KeyValueEntity>> transformedData = new LinkedHashMap<>();
        Map<String, List<Pair<String, Class>>> headerMap = new HashMap<>();
        Map<String, List<BandData>> childrenBands = rootBand.getChildrenBands();
        childrenBands.forEach((bandName, bandDataList) -> {
            if (!bandName.startsWith(HEADER_BAND_PREFIX)) {
                List<KeyValueEntity> entities = new ArrayList<>();
                Set<Pair<String, Class>> headers = new LinkedHashSet<>();

                bandDataList.forEach(bandData -> {
                    Map<String, Object> data = bandData.getData();
                    final Instance instance;
                    final String pkName;
                    final boolean pkInView;

                    if (data instanceof EntityMap) {
                        instance = ((EntityMap) data).getInstance();
                        pkName = metadata.getTools().getPrimaryKeyName(instance.getMetaClass());
                        View view = ((EntityMap) data).getView();
                        pkInView = view != null && pkName != null && view.containsProperty(pkName);
                    } else {
                        instance = null;
                        pkName = null;
                        pkInView = false;
                    }

                    KeyValueEntity entityRow = new KeyValueEntity();

                    data.forEach((name, value) -> {
                        if (!INSTANCE_NAME_KEY.equals(name)) {
                            if (pkName == null || !pkName.equals(name) || pkInView) {
                                if (instance != null) {
                                    name = messageTools.getPropertyCaption(instance.getMetaClass(), name);
                                }
                                checkInstanceNameLoaded(value);
                                entityRow.setValue(name, value);
                            }
                        }
                    });

                    if (headers.isEmpty() || headers.size() < data.size()) {
                        data.forEach((name, value) -> {
                            if (!INSTANCE_NAME_KEY.equals(name)) {
                                if (pkName == null || !pkName.equals(name) || pkInView) {
                                    if (instance != null) {
                                        name = messageTools.getPropertyCaption(instance.getMetaClass(), name);
                                    }

                                    if (name != null && value != null) {
                                        headers.add(new Pair<>(name, value.getClass()));
                                    }

                                    if (name != null && value == null) {
                                        headers.add(new Pair<>(name, String.class));
                                    }
                                }
                            }
                        });
                    }
                    entities.add(entityRow);
                });

                headers.removeIf(pair -> containsLowerCaseDuplicate(pair, headers));

                List<Pair<String, Class>> headersList = new LinkedList<>(headers);
                Collections.reverse(headersList);

                transformedData.put(bandName, entities);
                headerMap.put(bandName, headersList);
            }
        });

        return new CubaTableData(transformedData, headerMap);
    }

    protected boolean containsLowerCaseDuplicate(Pair<String, Class> pair, Set<Pair<String, Class>> headers) {
        if (!pair.getFirst().equals(pair.getFirst().toUpperCase()))
            return false;

        for (Pair<String, Class> header : headers) {
            if (!pair.equals(header) && header.getFirst().toUpperCase().equals(pair.getFirst()))
                return true;
        }

        return false;
    }

    protected void checkInstanceNameLoaded(Object value) {
        if (!(value instanceof Entity || value instanceof EntityMap))
            return;

        if (value instanceof EntityMap)
            value = ((EntityMap) value).getInstance();

        try {
            ((Entity) value).getInstanceName();
        } catch (RuntimeException e) {
            throw new ReportFormattingException("Cannot fetch instance name for entity " + value.getClass()
                    + ". Please add all attributes used at instance name to report configuration.", e);
        }
    }
}
