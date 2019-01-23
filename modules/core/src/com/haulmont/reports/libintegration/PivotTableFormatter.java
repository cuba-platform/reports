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

import com.haulmont.cuba.core.entity.KeyValueEntity;
import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import com.haulmont.reports.entity.PivotTableData;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.reports.entity.pivottable.PivotTableDescription;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.AbstractFormatter;
import com.haulmont.yarg.structure.BandData;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PivotTableFormatter extends AbstractFormatter {

    public PivotTableFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
        this.rootBand = formatterFactoryInput.getRootBand();
        this.reportTemplate = formatterFactoryInput.getReportTemplate();
    }

    @Override
    public void renderDocument() {
        PivotTableDescription pivotTableDescription = ((ReportTemplate) reportTemplate).getPivotTableDescription();
        SerializationSupport.serialize(new PivotTableData(PivotTableDescription.toJsonString(pivotTableDescription), getEntries(pivotTableDescription)), outputStream);
    }

    protected List<KeyValueEntity> getEntries(PivotTableDescription configuration) {
        List<BandData> childrenByName = rootBand.getChildrenByName(configuration.getBandName());
        if (childrenByName == null)
            return Collections.emptyList();
        return childrenByName.stream()
                .filter(band -> band.getData() != null && !band.getData().isEmpty())
                .map(band -> {
                    KeyValueEntity entity = new KeyValueEntity();
                    band.getData().forEach(entity::setValue);
                    return entity;
                })
                .collect(Collectors.toList());
    }
}
