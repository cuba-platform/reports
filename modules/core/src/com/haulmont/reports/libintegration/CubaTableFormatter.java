/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.bali.datastruct.Pair;
import com.haulmont.cuba.core.entity.KeyValueEntity;
import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import com.haulmont.reports.entity.tables.dto.CubaTableDTO;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.AbstractFormatter;
import com.haulmont.yarg.structure.BandData;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.*;

public class CubaTableFormatter extends AbstractFormatter {

    public CubaTableFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
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
            List<KeyValueEntity> entities = new ArrayList<>();
            Set<Pair<String, Class>> headers = new HashSet<>();
            Set<String> emptyHeaders = new HashSet<>();

            bandDataList.forEach(bandData -> {
                Map<String, Object> data = bandData.getData();
                KeyValueEntity entity = new KeyValueEntity();
                data.forEach(entity::setValue);
                if (headers.isEmpty() || headers.size() < data.size()) {
                    data.forEach((s, o) -> {
                        if (s != null && o != null)
                            headers.add(new Pair<>(s, o.getClass()));
                        if (s != null && o == null)
                            emptyHeaders.add(s);
                    });
                }
                entities.add(entity);
            });

            emptyHeaders.forEach(header -> {
                if (!containsHeader(headers, header))
                    headers.add(new Pair<>(header, String.class));
            });

            transformedData.put(bandName, entities);
            headerMap.put(bandName, headers);
        });

        return new CubaTableDTO(transformedData, headerMap);
    }

    protected boolean containsHeader(Set<Pair<String, Class>> headers, String header) {
        for (Pair<String, Class> pair : headers) {
            if (pair.getFirst().equals(header))
                return true;
        }
        return false;
    }
}
