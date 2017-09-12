package com.haulmont.reports.gui.datasource;

import com.haulmont.cuba.gui.data.impl.CollectionPropertyDatasourceImpl;
import com.haulmont.reports.entity.ParameterType;
import com.haulmont.reports.entity.ReportInputParameter;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class TextInputParametersDataSource extends CollectionPropertyDatasourceImpl<ReportInputParameter, UUID> {

    @Override
    protected Collection<ReportInputParameter> getCollection() {
        return super.getCollection().stream()
                .filter(reportInputParameter -> reportInputParameter.getType().equals(ParameterType.TEXT))
                .collect(Collectors.toList());
    }
}
