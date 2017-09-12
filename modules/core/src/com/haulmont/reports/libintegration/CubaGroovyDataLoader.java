/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Resources;
import com.haulmont.yarg.exception.DataLoadingException;
import com.haulmont.yarg.exception.ValidationException;
import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportQuery;
import com.haulmont.yarg.util.groovy.Scripting;
import groovy.lang.Closure;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class CubaGroovyDataLoader implements ReportDataLoader {
    protected Scripting scripting;

    @Inject
    protected Resources resources;

    @Inject
    protected GroovyScriptParametersProvider groovyScriptParametersProvider;

    @Inject
    public CubaGroovyDataLoader(Scripting scripting) {
        this.scripting = scripting;
    }

    @Override
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, BandData parentBand, Map<String, Object> params) {
        try {
            String script = reportQuery.getScript();
            Map<String, Object> scriptParams = groovyScriptParametersProvider.prepareParameters(reportQuery, parentBand, params);

            script = StringUtils.trim(script);
            if (script.endsWith(".groovy")) {
                script = resources.getResourceAsString(script);
            }
            return scripting.evaluateGroovy(script, scriptParams);
        } catch (ValidationException e) {
            throw e;
        } catch (Throwable e) {
            throw new DataLoadingException(String.format("An error occurred while loading data for data set [%s]", reportQuery.getName()), e);
        }
    }

    protected void validationException(String message) {
        throw new ValidationException(message);
    }

    protected void transactional(Closure closure) {
        Persistence persistence = AppBeans.get(Persistence.class);
        Transaction tx = persistence.getTransaction();
        try {
            closure.call(persistence.getEntityManager());
            tx.commit();
        } finally {
            tx.end();
        }
    }
}