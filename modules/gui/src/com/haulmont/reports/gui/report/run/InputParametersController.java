/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.gui.report.run;

import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.DateField.Resolution;
import com.haulmont.cuba.gui.components.validators.DoubleValidator;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.reports.gui.ReportHelper;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.reports.entity.ParameterType;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportInputParameter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class InputParametersController extends AbstractWindow {

    private interface FieldCreator {
        Field createField(ReportInputParameter parameter);
    }

    private ComponentsFactory cFactory = AppConfig.getFactory();

    private Report report;
    private Entity linkedEntity;

    private Messages messages;
    private Metadata metadata;

    @Inject
    private GridLayout parametersGrid;

    private HashMap<String, Field> parameterComponents = new HashMap<>();
    private Map<ParameterType, FieldCreator> fieldCreationMapping = new HashMap<>();

    {
        fieldCreationMapping.put(ParameterType.BOOLEAN, new CheckBoxCreator());
        fieldCreationMapping.put(ParameterType.DATE, new DateFieldCreator());
        fieldCreationMapping.put(ParameterType.ENTITY, new SingleFieldCreator());
        fieldCreationMapping.put(ParameterType.ENUMERATION, new EnumFieldCreator());
        fieldCreationMapping.put(ParameterType.TEXT, new TextFieldCreator());
        fieldCreationMapping.put(ParameterType.NUMERIC, new NumericFieldCreator());
        fieldCreationMapping.put(ParameterType.ENTITY_LIST, new MultiFieldCreator());
        fieldCreationMapping.put(ParameterType.DATETIME, new DateTimeFieldCreator());
        fieldCreationMapping.put(ParameterType.TIME, new TimeFieldCreator());
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        messages = AppBeans.get(Messages.NAME);
        metadata = AppBeans.get(Metadata.NAME);

        report = (Report) params.get("report");
        linkedEntity = (Entity) params.get("entity");

        if (report != null) {
            report = getDsContext().getDataSupplier().reload(report, "report.edit");
            if (CollectionUtils.isNotEmpty(report.getInputParameters()))
                parametersGrid.setRows(report.getInputParameters().size());

            int currentGridRow = 0;
            for (ReportInputParameter parameter : report.getInputParameters()) {
                createComponent(parameter, currentGridRow);
                currentGridRow++;
            }
        }
    }

    @SuppressWarnings("unused")
    public void printReport() {
        if (report != null) {
            try {
                validate();
                Map<String, Object> collectedParams = collectParameters(parameterComponents);
                ReportHelper.printReport(report, collectedParams);
            } catch (ValidationException e) {
                showNotification(getMessage("input.requiredParametersNotSet"), IFrame.NotificationType.WARNING);
            }
        }
    }

    private Map<String, Object> collectParameters(HashMap<String, Field> parameterComponents)
            throws com.haulmont.cuba.gui.components.ValidationException {

        Map<String, Object> parameters = new HashMap<>();
        for (String paramName : parameterComponents.keySet()) {
            Field _field = parameterComponents.get(paramName);
            _field.validate();
            Object value = _field.getValue();
            parameters.put(paramName, value);
        }
        return parameters;
    }

    //todo: reimplement this method

    private void createComponent(ReportInputParameter parameter, int currentGridRow) {
        Field field = fieldCreationMapping.get(parameter.getType()).createField(parameter);
        field.setId(parameter.getAlias());
        field.setWidth("250px");
        field.setFrame(this);
        field.setEditable(true);

        parameterComponents.put(parameter.getAlias(), field);
        field.setRequired(parameter.getRequired());

        Label label = cFactory.createComponent(Label.NAME);
        label.setAlignment(Alignment.MIDDLE_LEFT);
        label.setValue(parameter.getLocName());

        parametersGrid.add(label, 0, currentGridRow);
        parametersGrid.add(field, 1, currentGridRow);
    }

    private class DateFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            DateField dateField = cFactory.createComponent(DateField.NAME);
            dateField.setResolution(Resolution.DAY);
            dateField.setDateFormat(messages.getMessage(AppConfig.getMessagesPack(), "dateFormat"));
            return dateField;
        }
    }

    private class DateTimeFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            DateField dateField = cFactory.createComponent(DateField.NAME);
            dateField.setResolution(Resolution.MIN);
            dateField.setDateFormat(messages.getMessage(AppConfig.getMessagesPack(), "dateTimeFormat"));
            return dateField;
        }
    }

    private class TimeFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            return cFactory.createComponent(TimeField.NAME);
        }
    }

    private class CheckBoxCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            return cFactory.createComponent(CheckBox.NAME);
        }
    }

    private class TextFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            return cFactory.createComponent(TextField.NAME);
        }
    }

    private class NumericFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            TextField textField = cFactory.createComponent(TextField.NAME);
            textField.addValidator(new DoubleValidator());
            textField.setDatatype(Datatypes.getNN(Double.class));
            return textField;
        }
    }

    private class EnumFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            LookupField lookupField = cFactory.createComponent(LookupField.NAME);
            String enumClassName = parameter.getEnumerationClass();
            if (StringUtils.isNotEmpty(enumClassName)) {
                Class enumClass;
                try {
                    enumClass = Class.forName(enumClassName);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

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

    private class SingleFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            PickerField pickerField = cFactory.createComponent(PickerField.NAME);
            final com.haulmont.chile.core.model.MetaClass entityMetaClass =
                    metadata.getSession().getClass(parameter.getEntityMetaClass());
            Class clazz = entityMetaClass.getJavaClass();

            pickerField.setMetaClass(entityMetaClass);

            PickerField.LookupAction pickerlookupAction = new PickerField.LookupAction(pickerField) {
                @Override
                public void actionPerform(Component component) {
                    getDialogParams().setHeight(400);
                    getDialogParams().setResizable(true);
                    super.actionPerform(component);
                }
            };
            pickerlookupAction.setLookupScreenOpenType(WindowManager.OpenType.DIALOG);
            pickerField.addAction(pickerlookupAction);

            String alias = parameter.getScreen();

            if (StringUtils.isNotEmpty(alias)) {
                pickerlookupAction.setLookupScreen(alias);
                pickerlookupAction.setLookupScreenParams(Collections.<String, Object>emptyMap());
            } else {
                pickerlookupAction.setLookupScreen("report$commonLookup");
                Map<String, Object> params = new HashMap<>();
                params.put("class", entityMetaClass);

                pickerlookupAction.setLookupScreenParams(params);
            }

            if ((linkedEntity != null) && (clazz != null) && (clazz.isAssignableFrom(linkedEntity.getClass())))
                pickerField.setValue(linkedEntity);

            return pickerField;
        }
    }

    private class MultiFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            TokenList tokenList = cFactory.createComponent(TokenList.NAME);
            final com.haulmont.chile.core.model.MetaClass entityMetaClass =
                    metadata.getSession().getClass(parameter.getEntityMetaClass());

            DsBuilder builder = new DsBuilder(getDsContext());
            CollectionDatasource cds = builder
                    .setRefreshMode(CollectionDatasource.RefreshMode.NEVER)
                    .setId("entities_" + parameter.getAlias())
                    .setMetaClass(entityMetaClass)
                    .setViewName(View.LOCAL)
                    .buildCollectionDatasource();

            cds.refresh();

            tokenList.setDatasource(cds);
            tokenList.setEditable(true);
            tokenList.setLookup(true);
            tokenList.setLookupOpenMode(WindowManager.OpenType.DIALOG);
            tokenList.setHeight("150px");
            String alias = parameter.getScreen();

            if (StringUtils.isNotEmpty(alias)) {
                tokenList.setLookupScreen(alias);
                tokenList.setLookupScreenParams(Collections.<String, Object>emptyMap());
            } else {
                tokenList.setLookupScreen("report$commonLookup");
                Map<String, Object> params = new HashMap<>();
                params.put("class", entityMetaClass);
                tokenList.setLookupScreenParams(params);
            }

            tokenList.setAddButtonCaption(messages.getMessage(TokenList.class, "actions.Select"));
            tokenList.setSimple(true);

            return tokenList;
        }
    }
}