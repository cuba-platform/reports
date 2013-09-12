/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.gui.report.run;

import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.reports.entity.Report;

import java.util.Map;
import java.util.UUID;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class RunReportDatasource extends CollectionDatasourceImpl<Report, UUID> {
    protected void loadData(Map<String, Object> params) {
        //do nothing
    }
}