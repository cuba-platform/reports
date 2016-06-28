/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.testsupport;

import com.haulmont.cuba.testsupport.TestContainer;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Konstantin Krivopustov
 */
public class ReportsTestContainer extends TestContainer {

    public ReportsTestContainer() {
        super();
        appComponents = new ArrayList<>(Arrays.asList(
                "com.haulmont.cuba"
        ));
        appPropertiesFiles = new ArrayList<>(Arrays.asList(
                "cuba-app.properties",
                "test-app.properties",
                "reports-test-app.properties"
        ));
        dbDriver = "org.postgresql.Driver";
        dbUrl = "jdbc:postgresql://localhost/reports_test";
        dbUser = "root";
        dbPassword = "root";
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
            }
            setupContext();
        }

        @Override
        public void after() {
            cleanupContext();
            // never stops - do not call super
        }
    }
}
