/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.report.run;

import com.google.common.collect.ImmutableMap;
import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.validators.DoubleValidator;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.reports.entity.ParameterType;
import com.haulmont.reports.entity.ReportInputParameter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

import static com.haulmont.reports.gui.report.run.CommonLookupController.CLASS_PARAMETER;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class ParameterFieldCreator {
    protected ComponentsFactory componentsFactory = AppConfig.getFactory();
    protected Messages messages = AppBeans.get(Messages.class);
    protected Metadata metadata = AppBeans.get(Metadata.class);
    protected AbstractWindow window;

    protected Map<ParameterType, FieldCreator> fieldCreationMapping = new ImmutableMap.Builder<ParameterType, FieldCreator>()
            .put(ParameterType.BOOLEAN, new CheckBoxCreator())
            .put(ParameterType.DATE, new DateFieldCreator())
            .put(ParameterType.ENTITY, new SingleFieldCreator())
            .put(ParameterType.ENUMERATION, new EnumFieldCreator())
            .put(ParameterType.TEXT, new TextFieldCreator())
            .put(ParameterType.NUMERIC, new NumericFieldCreator())
            .put(ParameterType.ENTITY_LIST, new MultiFieldCreator())
            .put(ParameterType.DATETIME, new DateTimeFieldCreator())
            .put(ParameterType.TIME, new TimeFieldCreator())
            .build();

    public ParameterFieldCreator(AbstractWindow window) {
        this.window = window;
    }

    public Label createLabel(ReportInputParameter parameter, Field field) {
        Label label = componentsFactory.createComponent(Label.NAME);
        label.setAlignment(field instanceof TokenList ? Component.Alignment.TOP_LEFT : Component.Alignment.MIDDLE_LEFT);
        label.setWidth(Component.AUTO_SIZE);
        label.setValue(parameter.getLocName());
        return label;
    }

    public Field createField(ReportInputParameter parameter) {
        Field field = fieldCreationMapping.get(parameter.getType()).createField(parameter);
        field.setRequiredMessage(messages.formatMessage(this.getClass(), "error.paramIsRequiredButEmpty", parameter.getLocName()));

        field.setId("param_" + parameter.getAlias());
        field.setWidth("100%");
        field.setFrame(window);
        field.setEditable(true);

        field.setRequired(parameter.getRequired());
        return field;
    }

    protected interface FieldCreator {
        Field createField(ReportInputParameter parameter);
    }

    protected class DateFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            DateField dateField = componentsFactory.createComponent(DateField.NAME);
            dateField.setResolution(DateField.Resolution.DAY);
            dateField.setDateFormat(messages.getMessage(AppConfig.getMessagesPack(), "dateFormat"));
            return dateField;
        }
    }

    protected class DateTimeFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            DateField dateField = componentsFactory.createComponent(DateField.NAME);
            dateField.setResolution(DateField.Resolution.MIN);
            dateField.setDateFormat(messages.getMessage(AppConfig.getMessagesPack(), "dateTimeFormat"));
            return dateField;
        }
    }

    protected class TimeFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            return componentsFactory.createComponent(TimeField.NAME);
        }
    }

    protected class CheckBoxCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            return componentsFactory.createComponent(CheckBox.NAME);
        }
    }

    protected class TextFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            return componentsFactory.createComponent(TextField.NAME);
        }
    }

    protected class NumericFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            TextField textField = componentsFactory.createComponent(TextField.NAME);
            textField.addValidator(new DoubleValidator());
            textField.setDatatype(Datatypes.getNN(Double.class));
            return textField;
        }
    }

    protected class EnumFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            LookupField lookupField = componentsFactory.createComponent(LookupField.NAME);
            String enumClassName = parameter.getEnumerationClass();
            if (StringUtils.isNotBlank(enumClassName)) {
                Class enumClass = AppBeans.get(Scripting.class).loadClass(enumClassName);

                if (enumClass != null) {
                    Object[] constants = enumClass.getEnumConstants();
                    List<Object> optionsList = new ArrayList<>();
                    Collections.addAll(optionsList, constants);

                    lookupField.setOptionsList(optionsList);
                    lookupField.setCaptionMode(CaptionMode.ITEM);
                }
            }
            return lookupField;
        }
    }

    protected class SingleFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            PickerField pickerField = componentsFactory.createComponent(PickerField.NAME);
            final com.haulmont.chile.core.model.MetaClass entityMetaClass =
                    metadata.getSession().getClass(parameter.getEntityMetaClass());
            pickerField.setMetaClass(entityMetaClass);

            PickerField.LookupAction pickerLookupAction = new PickerField.LookupAction(pickerField) {
                @Override
                public void actionPerform(Component component) {
                    window.getDialogParams().setHeight(400);
                    window.getDialogParams().setResizable(true);
                    super.actionPerform(component);
                }
            };
            pickerLookupAction.setLookupScreenOpenType(WindowManager.OpenType.DIALOG);
            pickerField.addAction(pickerLookupAction);

            String parameterScreen = parameter.getScreen();

            if (StringUtils.isNotEmpty(parameterScreen)) {
                pickerLookupAction.setLookupScreen(parameterScreen);
                pickerLookupAction.setLookupScreenParams(Collections.<String, Object>emptyMap());
            } else {
                pickerLookupAction.setLookupScreen("report$commonLookup");
                Map<String, Object> params = new HashMap<>();
                params.put(CLASS_PARAMETER, entityMetaClass);

                pickerLookupAction.setLookupScreenParams(params);
            }

            return pickerField;
        }
    }

    protected class MultiFieldCreator implements FieldCreator {

        @Override
        public Field createField(final ReportInputParameter parameter) {
            TokenList tokenList = componentsFactory.createComponent(TokenList.NAME);
            final com.haulmont.chile.core.model.MetaClass entityMetaClass =
                    metadata.getSession().getClass(parameter.getEntityMetaClass());

            DsBuilder builder = new DsBuilder(window.getDsContext());
            CollectionDatasource cds = builder
                    .setRefreshMode(CollectionDatasource.RefreshMode.NEVER)
                    .setId("entities_" + parameter.getAlias())
                    .setMetaClass(entityMetaClass)
                    .setViewName(View.LOCAL)
                    .setAllowCommit(false)
                    .buildCollectionDatasource();

            cds.refresh();

            tokenList.setDatasource(cds);
            tokenList.setEditable(true);
            tokenList.setLookup(true);
            tokenList.setLookupOpenMode(WindowManager.OpenType.DIALOG);
            tokenList.setHeight("120px");

            String screen = parameter.getScreen();

            if (StringUtils.isNotEmpty(screen)) {
                tokenList.setLookupScreen(screen);
                tokenList.setLookupScreenParams(Collections.<String, Object>emptyMap());
            } else {
                tokenList.setLookupScreen("report$commonLookup");
                Map<String, Object> params = new HashMap<>();
                params.put(CLASS_PARAMETER, entityMetaClass);
                tokenList.setLookupScreenParams(params);
            }

            tokenList.setAddButtonCaption(messages.getMessage(TokenList.class, "actions.Select"));
            tokenList.setInline(true);
            tokenList.setSimple(true);

            if (Boolean.TRUE.equals(parameter.getRequired())) {
                tokenList.addValidator(new Field.Validator() {
                    @Override
                    public void validate(Object value) throws ValidationException {
                        if (value instanceof Collection && CollectionUtils.isEmpty((Collection) value)) {
                            throw new ValidationException(
                                    messages.formatMessage(this.getClass(), "error.paramIsRequiredButEmpty", parameter.getLocName()));
                        }
                    }
                });
            }

            return tokenList;
        }
    }
}
