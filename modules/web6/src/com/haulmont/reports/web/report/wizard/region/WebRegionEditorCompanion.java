/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.web.report.wizard.region;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.Tree;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.cuba.web.gui.data.ItemWrapper;
import com.haulmont.reports.entity.wizard.EntityTreeNode;
import com.haulmont.reports.entity.wizard.RegionProperty;
import com.haulmont.reports.gui.report.wizard.region.RegionEditor;
import com.vaadin.event.ItemClickEvent;
import org.springframework.cglib.core.CollectionUtils;
import org.springframework.cglib.core.Transformer;

import java.util.UUID;

/**
 * @author fedorchenko
 * @version $Id$
 */
public class WebRegionEditorCompanion implements RegionEditor.Companion {
    @Override
    public void addTreeTableDblClickListener(final Tree entityTree, final CollectionDatasource<RegionProperty, UUID> reportRegionPropertiesTableDs) {
        final com.haulmont.cuba.web.toolkit.ui.Tree webTree = WebComponentsHelper.unwrap(entityTree);
        webTree.setDoubleClickMode(true);
        webTree.addListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) { //always false here (
                    if (event.getItem() instanceof ItemWrapper && ((ItemWrapper) event.getItem()).getItem() instanceof EntityTreeNode) {
                        EntityTreeNode entityTreeNode = (EntityTreeNode) ((ItemWrapper) event.getItem()).getItem();
                        if (entityTreeNode.getWrappedMetaClass() != null) {
                            if (webTree.isExpanded(entityTreeNode.getId()))
                                webTree.collapseItem(entityTreeNode.getId());
                            else
                                webTree.expandItem(entityTreeNode.getId());
                            return;
                        }
                        if (CollectionUtils.transform(reportRegionPropertiesTableDs.getItems(), new Transformer() {
                            @Override
                            public Object transform(Object o) {
                                return ((RegionProperty) o).getEntityTreeNode();
                            }
                        }).contains(entityTreeNode)) {
                            return;
                        }
                        Metadata metadata = AppBeans.get(Metadata.NAME);
                        RegionProperty regionProperty = metadata.create(RegionProperty.class);
                        regionProperty.setEntityTreeNode(entityTreeNode);
                        regionProperty.setOrderNum((long) reportRegionPropertiesTableDs.getItemIds().size() + 1); //first element must be not zero cause later we do sorting by multiplying that values
                        reportRegionPropertiesTableDs.addItem(regionProperty);
                        Table propertiesTable = entityTree.getFrame().getComponent("propertiesTable");
                        if (propertiesTable != null) {
                            propertiesTable.setSelected(regionProperty);
                            ((com.vaadin.ui.Table) WebComponentsHelper.unwrap(propertiesTable)).setCurrentPageFirstItemId(regionProperty.getId());
                        }
                    }
                }
            }
        });
    }

    @Override
    public void initControlBtnsActions(Button button, final Table table) {
        ((com.vaadin.ui.Button) WebComponentsHelper.unwrap(button)).addListener(new com.vaadin.ui.Button.ClickListener() {
            @Override
            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
                com.vaadin.ui.Table vaadinTable = (com.vaadin.ui.Table) WebComponentsHelper.unwrap(table);
                vaadinTable.setCurrentPageFirstItemId(vaadinTable.lastItemId());
                vaadinTable.requestRepaint();
            }
        });
    }
}
