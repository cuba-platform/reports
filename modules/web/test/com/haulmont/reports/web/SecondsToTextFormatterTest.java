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

package com.haulmont.reports.web;

import com.haulmont.cuba.web.testsupport.TestUiEnvironment;
import com.haulmont.reports.gui.report.history.SecondsToTextFormatter;
import com.haulmont.reports.web.testsupport.ReportsWebTestContainer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SecondsToTextFormatterTest {
    @Rule
    public TestUiEnvironment environment =
            new TestUiEnvironment(ReportsWebTestContainer.Common.INSTANCE).withUserLogin("admin");

    private SecondsToTextFormatter formatter;

    @Before
    public void setUp() {
        formatter = new SecondsToTextFormatter();
    }

    @Test
    public void testFormatting() {
        Assert.assertNull(formatter.apply(null));
        Assert.assertEquals("45 sec", formatter.apply(45L));
        Assert.assertEquals("5 min 39 sec", formatter.apply(339L));
        Assert.assertEquals("2 h 31 min 29 sec", formatter.apply(9089L));
    }

}
