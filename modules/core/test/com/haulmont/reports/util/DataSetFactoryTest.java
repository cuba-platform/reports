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
