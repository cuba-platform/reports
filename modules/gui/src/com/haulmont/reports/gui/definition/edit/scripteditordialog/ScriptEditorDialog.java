/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.definition.edit.scripteditordialog;

import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.autocomplete.Suggester;

import javax.inject.Inject;
import java.util.Map;

public class ScriptEditorDialog extends AbstractWindow {

    @WindowParam
    protected SourceCodeEditor.Mode mode;

    @WindowParam
    protected Suggester suggester;

    @WindowParam
    protected String scriptValue;

    @WindowParam
    protected boolean helpVisible;

    @WindowParam
    protected String helpMsgKey;

    @Inject
    protected SourceCodeEditor editor;

    @Inject
    protected LinkButton textHelp;

    @Override
    public void init(Map<String, Object> params) {
        editor.setMode(mode != null ? mode : SourceCodeEditor.Mode.Text);
        editor.setSuggester(suggester);
        editor.setValue(scriptValue);
        editor.setHandleTabKey(true);
        textHelp.setVisible(helpVisible);

        addAction(new AbstractAction("windowCommit") {
            @Override
            public void actionPerform(Component component) {
                close(COMMIT_ACTION_ID);
            }

            @Override
            public String getCaption() {
                return messages.getMainMessage("actions.Ok");
            }
        });
        addAction(new AbstractAction("windowClose") {
            @Override
            public void actionPerform(Component component) {
                close(CLOSE_ACTION_ID);
            }

            @Override
            public String getCaption() {
                return messages.getMainMessage("actions.Cancel");
            }
        });
    }

    public String getValue() {
        return editor.getValue();
    }

    public void getTextHelp() {
        showMessageDialog(getMessage("dataSet.text"), getMessage(helpMsgKey),
                MessageType.CONFIRMATION_HTML
                        .modal(false)
                        .width(700f));
    }
}
