/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.listener;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.listener.BeforeDetachEntityListener;
import com.haulmont.reports.ReportingApi;
import com.haulmont.reports.entity.Report;
import org.apache.commons.lang.StringUtils;

import javax.annotation.ManagedBean;
import javax.inject.Inject;

/**
 * @author degtyarjov
 * @version $Id$
 */
@ManagedBean("report_ReportDetachListener")
public class ReportDetachListener implements BeforeDetachEntityListener<Report> {

    @Inject
    protected ReportingApi reportingApi;

    @Override
    public void onBeforeDetach(Report entity, EntityManager entityManager) {
        if (StringUtils.isNotBlank(entity.getXml())) {
            Report reportFromXml = reportingApi.convertToReport(entity.getXml());
            entity.setBands(reportFromXml.getBands());
            entity.setInputParameters(reportFromXml.getInputParameters());
            entity.setReportScreens(reportFromXml.getReportScreens());
            entity.setRoles(reportFromXml.getRoles());
            entity.setValuesFormats(reportFromXml.getValuesFormats());
        }
    }
}