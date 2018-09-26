/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.web.report.wizard.region;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.Tree;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.web.gui.data.ItemWrapper;
import com.haulmont.cuba.web.widgets.CubaTree;
import com.haulmont.reports.entity.wizard.EntityTreeNode;
import com.haulmont.reports.entity.wizard.RegionProperty;
import com.haulmont.reports.gui.report.wizard.region.RegionEditor;
import org.springframework.cglib.core.CollectionUtils;
import org.springframework.cglib.core.Transformer;

import java.util.UUID;

public class WebRegionEditorCompanion implements RegionEditor.Companion {
    @Override
    public void addTreeTableDblClickListener(final Tree entityTree, final CollectionDatasource<RegionProperty, UUID> reportRegionPropertiesTableDs) {
        CubaTree webTree = entityTree.unwrap(CubaTree.class);
        webTree.addItemClickListener(event -> {
            if (event.getMouseEventDetails().isDoubleClick()) {
                // TODO: gg, fix
                if (event.getItem() instanceof ItemWrapper
                        && ((ItemWrapper) event.getItem()).getItem() instanceof EntityTreeNode) {
                    EntityTreeNode entityTreeNode = (EntityTreeNode) ((ItemWrapper) event.getItem()).getItem();
                    if (entityTreeNode.getWrappedMetaClass() != null) {
                        if (webTree.isExpanded(entityTreeNode))
                            webTree.collapse(entityTreeNode);
                        else
                            webTree.expand(entityTreeNode);
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
                    regionProperty.setOrderNum((long) reportRegionPropertiesTableDs.getItemIds().size() + 1);
                    //first element must be not zero cause later we do sorting by multiplying that values
                    reportRegionPropertiesTableDs.addItem(regionProperty);

                    Table propertiesTable = (Table) entityTree.getFrame().getComponent("propertiesTable");
                    if (propertiesTable != null) {
                        propertiesTable.setSelected(regionProperty);

                        (propertiesTable.unwrap(com.vaadin.v7.ui.Table.class)).setCurrentPageFirstItemId(regionProperty.getId());
                    }
                }
            }
        });
    }

    @Override
    public void initControlBtnsActions(Button button, final Table table) {
        button.unwrap(com.vaadin.ui.Button.class).addClickListener((com.vaadin.ui.Button.ClickListener) event -> {
            com.vaadin.v7.ui.Table vaadinTable = table.unwrap(com.vaadin.v7.ui.Table.class);
            vaadinTable.setCurrentPageFirstItemId(vaadinTable.lastItemId());
            vaadinTable.refreshRowCache();
        });
    }
}