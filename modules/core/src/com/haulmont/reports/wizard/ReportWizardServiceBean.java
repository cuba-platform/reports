/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.wizard;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.app.service.ReportWizardService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.TemplateFileType;
import com.haulmont.reports.exception.TemplateGenerationException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author degtyarjov
 * @version $Id$
 */
@Service(ReportWizardService.NAME)
public class ReportWizardServiceBean implements ReportWizardService {
    @Inject
    private ReportingWizardApi reportingWizardApi;

    @Override
    public Report toReport(ReportData reportData, byte[] templateByteArray, boolean isTmp) {
        return reportingWizardApi.toReport(reportData, templateByteArray, isTmp);
    }

    @Override
    public boolean isEntityAllowedForReportWizard(MetaClass metaClass) {
        return reportingWizardApi.isEntityAllowedForReportWizard(metaClass);
    }

    @Override
    public boolean isPropertyAllowedForReportWizard(MetaProperty metaProperty) {
        return reportingWizardApi.isPropertyAllowedForReportWizard(metaProperty);
    }

    @Override
    public byte[] generateTemplate(ReportData reportData, TemplateFileType templateFileType) throws TemplateGenerationException {
        return reportingWizardApi.generateTemplate(reportData, templateFileType);
    }
}