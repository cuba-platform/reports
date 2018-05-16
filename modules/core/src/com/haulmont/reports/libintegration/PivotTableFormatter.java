/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
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
