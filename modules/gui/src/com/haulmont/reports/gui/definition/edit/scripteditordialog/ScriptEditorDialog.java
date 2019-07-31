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

package com.haulmont.reports.gui.definition.edit.scripteditordialog;

import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.autocomplete.Suggester;
import com.haulmont.cuba.security.app.UserSettingService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.Map;
import java.util.function.Consumer;

public class ScriptEditorDialog extends AbstractWindow {

    public static final String SIZE_SCRIPT_EDITOR_DIALOG = "sizeScriptEditorDialog";
    public static final String FULL = "full";
    public static final String DIALOG = "dialog";

    @WindowParam
    protected SourceCodeEditor.Mode mode;

    @WindowParam
    protected Suggester suggester;

    @WindowParam
    protected String scriptValue;

    @WindowParam
    protected Consumer<HasContextHelp.ContextHelpIconClickEvent> helpHandler;

    @Inject
    protected SourceCodeEditor editor;

    @Inject
    protected UserSettingService userSettingService;

    @Override
    public void init(Map<String, Object> params) {
        initEditor();
        initActions();
        loadParameterWindow();

        Object caption = params.get("caption");
        if (ObjectUtils.isNotEmpty(caption)) {
            setCaption(caption.toString());
        }

        addAfterCloseListener(afterCloseEvent -> saveParameterWindow());
    }

    private void initEditor() {
        editor.setMode(mode != null ? mode : SourceCodeEditor.Mode.Text);
        editor.setSuggester(suggester);
        editor.setValue(scriptValue);
        editor.setHandleTabKey(true);
        editor.setContextHelpIconClickHandler(helpHandler);
    }

    private void initActions() {
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

    private void loadParameterWindow() {
        String size = userSettingService.loadSetting(SIZE_SCRIPT_EDITOR_DIALOG);
        if (StringUtils.isNotEmpty(size)) {
            getDialogOptions().setMaximized(size.equals(FULL));
        }
    }

    private void saveParameterWindow() {
        if (getDialogOptions().getMaximized()) {
            userSettingService.saveSetting(SIZE_SCRIPT_EDITOR_DIALOG, FULL);
        } else {
            userSettingService.saveSetting(SIZE_SCRIPT_EDITOR_DIALOG, DIALOG);
        }
    }


    public String getValue() {
        return editor.getValue();
    }
}
