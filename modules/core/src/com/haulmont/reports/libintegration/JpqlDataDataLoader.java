/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.View;
import com.haulmont.reports.app.EntityMap;
import com.haulmont.yarg.exception.DataLoadingException;
import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.loaders.impl.AbstractDbDataLoader;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportQuery;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class JpqlDataDataLoader extends AbstractDbDataLoader implements ReportDataLoader {
    @Inject
    private Persistence persistence;

    private static final String QUERY_END = "%%END%%";
    private static final String ALIAS_PATTERN = "as\\s+\"?([\\w|\\d|_|\\.]+)\"?\\s*";
    private static final String OUTPUT_PARAMS_PATTERN = "(?i)" + ALIAS_PATTERN + "[,|from|" + QUERY_END + "]";

    protected List<OutputValue> parseQueryOutputParametersNames(String query) {
        ArrayList<OutputValue> result = new ArrayList<>();
        if (!query.endsWith(";"))
            query += QUERY_END;
        else
            query = query.substring(0, query.length() - 1) + QUERY_END;
        Pattern namePattern = Pattern.compile(OUTPUT_PARAMS_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = namePattern.matcher(query);

        while (matcher.find()) {
            String group = matcher.group(matcher.groupCount());
            if (group != null)
                result.add(new OutputValue(group.trim()));
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, BandData parentBand, Map<String, Object> params) {
        List<OutputValue> outputParameters = null;
        List queryResult = null;
        Transaction tx = persistence.createTransaction();
        try {
            String query = reportQuery.getScript();
            if (StringUtils.isBlank(query)) return Collections.emptyList();

            outputParameters = parseQueryOutputParametersNames(query);

            query = query.replaceAll("(?i)" + ALIAS_PATTERN + ",", ",");//replaces [as alias_name], entries except last
            query = query.replaceAll("(?i)" + ALIAS_PATTERN, " ");//replaces last [as alias_name] entry

            Query select = insertParameters(query, parentBand, params);
            queryResult = select.getResultList();
            tx.commit();
        } catch (Throwable e) {
            throw new DataLoadingException(String.format("An error occurred while loading data for data set [%s]", reportQuery.getName()), e);
        } finally {
            tx.end();
        }

        if (queryResult.size() > 0 && queryResult.get(0) instanceof Entity) {
            List<Map<String, Object>> wrappedResults = new ArrayList<>();
            for (Object theResult : queryResult) {
                wrappedResults.add(new EntityMap((Entity) theResult));
            }
            return wrappedResults;
        } else {
            return fillOutputData(queryResult, outputParameters);
        }
    }

    protected Query insertParameters(String query, BandData parentBand, Map<String, Object> params) {
        QueryPack pack = prepareQuery(query, parentBand, params);

        boolean inserted = pack.getParams().length > 0;
        EntityManager em = persistence.getEntityManager();
        Query select = em.createQuery(pack.getQuery());
        if (inserted) {
            //insert parameters to their position
            for (QueryParameter queryParameter : pack.getParams()) {
                Object value = queryParameter.getValue();
                select.setParameter(queryParameter.getPosition(), convertParameter(value));
            }
        }
        return select;
    }

    @Override
    protected String insertParameterToQuery(String query, QueryParameter parameter) {
        query = query.replaceAll(parameter.getParamRegexp(), "?" + parameter.getPosition());
        return query;
    }
}
