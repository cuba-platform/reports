/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.gui.report.run;

import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.gui.data.impl.GroupDatasourceImpl;
import com.haulmont.reports.entity.Report;

import java.util.Map;
import java.util.UUID;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class RunReportDatasource extends GroupDatasourceImpl<Report, UUID> {
    @Override
    protected void loadData(Map<String, Object> params) {
        //do nothing
    }
}