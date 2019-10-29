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
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.Screens;
import com.haulmont.cuba.gui.components.ActionType;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.actions.ListAction;
import com.haulmont.cuba.gui.components.data.meta.EntityDataUnit;
import com.haulmont.cuba.gui.meta.StudioAction;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.gui.report.history.ReportExecutionBrowser;

import javax.inject.Inject;

/**
 * Standard action for displaying the report execution history.
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.) connected to a data container,
 * containing {@link Report} type elements.
 */
@StudioAction(category = "Reports list actions", description = "Shows the report execution history")
@ActionType(ExecutionHistoryAction.ID)
public class ExecutionHistoryAction extends ListAction {

    public static final String ID = "executionHistory";

    public ExecutionHistoryAction() {
        this(ID);
    }

    public ExecutionHistoryAction(String id) {
        super(id);
    }

    @Inject
    public void setMessages(Messages messages) {
        this.caption = messages.getMessage(RunReportAction.class, "actions.ExecutionHistory");
    }

    @Override
    protected boolean isApplicable() {
        return target != null
                && target.getSelected().size() <= 1;
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
            throw new UnsupportedOperationException("Unsupported meta class " + entityMetaClass + " for executionHistory action");
        }

        Screens screens = ComponentsHelper.getScreenContext(target).getScreens();
        Report selectedReport = (Report) target.getSingleSelected();
        screens.create(ReportExecutionBrowser.class)
                .setFilterByReport(selectedReport)
                .show();
    }
}
