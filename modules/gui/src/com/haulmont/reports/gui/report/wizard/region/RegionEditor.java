/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.report.wizard.region;

import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.AbstractTreeDatasource;
import com.haulmont.reports.entity.wizard.EntityTreeNode;
import com.haulmont.reports.entity.wizard.RegionProperty;
import com.haulmont.reports.entity.wizard.ReportRegion;
import com.haulmont.reports.gui.components.actions.OrderableItemMoveAction;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.cglib.core.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

public class RegionEditor extends AbstractEditor<ReportRegion> {
    @Named("entityTreeFrame.reportEntityTreeNodeDs")
    protected AbstractTreeDatasource reportEntityTreeNodeDs;
    @Inject
    protected CollectionDatasource<RegionProperty, UUID> reportRegionPropertiesTableDs;
    @Named("entityTreeFrame.entityTree")
    protected Tree<EntityTreeNode> entityTree;
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
    protected Table<RegionProperty> propertiesTable;
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
    protected boolean updatePermission;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        updatePermission =  !Boolean.TRUE.equals(params.get("updateDisabled"));
        Companion companion = getCompanion();
        if (companion != null) {
            if (updatePermission) companion.addTreeTableDblClickListener(entityTree, reportRegionPropertiesTableDs);
            companion.initControlBtnsActions(addItem, propertiesTable);
        }
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
        tipLabel.setHtmlEnabled(true);
        initComponents(params);
    }

    protected void initComponents(Map<String, Object> params) {
        initControlBtnsActions();
        addRegionPropertiesDsListener();

        getDialogOptions().setWidth(regionEditorWindowWidth).setWidthUnit(SizeUnit.PIXELS);
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
                    showNotification(getMessage("valueNotFound"), NotificationType.HUMANIZED);
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
        if (isTabulated) {
            setCaption(getMessage("singleEntityDataSetViewEditor"));
        } else {
            setCaption(getMessage("multiEntityDataSetViewEditor"));
        }
    }

    protected boolean isUpdatePermitted() {
        return updatePermission;
    }

    protected void setTabulatedRegionEditorCaption(String collectionEntityName) {
        setCaption(getMessage("tabulatedRegionEditor"));
    }

    protected void setSimpleRegionEditorCaption() {
        setCaption(getMessage("simpleRegionEditor"));
    }

    protected void addRegionPropertiesDsListener() {
        reportRegionPropertiesTableDs.addItemChangeListener(e -> showOrHideSortBtns());
        reportRegionPropertiesTableDs.addCollectionChangeListener(e -> showOrHideSortBtns());
    }

    protected void showOrHideSortBtns() {
        if (propertiesTable.getSelected().size() == reportRegionPropertiesTableDs.getItems().size() ||
                propertiesTable.getSelected().size() == 0) {
            upItem.setEnabled(false);
            downItem.setEnabled(false);
        } else {
            upItem.setEnabled(isUpdatePermitted());
            downItem.setEnabled(isUpdatePermitted());
        }
    }

    protected void initControlBtnsActions() {
        Action addAction = new ItemTrackingAction(entityTree, "addItem") {
            @Override
            public void actionPerform(Component component) {
                @SuppressWarnings("unchecked")
                List<EntityTreeNode> nodesList = CollectionUtils.transform(
                        reportRegionPropertiesTableDs.getItems(), o -> ((RegionProperty) o).getEntityTreeNode());

                Set<EntityTreeNode> alreadyAddedNodes = new HashSet<>(nodesList);

                Set<EntityTreeNode> selectedItems = entityTree.getSelected();
                List<RegionProperty> addedItems = new ArrayList<>();
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
                        showNotification(getMessage("selectPropertyFromEntity"), NotificationType.HUMANIZED);
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

            @Override
            public boolean isEnabled() {
                return super.isEnabled() && isUpdatePermitted();
            }
        };
        entityTree.addAction(addAction);
        addItem.setAction(addAction);

        Action removeAction = new ItemTrackingAction(propertiesTable, "removeItem") {
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

            @Override
            public boolean isEnabled() {
                return super.isEnabled() && isUpdatePermitted();
            }
        };
        propertiesTable.addAction(removeAction);
        removeItem.setAction(removeAction);

        upItem.setAction(new OrderableItemMoveAction<Table<RegionProperty>, RegionProperty>("upItem", OrderableItemMoveAction.Direction.UP, propertiesTable) {
            @Override
            public boolean isEnabled() {
                return super.isEnabled() && isUpdatePermitted();
            }
        });
        downItem.setAction(new OrderableItemMoveAction<Table<RegionProperty>, RegionProperty>("downItem", OrderableItemMoveAction.Direction.DOWN, propertiesTable) {
            @Override
            public boolean isEnabled() {
                return super.isEnabled() && isUpdatePermitted();
            }
        });
    }

    protected void normalizeRegionPropertiesOrderNum() {
        long normalizedIdx = 0;
        List<RegionProperty> allItems = new ArrayList<>(reportRegionPropertiesTableDs.getItems());
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
        void addTreeTableDblClickListener(Tree entityTree, final CollectionDatasource<RegionProperty, UUID> reportRegionPropertiesTableDs);

        void initControlBtnsActions(Button button, Table table);
    }
}