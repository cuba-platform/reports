/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.gui.parameter.edit;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.utils.InstanceUtils;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.ScreensHelper;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.ParameterType;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.gui.report.run.ParameterClassResolver;
import com.haulmont.reports.gui.report.run.ParameterFieldCreator;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class ParameterEditor extends AbstractEditor {
    @Inject
    private Label defaultValueLabel;

    @Inject
    protected BoxLayout defaultValueBox;

    @Inject
    protected LookupField screen;

    @Inject
    protected LookupField enumeration;

    @Inject
    private LookupField type;

    @Inject
    protected LookupField metaClass;

    @Inject
    protected Metadata metadata;

    @Inject
    protected ReportService reportService;

    @Inject
    protected Datasource<ReportInputParameter> parameterDs;

    @Inject
    protected ScreensHelper screensHelper;

    protected ReportInputParameter parameter;

    protected ParameterFieldCreator parameterFieldCreator = new ParameterFieldCreator(this);

    protected ParameterClassResolver parameterClassResolver = new ParameterClassResolver();

    @Override
    public void setItem(Entity item) {
        ReportInputParameter newItem = parameterDs.getDataSupplier().newInstance(parameterDs.getMetaClass());
        InstanceUtils.copy(item, newItem);
        newItem.setId((UUID) item.getId());
        if (newItem.getParameterClass() == null) {
            newItem.setParameterClass(parameterClassResolver.resolveClass(newItem));
        }

        super.setItem(newItem);
        parameter = (ReportInputParameter) getItem();
        enableControlsByParamType(parameter.getType());
    }

    @Override
    @SuppressWarnings({"unchecked", "serial"})
    public void init(Map<String, Object> params) {
        super.init(params);

        initMetaClassLookup();

        initEnumsLookup();

        initListeners();

        getDialogParams().setWidthAuto();
    }

    protected void initListeners() {
        type.addValueChangeListener(e -> enableControlsByParamType(e.getValue()));

        parameterDs.addItemPropertyChangeListener(e -> {
            boolean typeChanged = e.getProperty().equalsIgnoreCase("type");
            boolean classChanged = e.getProperty().equalsIgnoreCase("entityMetaClass")
                    || e.getProperty().equalsIgnoreCase("enumerationClass");
            if (typeChanged || classChanged) {
                parameter.setParameterClass(parameterClassResolver.resolveClass(parameter));

                if (typeChanged) {
                    parameter.setEntityMetaClass(null);
                    parameter.setEnumerationClass(null);
                }

                parameter.setDefaultValue(null);
                parameter.setScreen(null);

                initDefaultValueField();
            }

            ((DatasourceImplementation<ReportInputParameter>) parameterDs).modified(e.getItem());
        });
    }

    protected void initScreensLookup() {
        Map<String, Object> screensMap = screensHelper.getAvailableBrowserScreens(parameter.getParameterClass());
        screen.setOptionsMap(screensMap);
    }

    protected void initEnumsLookup() {
        Map<String, Object> enumsOptionsMap = new TreeMap<>();
        for (Class enumClass : metadata.getTools().getAllEnums()) {
            String enumLocalizedName = messages.getMessage(enumClass, enumClass.getSimpleName());
            enumsOptionsMap.put(enumLocalizedName + " (" + enumClass.getSimpleName() + ")", enumClass.getCanonicalName());
        }
        enumeration.setOptionsMap(enumsOptionsMap);
    }

    protected void initMetaClassLookup() {
        Map<String, Object> metaClassesOptionsMap = new TreeMap<>();
        Collection<MetaClass> classes = metadata.getSession().getClasses();
        for (MetaClass clazz : classes) {
            if (!metadata.getTools().isSystemLevel(clazz)) {
                String caption = messages.getTools().getDetailedEntityCaption(clazz);
                metaClassesOptionsMap.put(caption, clazz.getName());
            }
        }
        metaClass.setOptionsMap(metaClassesOptionsMap);
    }

    protected void initDefaultValueField() {
        defaultValueLabel.setVisible(false);
        for (Component component : new ArrayList<>(defaultValueBox.getComponents())) {
            defaultValueBox.remove(component);
        }

        if (canHaveDefaultValue()) {
            Field field = parameterFieldCreator.createField(parameter);
            field.addValueChangeListener(e -> {
                if (e.getValue() != null) {
                    parameter.setDefaultValue(reportService.convertToString(e.getValue().getClass(), e.getValue()));
                }
            });

            if (parameter.getParameterClass() != null) {
                field.setValue(reportService.convertFromString(parameter.getParameterClass(), parameter.getDefaultValue()));
                initScreensLookup();
            }
            field.setRequired(false);

            defaultValueBox.add(field);
            defaultValueLabel.setVisible(true);
        }
    }

    protected boolean canHaveDefaultValue() {
        if (parameter == null) return false;

        ParameterType type = parameter.getType();
        return type != null && type != ParameterType.ENTITY_LIST
                && (type != ParameterType.ENTITY || StringUtils.isNotBlank(parameter.getEntityMetaClass()))
                && (type != ParameterType.ENUMERATION || StringUtils.isNotBlank(parameter.getEnumerationClass()));
    }

    protected void enableControlsByParamType(Object value) {
        boolean isEntity = ParameterType.ENTITY.equals(value) || ParameterType.ENTITY_LIST.equals(value);
        boolean isEnum = ParameterType.ENUMERATION.equals(value);
        metaClass.setEnabled(isEntity);
        screen.setEnabled(isEntity);
        enumeration.setEnabled(isEnum);

        initDefaultValueField();
    }
}