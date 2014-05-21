/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.web.report.wizard.region;

import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;

/**
 * @author kozyaikin
 * @version $Id$
 */
public class RegionEditor extends com.haulmont.reports.gui.report.wizard.region.RegionEditor {

    @Override
    protected void initControlBtnsActions() {
        super.initControlBtnsActions();
        ((Button)WebComponentsHelper.unwrap(addItem)).addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ((Table) WebComponentsHelper.unwrap(propertiesTable)).setCurrentPageFirstItemId(((Table) WebComponentsHelper.unwrap(propertiesTable)).lastItemId());
            }
        });
    }
}
