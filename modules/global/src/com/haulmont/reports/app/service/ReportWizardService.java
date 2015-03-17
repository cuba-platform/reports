/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.app.service;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.View;
import com.haulmont.reports.app.EntityTree;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.reports.entity.wizard.EntityTreeNode;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.ReportRegion;
import com.haulmont.reports.entity.wizard.TemplateFileType;
import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.yarg.reporting.ReportOutputDocument;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $id$
 */
public interface ReportWizardService {
    String NAME = "report_ReportWizardService";

    Report toReport(ReportData reportData, boolean temporary);

    View createViewByReportRegions(EntityTreeNode entityTreeRootNode, List<ReportRegion> reportRegions);

    ReportRegion createReportRegionByView(EntityTree entityTree, boolean isTabulated, @Nullable View view, @Nullable String collectionPropertyName);

    boolean isEntityAllowedForReportWizard(MetaClass metaClass);

    boolean isPropertyAllowedForReportWizard(MetaClass metaClass, MetaProperty metaProperty);

    byte[] generateTemplate(ReportData reportData, TemplateFileType templateFileType) throws TemplateGenerationException;

    EntityTree buildEntityTree(MetaClass metaClass);
}
