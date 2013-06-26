/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.entity.Entity;

import com.haulmont.reports.exception.ReportDataLoaderException;
import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.loaders.impl.AbstractDbDataLoader;
import com.haulmont.yarg.structure.ReportQuery;
import com.haulmont.yarg.structure.impl.BandData;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JpqlDataDataLoader extends AbstractDbDataLoader implements ReportDataLoader {
    @Inject
    private Persistence persistence;

    private static final String QUERY_END = "%%END%%";
    private static final String OUTPUT_PARAMS_PATTERN = "as ([\\w|\\d|_]+\\b)[\\s]*[,|from|" + QUERY_END + "]";

    protected List<String> parseQueryOutputParametersNames(String query) {
        ArrayList<String> result = new ArrayList<>();
        if (!query.endsWith(";"))
            query += QUERY_END;
        else
            query = query.substring(0, query.length() - 1) + QUERY_END;
        Pattern namePattern = Pattern.compile(OUTPUT_PARAMS_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = namePattern.matcher(query);

        while (matcher.find()) {
            String group = matcher.group(matcher.groupCount());
            if (group != null)
                result.add(group.trim());
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> loadData(ReportQuery dataSet, BandData band, Map<String, Object> params) {
        List<String> outputParameters = null;
        List queryResult = null;
        Transaction tx = persistence.createTransaction();
        try {
            String query = dataSet.getScript();
            if (StringUtils.isBlank(query)) return Collections.emptyList();

            outputParameters = parseQueryOutputParametersNames(query);

            query = query.replaceAll("(?i)as [\\w|\\d|_|\\s]+,", ",");//replaces [as alias_name] entries except last
            query = query.replaceAll("(?i)as [\\w|\\d|_]+ *", " ");//replaces last [as alias_name] entry

            Query select = insertParameters(query, band.getParentBand(), params);
            queryResult = select.getResultList();
            tx.commit();
        } catch (Exception e) {
            throw new ReportDataLoaderException(e);
        } finally {
            tx.end();
        }
        return fillOutputData(queryResult, outputParameters);
    }

    protected Query insertParameters(String query, BandData parentBand, Map<String, Object> params) {
        QueryPack pack = prepareQuery(query, parentBand, params);

        boolean inserted = pack.getParams().length > 0;
        EntityManager em = persistence.getEntityManager();
        Query select = em.createQuery(pack.getQuery());
        if (inserted) {
            //insert parameters to their position
            int i = 1;
            for (Object value : pack.getParams()) {
                select.setParameter(i++, value instanceof Entity ? ((Entity) value).getId() : value);
            }
        }
        return select;
    }

    protected QueryPack prepareQuery(String query, BandData parentBand, Map<String, Object> params) {
        Map<String, Object> currentParams = new HashMap<String, Object>();
        if (params != null) currentParams.putAll(params);

        //adds parameters from parent bands hierarchy
        while (parentBand != null) {
            addParentBandDataToParameters(parentBand, currentParams);
            parentBand = parentBand.getParentBand();
        }

        List<Object> values = new ArrayList<Object>();
        int i = 1;
        for (Map.Entry<String, Object> entry : currentParams.entrySet()) {
            //replaces ${alias} marks with ? and remembers their positions
            String alias = "${" + entry.getKey() + "}";
            String regexp = "\\$\\{" + entry.getKey() + "\\}";
            //todo: another regexp to remove parameter
            String deleteRegexp = "(?i)(and)?(or)? ?[\\w|\\d|\\.|\\_]+ ?(=|>=|<=|like) ?\\$\\{" + entry.getKey() + "\\}";

            if (entry.getValue() == null) {
                query = query.replaceAll(deleteRegexp, "");
            } else if (query.contains(alias)) {
                values.add(entry.getValue());
                query = query.replaceAll(regexp, "?" + i++);
            }
        }

        query = query.trim();
        if (query.endsWith("where")) query = query.replace("where", "");

        return new QueryPack(query, values.toArray());
    }
}
