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
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.View;
import com.haulmont.reports.app.EntityTree;
import com.haulmont.reports.app.service.ReportWizardService;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.wizard.EntityTreeNode;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.ReportRegion;
import com.haulmont.reports.entity.wizard.TemplateFileType;
import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.reports.wizard.template.TemplateGeneratorApi;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;

@Service(ReportWizardService.NAME)
public class ReportWizardServiceBean implements ReportWizardService {
    @Inject
    private ReportingWizardApi reportingWizardApi;
    @Inject
    private Provider<EntityTreeModelBuilderApi> entityTreeModelBuilderApiProvider;

    @Override
    public Report toReport(ReportData reportData, boolean temporary) {
        return reportingWizardApi.toReport(reportData, temporary);
    }

    @Override
    public View createViewByReportRegions(EntityTreeNode entityTreeRootNode, List<ReportRegion> reportRegions) {
        return reportingWizardApi.createViewByReportRegions(entityTreeRootNode, reportRegions);
    }

    @Override
    public ReportRegion createReportRegionByView(EntityTree entityTree, boolean isTabulated, @Nullable View view, @Nullable String collectionPropertyName) {
        return reportingWizardApi.createReportRegionByView(entityTree, isTabulated, view, collectionPropertyName);
    }

    @Override
    public boolean isEntityAllowedForReportWizard(MetaClass metaClass) {
        return reportingWizardApi.isEntityAllowedForReportWizard(metaClass);
    }

    @Override
    public boolean isPropertyAllowedForReportWizard(MetaClass metaClass, MetaProperty metaProperty) {
        return reportingWizardApi.isPropertyAllowedForReportWizard(metaClass, metaProperty);
    }

    @Override
    public byte[] generateTemplate(ReportData reportData, TemplateFileType templateFileType) throws TemplateGenerationException {
        TemplateGeneratorApi templateGeneratorApi = AppBeans.getPrototype(TemplateGeneratorApi.NAME, reportData, templateFileType);
        return templateGeneratorApi.generateTemplate();
    }

    @Override
    public EntityTree buildEntityTree(MetaClass metaClass) {
        return entityTreeModelBuilderApiProvider.get().buildEntityTree(metaClass);
    }
}