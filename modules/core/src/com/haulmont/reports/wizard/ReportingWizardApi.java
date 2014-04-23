/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.wizard;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.View;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.TemplateFileType;
import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.yarg.reporting.ReportOutputDocument;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * API for report wizard
 *
 * @author fedorchenko
 * @version $Id$
 */
public interface ReportingWizardApi {
    String NAME = "report_ReportingWizardApi";

    Report toReport(ReportData reportData, byte[] templateByteArray, boolean isTmp);

    byte[] generateTemplate(ReportData reportData, TemplateFileType templateFileType) throws TemplateGenerationException;

    boolean isEntityAllowedForReportWizard(MetaClass metaClass);

    boolean isPropertyAllowedForReportWizard(MetaClass metaClass, MetaProperty metaProperty);
}
