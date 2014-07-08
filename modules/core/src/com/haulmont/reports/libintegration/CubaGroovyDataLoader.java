/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.yarg.exception.DataLoadingException;
import com.haulmont.yarg.exception.ValidationException;
import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportQuery;
import com.haulmont.yarg.util.groovy.Scripting;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.MethodClosure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CubaGroovyDataLoader implements ReportDataLoader {
    protected Scripting scripting;

    public CubaGroovyDataLoader(Scripting scripting) {
        this.scripting = scripting;
    }

    @Override
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, BandData parentBand, Map<String, Object> params) {
        try {
            String script = reportQuery.getScript();
            Map<String, Object> scriptParams = new HashMap<String, Object>();
            scriptParams.put("reportQuery", reportQuery);
            scriptParams.put("parentBand", parentBand);
            scriptParams.put("params", params);
            scriptParams.put("persistence", AppBeans.get(Persistence.class));
            scriptParams.put("metadata", AppBeans.get(Metadata.class));
            scriptParams.put("transactional", new MethodClosure(this, "transactional"));
            scriptParams.put("validationException", new MethodClosure(this, "validationException"));

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
