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

package com.haulmont.reports.core;

import com.haulmont.cuba.testsupport.CubaTestCase;
import com.haulmont.cuba.testsupport.TestContext;
import com.haulmont.cuba.testsupport.TestDataSource;

import java.util.Arrays;
import java.util.List;

public abstract class ReportsTestCase extends CubaTestCase {

    @Override
    protected void initDataSources() throws Exception {
        Class.forName("org.postgresql.Driver");
        TestDataSource ds = new TestDataSource("jdbc:postgresql://localhost/reports_test", "root", "root");
        TestContext.getInstance().bind("java:comp/env/jdbc/CubaDS", ds);
    }

    @Override
    protected List<String> getTestAppProperties() {
        String[] files = {
                "com/haulmont/cuba/app.properties",
                "com/haulmont/reports/test-app.properties",
                "test-app.properties",
        };
        return Arrays.asList(files);
    }
}
