/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.components.actions;

import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.ListComponent;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.reports.entity.wizard.OrderableEntity;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Move items in ListComponent up or down. Items in datasource must to be an entity of type "OrderableEntity". <br/>
 * Note that order num of item will be changed for moving
 * Move algorithm is differ from selected items count:
 * <ul><li>swap items algorithm if one item is selected </li><li>index recalculating algorithm if more than one item selected)</li><ul/>
 *
 * @author fedorchenko
 * @version $Id$
 */
public class OrderableItemMoveAction<T extends ListComponent> extends AbstractAction {
    protected T listComponent;
    protected Direction direction;

    public OrderableItemMoveAction(String actionId, Direction direction, T listComponent, @Nullable String shortcut) {
        super(actionId, shortcut);
        if (!(OrderableEntity.class.isAssignableFrom(listComponent.getDatasource().getMetaClass().getJavaClass()))) {
            throw new UnsupportedOperationException("List component must contain datasource with entities type of OrderableEntity for ordering");
        }
        this.direction = direction;
        this.listComponent = listComponent;
    }

    public OrderableItemMoveAction(String actionId, Direction direction, T listComponent) {
        this(actionId, direction, listComponent, null);
    }

    @Override
    public void actionPerform(Component component) {
        swapItems();
    }

    protected void swapItems() {
        if (listComponent.getSelected().size() <= 1) {
            swapSingleItemWithNeighbour();
        } else {
            moveFewItems();
        }
    }

    /**
     * Swap items is simple
     */
    protected void swapSingleItemWithNeighbour() {
        OrderableEntity selectedItem = listComponent.getSingleSelected();
        OrderableEntity neighbourItem = null;
        List<OrderableEntity> allItems = new ArrayList(listComponent.getDatasource().getItems());
        for (ListIterator<OrderableEntity> iterator = allItems.listIterator(); iterator.hasNext(); ) {
            if (iterator.next().equals(selectedItem)) {
                neighbourItem = getItemNeighbour(iterator);
                break;
            }
        }
        if (neighbourItem != null) {
            switch (direction) {
                case UP:
                    neighbourItem.setOrderNum(neighbourItem.getOrderNum() + 1);
                    selectedItem.setOrderNum(selectedItem.getOrderNum() - 1);
                    break;
                case DOWN:
                    neighbourItem.setOrderNum(neighbourItem.getOrderNum() - 1);
                    selectedItem.setOrderNum(selectedItem.getOrderNum() + 1);
                    break;
            }
            sortTableDsByItemsOrderNum();
        }
    }

    protected OrderableEntity getItemNeighbour(ListIterator<OrderableEntity> iterator) {
        OrderableEntity neighbourItem = null;
        switch (direction) {
            case UP:
                iterator.previous(); //lets go 1 step back
                if (iterator.hasPrevious()) {
                    neighbourItem = iterator.previous();
                }
                break;
            case DOWN:
                if (iterator.hasNext()) {
                    neighbourItem = iterator.next();
                }
                break;
        }
        return neighbourItem;
    }

    protected void sortTableDsByItemsOrderNum() {
        if (listComponent.getDatasource() instanceof CollectionDatasource.Sortable) {
            CollectionDatasource.Sortable.SortInfo sortInfo = new CollectionDatasource.Sortable.SortInfo();
            sortInfo.setOrder(CollectionDatasource.Sortable.Order.ASC);
            sortInfo.setPropertyPath(listComponent.getSingleSelected().getMetaClass().getPropertyPath("orderNum"));
            ((CollectionDatasource.Sortable) listComponent.getDatasource()).sort(new CollectionDatasource.Sortable.SortInfo[]{sortInfo});
        }
    }

