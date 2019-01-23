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
            throw new RuntimeException("ReportsTestContainer not initialized", throwable);
        }
    }
}
