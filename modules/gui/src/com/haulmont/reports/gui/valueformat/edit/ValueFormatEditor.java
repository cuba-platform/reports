/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.gui.valueformat.edit;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.FieldGroup;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.data.Datasource;

import java.util.HashMap;
import java.util.Map;

/**
 * @author artamonov
 * @version $Id$
 */
public class ValueFormatEditor extends AbstractEditor {
    private static String[] defaultFormats = new String[]{
            "#,##0",
            "##,##0",
            "#,##0.###",
            "#,##0.##",
            "dd/MM/yyyy HH:mm",
            "${image:WxH}",
            "${bitmap:WxH}",
            "${imageFileId:WxH}",
            "${html}"
    };

    LookupField formatField = null;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        final FieldGroup fields = getComponent("formatFields");

        // Add default format strings to combobox
        FieldGroup.Field f = fields.getField("formatString");
        fields.addCustomField(f, new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                formatField = AppConfig.getFactory().createComponent(LookupField.NAME);
                Map<String, Object> options = new HashMap<>();
                for (String format : defaultFormats) {
                    options.put(format, format);
                }

                formatField.setOptionsMap(options);
                formatField.setNewOptionAllowed(true);
                formatField.setNewOptionHandler(new LookupField.NewOptionHandler() {
                    @Override
                    public void addNewOption(String caption) {
                        addFormatItem(caption);
                        formatField.setValue(caption);
                    }
                });
                return formatField;
            }
        });

        getDialogParams().setWidth(450);
    }

    private void addFormatItem(String caption) {
        Map<String, Object> optionsMap = formatField.getOptionsMap();
        optionsMap.put(caption, caption);
        formatField.setOptionsMap(optionsMap);
    }


    @Override
    public void setItem(Entity item) {
        super.setItem(item);
        Object value = formatField.getValue();
        if (value != null) {
            if (!formatField.getOptionsMap().containsValue(value))
                addFormatItem((String) value);
            formatField.setValue(value);
        }
    }
}