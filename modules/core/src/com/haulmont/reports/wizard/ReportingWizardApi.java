/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import javax.annotation.Nullable;
import java.util.List;

/**
 * API for report wizard
 *
 */
public interface ReportingWizardApi {
    String NAME = "report_ReportingWizardApi";

    Report toReport(ReportData reportData, boolean temporary);

    View createViewByReportRegions(EntityTreeNode entityTreeRootNode, List<ReportRegion> reportRegions);

    ReportRegion createReportRegionByView(EntityTree entityTree, boolean isTabulated, @Nullable View view, @Nullable String collectionPropertyName);

    boolean isEntityAllowedForReportWizard(MetaClass metaClass);

    boolean isPropertyAllowedForReportWizard(MetaClass metaClass, MetaProperty metaProperty);
}
