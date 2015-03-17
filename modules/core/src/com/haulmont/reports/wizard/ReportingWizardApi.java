/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.wizard;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.global.View;
import com.haulmont.reports.app.EntityTree;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.wizard.EntityTreeNode;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.ReportRegion;
import com.haulmont.reports.entity.wizard.TemplateFileType;

import javax.annotation.Nullable;
import java.util.List;

/**
 * API for report wizard
 *
 * @author fedorchenko
 * @version $Id$
 */
public interface ReportingWizardApi {
    String NAME = "report_ReportingWizardApi";

    Report toReport(ReportData reportData, boolean temporary);

    View createViewByReportRegions(EntityTreeNode entityTreeRootNode, List<ReportRegion> reportRegions);

    ReportRegion createReportRegionByView(EntityTree entityTree, boolean isTabulated, @Nullable View view, @Nullable String collectionPropertyName);

    boolean isEntityAllowedForReportWizard(MetaClass metaClass);

    boolean isPropertyAllowedForReportWizard(MetaClass metaClass, MetaProperty metaProperty);
}
