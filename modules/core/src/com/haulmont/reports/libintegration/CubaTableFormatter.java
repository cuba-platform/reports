/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.bali.datastruct.Pair;
import com.haulmont.chile.core.model.Instance;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.KeyValueEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.MessageTools;
import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import com.haulmont.reports.app.EntityMap;
import com.haulmont.reports.entity.tables.dto.CubaTableDTO;
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

    public CubaTableFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
        messageTools = AppBeans.get(MessageTools.class);
    }

    @Override
    public void renderDocument() {
        CubaTableDTO dto = transformData(rootBand);
        byte[] serializedData = SerializationSupport.serialize(dto);
        try {
            IOUtils.write(serializedData, outputStream);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while rendering chart",e);
        }
    }

    protected CubaTableDTO transformData(BandData rootBand) {
        Map<String, List<KeyValueEntity>> transformedData = new LinkedHashMap<>();
        Map<String, Set<Pair<String, Class>>> headerMap = new HashMap<>();
        Map<String, List<BandData>> childrenBands = rootBand.getChildrenBands();
        childrenBands.forEach((bandName, bandDataList) -> {
            if (!bandName.startsWith(HEADER_BAND_PREFIX)) {
                List<KeyValueEntity> entities = new ArrayList<>();
                Set<Pair<String, Class>> headers = new HashSet<>();
                Set<String> emptyHeaders = new HashSet<>();

                bandDataList.forEach(bandData -> {
                    Map<String, Object> data = bandData.getData();
                    Instance instance = (data instanceof EntityMap) ? ((EntityMap) data).getInstance() : null;
                    KeyValueEntity entityRow = new KeyValueEntity();

                    checkInstanceNameLoaded(data);
                    data.forEach((name, value) -> {
                        if (!INSTANCE_NAME_KEY.equals(name)) {
                            if (instance != null)
                                name = messageTools.getPropertyCaption(instance.getMetaClass(), name);
                            checkInstanceNameLoaded(value);
                            entityRow.setValue(name, value);
                        }
                    });

                    if (headers.isEmpty() || headers.size() < data.size()) {
                        data.forEach((name, value) -> {
                            if (!INSTANCE_NAME_KEY.equals(name)) {
                                if (instance != null)
                                    name = messageTools.getPropertyCaption(instance.getMetaClass(), name);

                                if (name != null && value != null)
                                    headers.add(new Pair<>(name, value.getClass()));
                                if (name != null && value == null)
                                    emptyHeaders.add(name);
                            }
                        });
                    }
                    entities.add(entityRow);
                });

                emptyHeaders.forEach(header -> {
                    if (!containsHeader(headers, header))
                        headers.add(new Pair<>(header, String.class));
                });

                headers.removeIf(pair -> containsLowerCaseDuplicate(pair, headers));

                transformedData.put(bandName, entities);
                headerMap.put(bandName, headers);
            }
        });

        return new CubaTableDTO(transformedData, headerMap);
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
        }
        catch (RuntimeException e) {
            throw new ReportFormattingException("Cannot fetch instance name for entity " + value.getClass()
                    + ". Please add all attributes used at instance name to report configuration.", e);
        }
    }

    protected boolean containsHeader(Set<Pair<String, Class>> headers, String header) {
        for (Pair<String, Class> pair : headers) {
            if (pair.getFirst().equals(header))
                return true;
        }
        return false;
    }
}
