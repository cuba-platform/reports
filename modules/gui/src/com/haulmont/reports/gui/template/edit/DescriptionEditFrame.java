/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.template.edit;

import com.haulmont.cuba.gui.components.AbstractFrame;
import com.haulmont.cuba.gui.components.BoxLayout;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.reports.entity.ReportOutputType;
import com.haulmont.reports.entity.ReportTemplate;

import java.util.Map;

public abstract class DescriptionEditFrame extends AbstractFrame {

    protected ReportTemplate reportTemplate;
    protected BoxLayout previewBox;

    public void init(Map<String, Object> params) {
        super.init(params);
        Window parent = (Window) getFrame();
        previewBox = (BoxLayout) parent.getComponentNN("previewBox");
    }

    public void setItem(ReportTemplate reportTemplate) {
        this.reportTemplate = reportTemplate;
    }

    public ReportTemplate getReportTemplate() {
        return reportTemplate;
    }

    public void showPreview() {
        Window parent = (Window) getFrame();
        previewBox.setVisible(true);
        previewBox.setHeight("100%");
        previewBox.setWidth("100%");
        previewBox.removeAll();
        parent.getDialogOptions()
                .setWidth("1280px")
                .setResizable(true)
                .center();
        initPreviewContent(previewBox);
    }

    public void hidePreview() {
        Window parent = (Window) getFrame();
        previewBox.setVisible(false);
        previewBox.removeAll();
        parent.getDialogOptions()
                .setWidthAuto()
                .setHeightAuto()
                .setResizable(false)
                .center();
    }


    public abstract boolean isApplicable(ReportOutputType reportOutputType);

    public abstract boolean applyChanges();

    protected abstract void initPreviewContent(BoxLayout previewBox);
}
