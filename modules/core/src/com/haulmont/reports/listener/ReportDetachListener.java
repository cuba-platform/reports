/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.listener;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.listener.BeforeDetachEntityListener;
import com.haulmont.reports.ReportingApi;
import com.haulmont.reports.entity.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author degtyarjov
 * @version $Id$
 */
@ManagedBean("report_ReportDetachListener")
public class ReportDetachListener implements BeforeDetachEntityListener<Report> {

    @Inject
    protected ReportingApi reportingApi;

    @Inject
    protected Persistence persistence;

    @Override
    public void onBeforeDetach(Report entity, EntityManager entityManager) {
        if (persistence.getTools().isLoaded(entity, "xml") && StringUtils.isNotBlank(entity.getXml())) {
            Report reportFromXml = reportingApi.convertToReport(entity.getXml());
            entity.setBands(reportFromXml.getBands());
            entity.setInputParameters(reportFromXml.getInputParameters());
            entity.setReportScreens(reportFromXml.getReportScreens());
            entity.setRoles(reportFromXml.getRoles());
            entity.setValuesFormats(reportFromXml.getValuesFormats());

            setRelevantReferencesToReport(entity);
            sortRootChildrenBands(entity);
        }
    }

    protected void sortRootChildrenBands(Report entity) {
        if (entity.getRootBandDefinition() != null
                && CollectionUtils.isNotEmpty(entity.getRootBandDefinition().getChildrenBandDefinitions())) {
            List<BandDefinition> bandDefinitions = new ArrayList<>(entity.getRootBandDefinition().getChildrenBandDefinitions());
            Collections.sort(bandDefinitions, new Comparator<BandDefinition>() {
                @Override
                public int compare(BandDefinition o1, BandDefinition o2) {
                    return o1.getPosition().compareTo(o2.getPosition());
                }
            });
            entity.getRootBandDefinition().setChildrenBandDefinitions(bandDefinitions);
        }
    }

    protected void setRelevantReferencesToReport(Report entity) {
        for (ReportValueFormat reportValueFormat : entity.getValuesFormats()) {
            reportValueFormat.setReport(entity);
        }

        for (BandDefinition bandDefinition : entity.getBands()) {
            bandDefinition.setReport(entity);
        }

        for (ReportInputParameter reportInputParameter : entity.getInputParameters()) {
            reportInputParameter.setReport(entity);
        }

        for (ReportScreen reportScreen : entity.getReportScreens()) {
            reportScreen.setReport(entity);
        }
    }
}