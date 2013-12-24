/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.actions;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.gui.ReportGuiManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author artamonov
 * @version $Id$
 */
public class RunReportAction extends AbstractAction {

    protected final IFrame window;

    protected Messages messages = AppBeans.get(Messages.class);

    protected ReportGuiManager reportGuiManager = AppBeans.get(ReportGuiManager.class);

    public RunReportAction(IFrame window, String captionId) {
        super(captionId);

        checkArgument(window != null, "Can not create RunReportAction with null window");

        this.window = window;
    }

    @Override
    public void actionPerform(Component component) {
        final Map<String, Object> params = new HashMap<>();
        params.put("screen", window.getId());

        window.openLookup("report$Report.run", new Window.Lookup.Handler() {

            @Override
            public void handleLookup(Collection items) {
                if (items != null && items.size() > 0) {
                    Report report = (Report) items.iterator().next();
                    report = window.getDsContext().getDataSupplier().reload(report, "report.edit");
                    if (report != null) {
                        if (report.getInputParameters() != null && report.getInputParameters().size() > 0) {
                            openReportParamsDialog(report, window);
                        } else {
                            reportGuiManager.printReport(report, Collections.<String, Object>emptyMap(), window);
                        }
                    }
                }
            }
        }, WindowManager.OpenType.DIALOG, params);
    }

    @Override
    public String getCaption() {
        return messages.getMessage(window.getMessagesPack(), getId());
    }

    protected void openReportParamsDialog(Report report, IFrame window) {
        window.openWindow("report$inputParameters", WindowManager.OpenType.DIALOG,
                Collections.<String, Object>singletonMap("report", report));
    }
}