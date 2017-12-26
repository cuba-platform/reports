/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.testsupport;

import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextLoader;

import static com.haulmont.reports.testsupport.ReportsTestContainer.Common.INSTANCE;

/**
 * Spring context loader for {@link  org.springframework.test.context.junit4.SpringJUnit4ClassRunner}
 *
 * @see ReportsContextBootstrapper
 * @see org.junit.runner.RunWith
 * @see org.springframework.test.context.BootstrapWith
 */
public class ReportsContextLoader implements ContextLoader {
    @Override
    public String[] processLocations(Class<?> clazz, String... locations) {
        return new String[0];
    }

    @Override
    public ApplicationContext loadContext(String... locations) throws Exception {
        try {
            INSTANCE.before();
            return INSTANCE.getSpringAppContext();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable.getMessage(), throwable);
        }
    }
}
