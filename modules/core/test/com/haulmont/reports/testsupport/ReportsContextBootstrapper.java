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
