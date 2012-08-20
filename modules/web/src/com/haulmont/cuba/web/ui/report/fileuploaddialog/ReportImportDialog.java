/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.cuba.web.ui.report.fileuploaddialog;

import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.FileUploadField;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.components.Window;

import java.util.Map;

/**
 * @author fontanenko
 * @version $Id$
 */
public class ReportImportDialog extends AbstractWindow {

    private static final long serialVersionUID = -8624761668385369711L;

    private byte[] bytes;

    public ReportImportDialog(IFrame frame) {
        super(frame);
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        final FileUploadField fileUploadField = getComponent("fileUpload");
        fileUploadField.addListener(new FileUploadField.ListenerAdapter() {
            @Override
            public void uploadSucceeded(Event event) {
                bytes = fileUploadField.getBytes();
                close(Window.COMMIT_ACTION_ID);
            }
        });
    }
}
