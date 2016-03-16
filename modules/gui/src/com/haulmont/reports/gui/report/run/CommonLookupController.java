/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports.gui.report.run;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;

import javax.inject.Inject;
import java.util.Map;

/**
 */
public class CommonLookupController extends AbstractLookup {

    public static final String CLASS_PARAMETER = "class";
    @Inject
    private BoxLayout mainPane;

    @Inject
    private Messages messages;

    private ComponentsFactory cFactory = AppConfig.getFactory();

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        final MetaClass metaClass = (MetaClass) params.get(CLASS_PARAMETER);
        final Class javaClass = metaClass.getJavaClass();

        this.setCaption(messages.getMessage(javaClass, javaClass.getSimpleName()));

        CollectionDatasource cds = new DsBuilder(getDsContext())
                .setId("mainDs")
                .setMetaClass(metaClass)
                .setViewName(View.LOCAL)
                .buildCollectionDatasource();
        cds.setQuery("select e from " + metaClass.getName() + " e");

        final Table table = cFactory.createComponent(Table.class);
        table.setId("lookupTable");
        table.setFrame(this);
        table.setDatasource(cds);
        table.setMultiSelect(true);
        table.setWidth("100%");
        table.setHeight("100%");
        table.requestFocus();

        RowsCount rowsCount = cFactory.createComponent(RowsCount.class);
        rowsCount.setDatasource(cds);
        rowsCount.setOwner(table);
        table.setRowsCount(rowsCount);

        Filter filter = cFactory.createComponent(Filter.class);
        filter.setFrame(this);
        filter.setId("filter");
        filter.setApplyTo(table);
        filter.setDatasource(cds);
        filter.setMaxResults(100);
        filter.setUseMaxResults(true);
        filter.setManualApplyRequired(true);
        filter.setEditable(true);
        ((FilterImplementation) filter).loadFiltersAndApplyDefault();

        mainPane.add(filter);
        mainPane.add(table);
        mainPane.expand(table);

        filter.apply(true);

        this.setLookupComponent(table);
    }
}