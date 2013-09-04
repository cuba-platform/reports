/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: degtyarjov
 * Created: 04.09.13 13:02
 *
 * $Id$
 */
package com.haulmont.reports.listener;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.listener.BeforeDetachEntityListener;
import com.haulmont.reports.ReportingApi;
import com.haulmont.reports.entity.Report;
import org.apache.commons.lang.StringUtils;

public class ReportDetachListener implements BeforeDetachEntityListener<Report> {
    @Override
    public void onBeforeDetach(Report entity, EntityManager entityManager) {
        if (StringUtils.isNotBlank(entity.getXml())) {
            ReportingApi reportingApi = AppBeans.get(ReportingApi.NAME);
            Report reportFromXml = reportingApi.convertToReport(entity.getXml());
            entity.setBands(reportFromXml.getBands());
            entity.setInputParameters(reportFromXml.getInputParameters());
            entity.setReportScreens(reportFromXml.getReportScreens());
            entity.setRoles(reportFromXml.getRoles());
            entity.setValuesFormats(reportFromXml.getValuesFormats());
        }
    }
}
