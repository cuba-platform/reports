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

import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.TestContextBootstrapper;
import org.springframework.test.context.support.AbstractTestContextBootstrapper;

/**
 * Spring context bootstrapper for {@link  org.springframework.test.context.junit4.SpringJUnit4ClassRunner}
 * <p>ex:</p> {@code
 * @literal @RunWith(SpringJUnit4ClassRunner.class)
 * @literal @BootstrapWith(ReportsContextBootstrapper.class)
 * public class ClassTest {}
 * }
 * @see org.junit.runner.RunWith
 * @see org.springframework.test.context.BootstrapWith
 * @see ReportsContextLoader
 */
public class ReportsContextBootstrapper extends AbstractTestContextBootstrapper implements TestContextBootstrapper {
    @Override
    protected Class<? extends ContextLoader> getDefaultContextLoaderClass(Class<?> testClass) {
        return ReportsContextLoader.class;
    }
}
