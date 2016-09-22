/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.global.Stores;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.yarg.structure.ReportQuery;

import java.util.Map;

public class StoreUtils {

    private StoreUtils() {
    }

    public static String getStoreName(ReportQuery reportQuery) {
        String storeName = Stores.MAIN;
        Map<String, Object> params = reportQuery.getAdditionalParams();
        if (params != null && params.get(DataSet.DATA_STORE_PARAM_NAME) != null) {
            storeName = (String) params.get(DataSet.DATA_STORE_PARAM_NAME);
        }
        return storeName;
    }

}
