/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.util;

import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.reports.entity.BandDefinition;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.reports.entity.DataSetType;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Class presents factory bean for create {@link DataSet} instance with basic filled attributes
 */
@Component("report_DataSetFactory")
public class DataSetFactory {

    @Inject
    protected Metadata metadata;

    /**
     * Methods create {@link DataSet} instance with basic filled
     * @param dataBand with some filled attributes
     * @return new instance of {@link DataSet}
     */
    public DataSet createEmptyDataSet(BandDefinition dataBand) {
        checkNotNull(dataBand);

        DataSet dataSet = metadata.create(DataSet.class);
        dataSet.setBandDefinition(dataBand);
        dataSet.setType(DataSetType.GROOVY);
        dataSet.setText("return [[:]]");
        return dataSet;
    }
}
