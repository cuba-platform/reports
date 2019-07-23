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
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import com.haulmont.reports.app.EntityMap;
import com.haulmont.reports.entity.CubaTableData;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.reports.entity.table.TemplateTableBand;
import com.haulmont.reports.entity.table.TemplateTableColumn;
import com.haulmont.reports.entity.table.TemplateTableDescription;
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
    protected MetadataTools metadataTools;

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
        TemplateTableDescription templateTableDescription = ((ReportTemplate) reportTemplate).getTemplateTableDescription();

        Map<String, List<KeyValueEntity>> transformedData = new LinkedHashMap<>();
        Map<String, List<Pair<String, Class>>> headerMap = new HashMap<>();
        Map<String, List<BandData>> childrenBands = rootBand.getChildrenBands();

        for (TemplateTableBand templateTableBand : templateTableDescription.getTemplateTableBands()) {
            String bandName = templateTableBand.getBandName();

            if (bandName.startsWith(HEADER_BAND_PREFIX)) {
                break;
            }

            List<BandData> bandDataList = childrenBands.get(templateTableBand.getBandName());

            List<KeyValueEntity> entities = new ArrayList<>();
            Set<Pair<String, Class>> headers = new LinkedHashSet<>();

            for (BandData bandData : bandDataList) {
                Map<String, Object> data = bandData.getData();
                Instance instance = null;
                String pkName = null;
                boolean pkInView = false;

                if (data instanceof EntityMap) {
                    instance = ((EntityMap) data).getInstance();
                    pkName = metadata.getTools().getPrimaryKeyName(instance.getMetaClass());

                    View view = ((EntityMap) data).getView();
                    pkInView = view != null && pkName != null && view.containsProperty(pkName);
                }

                KeyValueEntity entityRow = getKeyValueEntity(data, templateTableBand, pkName, pkInView);
                entities.add(entityRow);

                if (templateTableBand.getTemplateTableColumns().size() == 0) {
                    getHeaders(headers, data, instance, pkName, pkInView);
                } else {
                    for (TemplateTableColumn templateTableColumn : templateTableBand.getTemplateTableColumns()) {
                        Object value = data.get(templateTableColumn.getColumn());

                        Pair<String, Class> columnPair = value != null ?
                                new Pair<>(templateTableColumn.getColumnName(), value.getClass()) :
                                new Pair<>(templateTableColumn.getColumnName(), String.class);

                        headers.add(columnPair);
                    }
                }
            }

            headers.removeIf(pair -> containsLowerCaseDuplicate(pair, headers));
            List<Pair<String, Class>> headersList = new LinkedList<>(headers);

            transformedData.put(bandName, entities);
            headerMap.put(bandName, headersList);
        }

        return new CubaTableData(transformedData, headerMap);
    }

    private void getHeaders(Set<Pair<String, Class>> headers, Map<String, Object> data, Instance instance, String pkName, boolean pkInView) {
        if (headers.isEmpty() || headers.size() < data.size()) {
            data.forEach((name, value) -> {
                if (INSTANCE_NAME_KEY.equals(name)) {
                    return;
                }

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
            });
        }
    }

    private KeyValueEntity getKeyValueEntity(Map<String, Object> data, TemplateTableBand templateTableBand, String pkName, boolean pkInView) {
        KeyValueEntity entityRow = new KeyValueEntity();

        for (TemplateTableColumn templateTableColumn : templateTableBand.getTemplateTableColumns()) {
            String name = templateTableColumn.getColumn();
            Object value = data.get(name);

            if (INSTANCE_NAME_KEY.equals(name)) {
                break;
            }

            if (pkName == null || !pkName.equals(name) || pkInView) {
                name = templateTableColumn.getColumnName();

                checkInstanceNameLoaded(value);
                entityRow.setValue(name, value);
            }
        }
        return entityRow;
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
            metadataTools.getInstanceName((Entity)value);
        } catch (RuntimeException e) {
            throw new ReportFormattingException("Cannot fetch instance name for entity " + value.getClass()
                    + ". Please add all attributes used at instance name to report configuration.", e);
        }
    }
}
