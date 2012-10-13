/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports.core;

import com.haulmont.cuba.core.CubaTestCase;
import com.haulmont.cuba.testsupport.TestContext;
import com.haulmont.cuba.testsupport.TestDataSource;

import java.util.Arrays;
import java.util.List;

/**
 * @author artamonov
 * @version $Id$
 */
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
                "cuba-app.properties",
                "reports-test-app.properties",
                "test-app.properties",
        };
        return Arrays.asList(files);
    }
}
