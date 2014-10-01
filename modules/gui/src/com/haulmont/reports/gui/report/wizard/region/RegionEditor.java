/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.report.wizard.region;

import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.AbstractTreeDatasource;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.reports.entity.wizard.EntityTreeNode;
import com.haulmont.reports.entity.wizard.RegionProperty;
import com.haulmont.reports.entity.wizard.ReportRegion;
import com.haulmont.reports.gui.components.actions.OrderableItemMoveAction;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.cglib.core.CollectionUtils;
import org.springframework.cglib.core.Transformer;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author fedorchenko
 * @version $Id$
 */
public class RegionEditor extends AbstractEditor<ReportRegion> {
    @Named("entityTreeFrame.reportEntityTreeNodeDs")
    protected AbstractTreeDatasource reportEntityTreeNodeDs;
    @Inject
    protected CollectionDatasource<RegionProperty, UUID> reportRegionPropertiesTableDs;
    @Named("entityTreeFrame.entityTree")
    protected Tree entityTree;
    @Named("entityTreeFrame.reportPropertyName")
    protected TextField reportPropertyName;
    @Named("entityTreeFrame.reportPropertyNameSearchButton")
    protected Button reportPropertyNameSearchButton;
    @Inject
    protected Datasource reportRegionDs;
    @Inject
    protected Button addItem;
    @Inject
    protected Button removeItem;
    @Inject
    protected Button upItem;
    @Inject
    protected Button downItem;
    @Inject
    protected Table propertiesTable;
    @Inject
    protected Label tipLabel;
    @Inject
    protected Metadata metadata;
    @Inject
    protected Configuration configuration;
    protected boolean isTabulated;//if true then user perform add tabulated region action
    protected int regionEditorWindowWidth = 950;
    protected boolean asViewEditor;
    protected EntityTreeNode rootNode;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        Companion companion = getCompanion();
        companion.addTreeTableDblClickListener(entityTree, reportRegionPropertiesTableDs);
        companion.initControlBtnsActions(addItem, propertiesTable);
        isTabulated = ((ReportRegion) WindowParams.ITEM.getEntity(params)).getIsTabulatedRegion();
        asViewEditor = BooleanUtils.isTrue((Boolean) params.get("asViewEditor"));
        params.put("component$reportPropertyName", reportPropertyName);
        reportEntityTreeNodeDs.refresh(params);
        //TODO add disallowing of classes selection in tree
        rootNode = (EntityTreeNode) params.get("rootEntity");
        if (!asViewEditor) {
            if (isTabulated) {
                setTabulatedRegionEditorCaption(((EntityTreeNode) (params.get("rootEntity"))).getName());
            } else {
                setSimpleRegionEditorCaption();
            }
        }
        tipLabel.setValue(
                formatMessage(isTabulated ? "selectEntityPropertiesForTableArea" : "selectEntityProperties", rootNode.getLocalizedName()));
        initComponents(params);
    }

    @Override
    protected void postInit() {
        super.postInit();
    }

    protected void initComponents(Map<String, Object> params) {
        initControlBtnsActions();
        addRegionPropertiesDsListener();

        getWindowManager().getDialogParams().setWidth(regionEditorWindowWidth);
        if (asViewEditor) {
            initAsViewEditor();
        }
        entityTree.setMultiSelect(true);
        entityTree.expand(rootNode.getId());

        Action search = new AbstractAction("search", configuration.getConfig(ClientConfig.class).getFilterApplyShortcut()) {
            @Override
            public void actionPerform(Component component) {
                reportEntityTreeNodeDs.refresh();
                if (!reportEntityTreeNodeDs.getItemIds().isEmpty()) {
                    entityTree.collapseTree();
                    entityTree.expand(rootNode.getId());
                } else {
                    showNotification(getMessage("valueNotFound"), NotificationType.TRAY);
                }
            }

            @Override
            public String getCaption() {
                return null;
            }
        };
        reportPropertyNameSearchButton.setAction(search);
        addAction(search);
    }

    protected void initAsViewEditor() {
        reportRegionDs.setAllowCommit(false);
        reportRegionPropertiesTableDs.setAllowCommit(false);
//        upItem.setVisible(false);
//        downItem.setVisible(false);
        if (isTabulated) {
            setCaption(getMessage("singleEntityDataSetViewEditor"));
        } else {
            setCaption(getMessage("multiEntityDataSetViewEditor"));
        }
    }

    protected void setTabulatedRegionEditorCaption(String collectionEntityName) {
        setCaption(getMessage("tabulatedRegionEditor"));
    }

    protected void setSimpleRegionEditorCaption() {
        setCaption(getMessage("simpleRegionEditor"));
    }

    protected void addRegionPropertiesDsListener() {
        reportRegionPropertiesTableDs.addListener(new CollectionDsListenerAdapter() {
            @Override
            public void itemChanged(Datasource ds, @Nullable Entity prevItem, @Nullable Entity item) {
                super.itemChanged(ds, prevItem, item);
                showOrHideSortBtns();
            }

            @Override
            public void collectionChanged(CollectionDatasource ds, Operation operation, List items) {
                super.collectionChanged(ds, operation, items);
                showOrHideSortBtns();
            }

            void showOrHideSortBtns() {
                if (propertiesTable.getSelected().size() == reportRegionPropertiesTableDs.getItems().size() ||
                        propertiesTable.getSelected().size() == 0) {
                    upItem.setEnabled(false);
                    downItem.setEnabled(false);
                } else {
                    upItem.setEnabled(true);
                    downItem.setEnabled(true);
                }
            }
        });
    }

    protected void initControlBtnsActions() {
        addItem.setAction(new AbstractAction("addItem") {
            @Override
            public void actionPerform(Component component) {

                Set<EntityTreeNode> alreadyAddedNodes = new HashSet<>();

                alreadyAddedNodes.addAll(CollectionUtils.transform(reportRegionPropertiesTableDs.getItems(), new Transformer() {
                    @Override
                    public Object transform(Object o) {
                        return ((RegionProperty) o).getEntityTreeNode();
                    }
                }));

                Set<EntityTreeNode> selectedItems = entityTree.getSelected();
                List addedItems = new ArrayList();
                boolean alreadyAdded = false;
                for (EntityTreeNode entityTreeNode : selectedItems) {
                    if (entityTreeNode.getWrappedMetaClass() != null) {
                        continue;
                    }
                    if (!alreadyAddedNodes.contains(entityTreeNode)) {
                        RegionProperty regionProperty = metadata.create(RegionProperty.class);
                        regionProperty.setEntityTreeNode(entityTreeNode);
                        regionProperty.setOrderNum((long) reportRegionPropertiesTableDs.getItemIds().size() + 1); //first element must be not zero cause later we do sorting by multiplying that values
                        reportRegionPropertiesTableDs.addItem(regionProperty);
                        addedItems.add(regionProperty);
                    } else {
                        alreadyAdded = true;
                    }
                }
                if (addedItems.isEmpty()) {
                    if (alreadyAdded)
                        showNotification(getMessage("elementsAlreadyAdded"), NotificationType.TRAY);
                    else if (selectedItems.size() != 0)
                        showNotification(getMessage("selectPropertyFromEntity"), NotificationType.TRAY);
                    else
                        showNotification(getMessage("elementsWasNotAdded"), NotificationType.TRAY);
                } else {
                    propertiesTable.setSelected(addedItems);
                }
            }

            @Override
            public String getCaption() {
                return "";
            }
        });
        removeItem.setAction(new AbstractAction("removeItem") {
            @Override
            public void actionPerform(Component component) {
                for (Entity item : propertiesTable.getSelected()) {
                    reportRegionPropertiesTableDs.removeItem((RegionProperty) item);
                    normalizeRegionPropertiesOrderNum();
                }
            }

            @Override
            public String getCaption() {
                return "";
            }
        });
        upItem.setAction(new OrderableItemMoveAction("upItem", OrderableItemMoveAction.Direction.UP, propertiesTable));
        downItem.setAction(new OrderableItemMoveAction("downItem", OrderableItemMoveAction.Direction.DOWN, propertiesTable));
    }

    protected void normalizeRegionPropertiesOrderNum() {
        long normalizedIdx = 0;
        List<RegionProperty> allItems = new ArrayList(reportRegionPropertiesTableDs.getItems());
        for (RegionProperty item : allItems) {
            item.setOrderNum(++normalizedIdx); //first must to be 1
        }
    }

    @Override
    protected boolean preCommit() {
        if (reportRegionPropertiesTableDs.getItems().isEmpty()) {
            showNotification(getMessage("selectAtLeastOneProp"), NotificationType.TRAY);
            return false;
        }
        return super.preCommit();
    }

    public interface Companion {
        void addTreeTableDblClickListener(Tree entityTree, final CollectionDatasource reportRegionPropertiesTableDs);

        void initControlBtnsActions(Button button, Table table);
    }
}

