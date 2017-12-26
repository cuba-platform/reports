/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.util;

import com.haulmont.reports.entity.BandDefinition;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.reports.entity.DataSetType;
import com.haulmont.reports.fixture.yml.YmlBandUtil;
import com.haulmont.reports.testsupport.ReportsContextBootstrapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;

@RunWith(SpringJUnit4ClassRunner.class)
@BootstrapWith(ReportsContextBootstrapper.class)
public class DataSetFactoryTest {

    @Inject
    private DataSetFactory dataSetFactory;

    @Inject
    private ResourceLoader resourceLoader;

    @Test
    public void testDataSetEmptyCreation() throws URISyntaxException, IOException {
        BandDefinition bandDefinition = YmlBandUtil.bandFrom(
                resourceLoader.getResource("com/haulmont/reports/fixture/dataset-creation.yml").getFile());
        Assert.assertNotNull(bandDefinition);
        DataSet dataSet = dataSetFactory.createEmptyDataSet(bandDefinition);

        Assert.assertEquals(DataSetType.GROOVY, dataSet.getType());
        Assert.assertEquals(bandDefinition, dataSet.getBandDefinition());
    }
}
