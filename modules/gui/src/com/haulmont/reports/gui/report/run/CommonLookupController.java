/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.gui.report.run;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class CommonLookupController extends AbstractLookup {

    @Inject
    private BoxLayout mainPane;

    @Inject
    private Messages messages;

    private ComponentsFactory cFactory = AppConfig.getFactory();

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        final MetaClass metaClass = (MetaClass) params.get("class");
        final Class javaClass = metaClass.getJavaClass();

        this.setCaption(messages.getMessage(javaClass, javaClass.getSimpleName()));

        CollectionDatasource cds = new DsBuilder(getDsContext())
                .setId("mainDs")
                .setMetaClass(metaClass)
                .setViewName(View.MINIMAL)
                .buildCollectionDatasource();

        final Table table = cFactory.createComponent(Table.NAME);
                MetaPropertyPath nameProperty = new MetaPropertyPath(metaClass,
                        new ArrayList<>(metaClass.getOwnProperties()).get(0));
        table.setId("lookupTable");

        Table.Column nameColumn = new Table.Column(nameProperty);
        nameColumn.setCaption(messages.getMessage(getClass(), "name"));

        table.addColumn(nameColumn);
        table.setDatasource(cds);

        table.addGeneratedColumn(nameProperty.getMetaProperty().getName(), new Table.ColumnGenerator() {
            @Override
            public Component generateCell(Entity entity) {
                // noinspection unchecked
                Label label = cFactory.createComponent(Label.NAME);
                label.setValue(entity.getInstanceName());
                return label;
            }
        });

        table.setMultiSelect(true);
        table.setWidth("100%");
        table.setHeight("100%");
        mainPane.add(table);

        table.refresh();

        this.setLookupComponent(table);

        getDialogParams().setHeight(350);
    }
}
