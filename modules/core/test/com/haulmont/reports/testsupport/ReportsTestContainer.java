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

package com.haulmont.reports.testsupport;

import com.haulmont.cuba.testsupport.TestContainer;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class ReportsTestContainer extends TestContainer {

    public ReportsTestContainer() {
        super();
        appComponents = new ArrayList<>(Arrays.asList(
                "com.haulmont.cuba"
        ));
        appPropertiesFiles = new ArrayList<>(Arrays.asList(
                "com/haulmont/cuba/app.properties",
                "com/haulmont/cuba/testsupport/test-app.properties",
                "com/haulmont/reports/test-app.properties"
        ));
        dbDriver = ObjectUtils.defaultIfNull(System.getenv("dbDriver"), "org.postgresql.Driver");
        dbUrl = ObjectUtils.defaultIfNull(System.getenv("dbUrlParam"), "jdbc:postgresql://localhost/reports_test");
        dbUser = ObjectUtils.defaultIfNull(System.getenv("dbUserParam"), "root");
        dbPassword = ObjectUtils.defaultIfNull(System.getenv("dbPasswordParam"), "root");
    }

    public static class Common extends ReportsTestContainer {

        public static final ReportsTestContainer.Common INSTANCE = new ReportsTestContainer.Common();

        private static volatile boolean initialized;

        private Common() {
        }

        @Override
        public void before() throws Throwable {
            if (!initialized) {
                super.before();
                initialized = true;
                setupContext();
            }
        }

        @Override
        public void after() {
            // never stops - do not call super
        }
    }
}
