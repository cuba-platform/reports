/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.yarg.loaders.impl.SqlDataLoader;
import com.haulmont.yarg.structure.ReportQuery;
import com.haulmont.yarg.util.db.QueryRunner;
import com.haulmont.yarg.util.db.ResultSetHandler;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class CubaSqlDataLoader extends SqlDataLoader {

    public CubaSqlDataLoader(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected List runQuery(ReportQuery reportQuery, String queryString, Object[] params, ResultSetHandler<List> handler) throws SQLException {
        Persistence persistence = AppBeans.get(Persistence.class);
        QueryRunner runner = new QueryRunner(persistence.getDataSource(StoreUtils.getStoreName(reportQuery)));
        return runner.query(queryString, params, handler);
    }
}
