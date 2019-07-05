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
package com.haulmont.reports.gui.valueformat.edit;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.gui.ScreenBuilders;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import com.haulmont.cuba.gui.screen.MapScreenOptions;
import com.haulmont.cuba.gui.screen.OpenMode;
import com.haulmont.cuba.gui.screen.StandardCloseAction;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.reports.entity.ReportValueFormat;
import com.haulmont.reports.gui.definition.edit.scripteditordialog.ScriptEditorDialog;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ValueFormatEditor extends AbstractEditor<ReportValueFormat> {

    public static final String RETURN_VALUE = "return value";

    protected String[] defaultFormats = new String[]{
            "#,##0",
            "##,##0",
            "#,##0.###",
            "#,##0.##",
            "dd/MM/yyyy HH:mm",
            "${image:WxH}",
            "${bitmap:WxH}",
            "${imageFileId:WxH}",
            "${html}",
            "class:"
    };

    protected LookupField<String> formatField = null;

    @Inject
    protected FieldGroup formatFields;

    @Inject
    protected CheckBox groovyCheckBox;

    @Inject
    protected LinkButton groovyFullScreenLinkButton;

    @Inject
    protected VBoxLayout groovyVBox;

    @Inject
    protected SourceCodeEditor groovyCodeEditor;

    @Inject
    protected ComponentsFactory componentsFactory;

    @Inject
    protected Datasource valuesFormatsDs;

    @Inject
    protected Metadata metadata;

    @Inject
    protected Security security;

    @Inject
    protected ScreenBuilders screenBuilders;

    protected SourceCodeEditor.Mode groovyScriptFieldMode = SourceCodeEditor.Mode.Groovy;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        getDialogOptions().setWidthAuto();

        // Add default format strings to combobox
        formatFields.addCustomField("formatString", new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                formatField = componentsFactory.createComponent(LookupField.class);
                Map<String, String> options = new HashMap<>();
                for (String format : defaultFormats) {
                    options.put(format, format);
                }

                formatField.setDatasource(datasource, propertyId);
                formatField.setOptionsMap(options);
                formatField.setNewOptionAllowed(true);
                formatField.setNewOptionHandler(caption -> {
                    addFormatItem(caption);
                    formatField.setValue(caption);
                });
                formatField.setEditable(security.isEntityOpPermitted(ReportValueFormat.class, EntityOp.UPDATE));
                return formatField;
            }
        });

        groovyCheckBox.addValueChangeListener(booleanValueChangeEvent -> {
            Boolean visible = booleanValueChangeEvent.getValue();
            Boolean prevVisible = booleanValueChangeEvent.getPrevValue();

            Boolean userOriginated = booleanValueChangeEvent.isUserOriginated();

            if (isClickTrueGroovyScript(visible, prevVisible, userOriginated)) {
                groovyCodeEditor.setValue(RETURN_VALUE);
            }
            if (Boolean.FALSE.equals(visible)) {
                formatField.clear();
            }

            groovyVBox.setVisible(Boolean.TRUE.equals(visible));
            formatField.setVisible(Boolean.FALSE.equals(visible));
        });

        //noinspection unchecked
        valuesFormatsDs.addItemPropertyChangeListener(e ->
                ((DatasourceImplementation) valuesFormatsDs).modified(e.getItem()));
    }

    protected boolean isClickTrueGroovyScript(Boolean visible, Boolean prevVisible, Boolean userOriginated) {
        return Boolean.TRUE.equals(userOriginated) && Boolean.TRUE.equals(visible) && Boolean.FALSE.equals(prevVisible);
    }

    protected void addFormatItem(String caption) {
        //noinspection unchecked
        Map<String, String> optionsMap =
                new HashMap<>((Map<? extends String, ? extends String>) formatField.getOptionsMap());
        optionsMap.put(caption, caption);

        formatField.setOptionsMap(optionsMap);
    }

    @Override
    protected void postInit() {
        String value = formatField.getValue();
        if (value != null) {
            if (!formatField.getOptionsMap().containsValue(value) && Boolean.FALSE.equals(groovyCheckBox.isChecked())) {
                addFormatItem(value);
            }
            formatField.setValue(value);
        }
    }

    @Override
    public void setItem(Entity item) {
        Entity newItem = valuesFormatsDs.getDataSupplier().newInstance(valuesFormatsDs.getMetaClass());
        metadata.getTools().copy(item, newItem);
        ((ReportValueFormat) newItem).setId((UUID) item.getId());
        super.setItem(newItem);
    }

    public void getGroovyHelp() {
        showMessageDialog(getMessage("valuesFormats.groovyScript"), getMessage("valuesFormats.groovyScriptHelpText"),
                MessageType.CONFIRMATION_HTML
                        .modal(false)
                        .width(700f));
    }

    public void showGroovyEditorDialog() {
        ScriptEditorDialog editorDialog = (ScriptEditorDialog) screenBuilders.screen(this)
                .withScreenId("scriptEditorDialog")
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(ParamsMap.of(
                        "mode", groovyScriptFieldMode,
                        "suggester", groovyCodeEditor.getSuggester(),
                        "scriptValue", groovyCodeEditor.getValue(),
                        "helpVisible", groovyCodeEditor.isVisible(),
                        "helpMsgKey", "valuesFormats.groovyScriptHelpText"
                )))
                .build();
        editorDialog.addAfterCloseListener(actionId -> {
            StandardCloseAction closeAction = (StandardCloseAction) actionId.getCloseAction();
            if (COMMIT_ACTION_ID.equals(closeAction.getActionId())) {
                groovyCodeEditor.setValue(editorDialog.getValue());
            }
        });
        editorDialog.show();
    }

}