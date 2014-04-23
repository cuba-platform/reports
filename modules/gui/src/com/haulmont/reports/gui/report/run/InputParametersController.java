/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.gui.report.run;

import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.cuba.core.entity.Entity;
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
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.reports.entity.ParameterType;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.gui.ReportGuiManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class InputParametersController extends AbstractWindow {

    protected interface FieldCreator {
        Field createField(ReportInputParameter parameter);
    }

    protected ComponentsFactory cFactory = AppConfig.getFactory();

    protected Report report;

    protected Entity linkedEntity;

    protected Map<String, Object> parameters;

    protected String templateCode;

    protected String outputFileName;

    @Inject
    protected Messages messages;

    @Inject
    protected Metadata metadata;

    @Inject
    protected GridLayout parametersGrid;

    @Inject
    protected ReportGuiManager reportGuiManager;

    protected HashMap<String, Field> parameterComponents = new HashMap<>();

    protected Map<ParameterType, FieldCreator> fieldCreationMapping = new HashMap<>();

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

        report = (Report) params.get("report");
        linkedEntity = (Entity) params.get("entity");
        parameters = (Map<String, Object>) params.get("parameters");
        templateCode = (String) params.get("templateCode");
        outputFileName = (String) params.get("outputFileName");

        if (parameters == null) {
            parameters = Collections.emptyMap();
        }

        if (report != null) {
            if (!report.getIsTmp()){
                report = getDsContext().getDataSupplier().reload(report, "report.edit");
            }
            if (CollectionUtils.isNotEmpty(report.getInputParameters())) {
                parametersGrid.setRows(report.getInputParameters().size());

                int currentGridRow = 0;
                for (ReportInputParameter parameter : report.getInputParameters()) {
                    createComponent(parameter, currentGridRow);
                    currentGridRow++;
                }
            }
        }
    }

    public void printReport() {
        if (report != null) {
            if (validateAll()) {
                reportGuiManager.printReport(report,
                        collectParameters(parameterComponents), templateCode, outputFileName, this);
            }
        }
    }

    protected Map<String, Object> collectParameters(HashMap<String, Field> parameterComponents) {
        Map<String, Object> parameters = new HashMap<>();
        for (String paramName : parameterComponents.keySet()) {
            Field parameterField = parameterComponents.get(paramName);
            Object value = parameterField.getValue();
            parameters.put(paramName, value);
        }
        return parameters;
    }

    protected void createComponent(ReportInputParameter parameter, int currentGridRow) {
        Field field = fieldCreationMapping.get(parameter.getType()).createField(parameter);
        field.setRequiredMessage(formatMessage("error.paramIsRequiredButEmpty", parameter.getLocName()));

        field.setId(parameter.getAlias());
        field.setWidth("250px");
        field.setFrame(this);
        field.setEditable(true);

        parameterComponents.put(parameter.getAlias(), field);
        field.setRequired(parameter.getRequired());

        Object value = parameters.get(parameter.getAlias());
        if (!(field instanceof TokenList)) {
            field.setValue(value);
        } else {
            CollectionDatasource datasource = (CollectionDatasource) field.getDatasource();
            if (value instanceof Collection) {
                Collection collection = (Collection) value;
                for (Object selected : collection) {
                    datasource.includeItem((Entity) selected);
                }
            }
        }

        Label label = cFactory.createComponent(Label.NAME);
        label.setAlignment(Alignment.MIDDLE_LEFT);
        label.setValue(parameter.getLocName());

        parametersGrid.add(label, 0, currentGridRow);
        parametersGrid.add(field, 1, currentGridRow);
    }

    protected class DateFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            DateField dateField = cFactory.createComponent(DateField.NAME);
            dateField.setResolution(Resolution.DAY);
            dateField.setDateFormat(messages.getMessage(AppConfig.getMessagesPack(), "dateFormat"));
            return dateField;
        }
    }

    protected class DateTimeFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            DateField dateField = cFactory.createComponent(DateField.NAME);
            dateField.setResolution(Resolution.MIN);
            dateField.setDateFormat(messages.getMessage(AppConfig.getMessagesPack(), "dateTimeFormat"));
            return dateField;
        }
    }

    protected class TimeFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            return cFactory.createComponent(TimeField.NAME);
        }
    }

    protected class CheckBoxCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            return cFactory.createComponent(CheckBox.NAME);
        }
    }

    protected class TextFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            return cFactory.createComponent(TextField.NAME);
        }
    }

    protected class NumericFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            TextField textField = cFactory.createComponent(TextField.NAME);
            textField.addValidator(new DoubleValidator());
            textField.setDatatype(Datatypes.getNN(Double.class));
            return textField;
        }
    }

    protected class EnumFieldCreator implements FieldCreator {

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

    protected class SingleFieldCreator implements FieldCreator {

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

    protected class MultiFieldCreator implements FieldCreator {

        @Override
        public Field createField(final ReportInputParameter parameter) {
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
            String screen = parameter.getScreen();

            if (StringUtils.isNotEmpty(screen)) {
                tokenList.setLookupScreen(screen);
                tokenList.setLookupScreenParams(Collections.<String, Object>emptyMap());
            } else {
                tokenList.setLookupScreen("report$commonLookup");
                Map<String, Object> params = new HashMap<>();
                params.put("class", entityMetaClass);
                tokenList.setLookupScreenParams(params);
            }

            tokenList.setAddButtonCaption(messages.getMessage(TokenList.class, "actions.Select"));
            tokenList.setSimple(true);

            if (Boolean.TRUE.equals(parameter.getRequired())) {
                tokenList.addValidator(new Field.Validator() {
                    @Override
                    public void validate(Object value) throws ValidationException {
                        if (value instanceof Collection && CollectionUtils.isEmpty((Collection) value)) {
                            throw new ValidationException(formatMessage("error.paramIsRequiredButEmpty", parameter.getLocName()));
                        }
                    }
                });
            }


            return tokenList;
        }
    }
}