    /**
     * Move few items up or down by recalculating their indexes.
     * Then we sorting table and normalize indexes
     */
    protected void moveFewItems() {
        //System.out.println("swap-------------");
        List<OrderableEntity> allItems = new ArrayList(listComponent.getDatasource().getItems());
        Set<OrderableEntity> selectedItems = listComponent.getSelected();
        int spreadKoef = listComponent.getSelected().size();//U can use 10 for easier debug

        long idx = 0;
        long lastIdxInGrp = Long.MAX_VALUE;
        long idxInGrp = 0;

        Map<OrderableEntity, Long> itemAndIndexInSelectedGroup = Collections.EMPTY_MAP;//for detection new orderNum values we must to store information about size of selected groups of items
        for (OrderableEntity item : allItems) {
            ++idx;
            item.setOrderNum(item.getOrderNum() * spreadKoef); //spread item indexes

            if (selectedItems.contains(item)) {
                if (itemAndIndexInSelectedGroup.isEmpty()) {
                    //start to store selected items with them index changing values to that map
                    itemAndIndexInSelectedGroup = new LinkedHashMap<>();
                }

                if (!itemAndIndexInSelectedGroup.isEmpty() || lastIdxInGrp == Long.MAX_VALUE) { //check that we are still in group of sequential selected items. sequence can contain one element
                    //we are enter inside group of selected item(s) now
                    idxInGrp++;
                    itemAndIndexInSelectedGroup.put(item, idxInGrp);
                }
                lastIdxInGrp = idx;
            } else {
                //we left group of sequential selected items. Now lets calc new orderNum values
                if (!itemAndIndexInSelectedGroup.isEmpty()) {
                    //System.out.print("*midGrp size" + idxInGrp + "*");
                    updateItemOrderNums(idxInGrp, itemAndIndexInSelectedGroup, spreadKoef);
                    itemAndIndexInSelectedGroup = Collections.EMPTY_MAP;
                }
                //reset counter values for re-use
                idxInGrp = 0;
                lastIdxInGrp = Long.MAX_VALUE;
            }
            //System.out.print("before " + item.getOrderNum() + " |");
            //System.out.print("after " + item.getOrderNum() + " |\n");
        }
        //we left group of sequential selected items. Last item was selected in list. Now lets calc new orderNum values
        if (!itemAndIndexInSelectedGroup.isEmpty()) {
            //System.out.print("*endGrp size" + idxInGrp + "*");
            updateItemOrderNums(idxInGrp, itemAndIndexInSelectedGroup, spreadKoef);
        }
        sortTableDsByItemsOrderNum();//lets sort by the new values
        normalizeEntityOrderNum(); //lets normalize recalculated indexes like 1,2,3...
    }

    private void updateItemOrderNums(long grpSize, Map<OrderableEntity, Long> itemAndIndexInGroup, int spreadKoef) {
        for (OrderableEntity itemToChange : itemAndIndexInGroup.keySet()) {
            //System.out.print("*** before " + itemToChange.getOrderNum() + " |");
            long newValue = itemToChange.getOrderNum();
            long itemIndexInGrp = itemAndIndexInGroup.get(itemToChange);
            switch (direction) {
                case UP:
                    newValue = itemToChange.getOrderNum() + (grpSize - itemIndexInGrp) * spreadKoef - (grpSize - itemIndexInGrp + 1) - grpSize * spreadKoef;
                    break;
                case DOWN:
                    newValue = itemToChange.getOrderNum() - (itemIndexInGrp - 1) * spreadKoef + itemIndexInGrp + grpSize * spreadKoef;
                    break;
            }
            itemToChange.setOrderNum(newValue);
            //System.out.print("after " + itemToChange.getOrderNum() + " |\n");

        }
    }

    @Override
    public String getCaption() {
        return "";
    }

    /**
     * Iterate over items and set orderNum value from 1 to size()+1;
     */
    protected void normalizeEntityOrderNum() {
        long normalizedIdx = 0;
        List<OrderableEntity> allItems = new ArrayList(listComponent.getDatasource().getItems());
        for (OrderableEntity item : allItems) {
            item.setOrderNum(++normalizedIdx); //first must to be 1
        }
    }

    public enum Direction {
        UP, DOWN
    }
}
