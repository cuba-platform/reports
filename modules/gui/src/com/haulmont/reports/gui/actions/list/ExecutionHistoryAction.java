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

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.ScreenBuilders;
import com.haulmont.cuba.gui.components.ActionType;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.actions.ListAction;
import com.haulmont.cuba.gui.components.data.DataUnit;
import com.haulmont.cuba.gui.components.data.meta.EntityDataUnit;
import com.haulmont.cuba.gui.meta.StudioAction;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.gui.report.history.ReportExecutionDialog;

import javax.inject.Inject;

/**
 * Standard action for displaying the report execution history.
 * <p>
 * Should be defined in the screen that is associated with {@link Report}. Should be defined for a {@code Button}
 * or a list component ({@code Table}, {@code DataGrid}, etc.).
 */
@StudioAction(category = "Reports list actions", description = "Shows the report execution history")
@ActionType(ExecutionHistoryAction.ID)
public class ExecutionHistoryAction extends ListAction {

    public static final String ID = "executionHistory";

    protected ScreenBuilders screenBuilders;

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

    @Inject
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Override
    public void actionPerform(Component component) {
        if (target != null && target.getFrame() != null) {
            MetaClass metaClass = null;
            DataUnit items = target.getItems();
            if (items instanceof EntityDataUnit) {
                metaClass = ((EntityDataUnit) items).getEntityMetaClass();
            }

            openLookup(target.getFrame().getFrameOwner(), metaClass);
        } else if (component instanceof Component.BelongToFrame) {
            FrameOwner screen = ComponentsHelper.getWindowNN((Component.BelongToFrame) component).getFrameOwner();
            openLookup(screen, null);
        } else {
            throw new IllegalStateException("No target screen or component found for 'ExecutionHistoryAction'");
        }
    }

    protected void openLookup(FrameOwner screen, MetaClass metaClass) {
        Screen hostScreen;
        if (screen instanceof Screen) {
            hostScreen = (Screen) screen;
        } else {
            hostScreen = UiControllerUtils.getHostScreen((ScreenFragment) screen);
        }

        screenBuilders.lookup(Report.class, screen)
                .withScreenClass(ReportExecutionDialog.class)
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(ParamsMap.of(
                        ReportExecutionDialog.SCREEN_PARAMETER, hostScreen.getId(),
                        ReportExecutionDialog.META_CLASS_PARAMETER, metaClass)))
                .show();
    }
}
