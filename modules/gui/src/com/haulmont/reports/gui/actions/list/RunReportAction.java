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

package com.haulmont.reports.gui.actions.list;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.BeanLocator;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.Screens;
import com.haulmont.cuba.gui.actions.list.SecuredListAction;
import com.haulmont.cuba.gui.components.ActionType;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.components.data.meta.ContainerDataUnit;
import com.haulmont.cuba.gui.components.data.meta.EntityDataUnit;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.icons.CubaIcon;
import com.haulmont.cuba.gui.icons.Icons;
import com.haulmont.cuba.gui.meta.StudioAction;
import com.haulmont.cuba.gui.screen.MapScreenOptions;
import com.haulmont.cuba.gui.screen.OpenMode;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.gui.ReportGuiManager;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Standard action for displaying the list of all available reports.
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.) connected to a data container,
 * containing {@link Report} type elements.
 */
@StudioAction(category = "Reports list actions", description = "Shows the list of all available reports")
@ActionType(RunReportAction.ID)
public class RunReportAction extends SecuredListAction {

    public static final String ID = "runReport";

    protected BeanLocator beanLocator;

    public RunReportAction() {
        this(ID);
    }

    public RunReportAction(String id) {
        super(id);
    }

    @Inject
    public void setIcons(Icons icons) {
        this.icon = icons.get(CubaIcon.ANGLE_DOUBLE_RIGHT);
    }

    @Inject
    public void setMessages(Messages messages) {
        this.caption = messages.getMessage(RunReportAction.class, "actions.RunReport");
    }

    @Inject
    protected void setBeanLocator(BeanLocator beanLocator) {
        this.beanLocator = beanLocator;
    }

    @Override
    public void actionPerform(Component component) {
        MetaClass entityMetaClass;
        if (target.getItems() instanceof EntityDataUnit) {
            entityMetaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        } else {
            throw new UnsupportedOperationException("Unsupported data unit " + target.getItems());
        }

        if (!entityMetaClass.getJavaClass().equals(Report.class)) {
            throw new UnsupportedOperationException("Unsupported meta class " + entityMetaClass + " for runReport action");
        }

        Report report = (Report) target.getSingleSelected();
        if (target.getItems() instanceof ContainerDataUnit) {
            DataManager dataManager = beanLocator.get(DataManager.NAME);
            report = dataManager.reload(report, "report.edit");
        } else {
            DataSupplier dataSupplier = target.getDatasource().getDataSupplier();
            report = dataSupplier.reload(report, "report.edit");
        }

        ReportGuiManager reportGuiManager = beanLocator.get(ReportGuiManager.class);
        List<ReportInputParameter> inputParameters = report.getInputParameters();
        if (inputParameters != null
                && inputParameters.size() > 0
                || reportGuiManager.inputParametersRequiredByTemplates(report)) {
            Map<String, Object> params = new HashMap<>();
            params.put("report", report);

            Screens screens = ComponentsHelper.getScreenContext(target).getScreens();
            screens.create("report$inputParameters", OpenMode.DIALOG, new MapScreenOptions(params))
                    .show();
        } else {
            Window window = ComponentsHelper.getWindowNN(target);
            reportGuiManager.printReport(report, Collections.emptyMap(), window.getFrameOwner());
        }
    }
}
