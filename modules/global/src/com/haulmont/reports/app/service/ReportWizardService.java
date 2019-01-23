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
package com.haulmont.reports.app.service;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.global.View;
import com.haulmont.reports.app.EntityTree;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.wizard.EntityTreeNode;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.ReportRegion;
import com.haulmont.reports.entity.wizard.TemplateFileType;
import com.haulmont.reports.exception.TemplateGenerationException;

import javax.annotation.Nullable;
import java.util.List;

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