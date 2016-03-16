/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.entity.wizard;

import com.haulmont.cuba.core.entity.Entity;

import java.util.UUID;

/**
 * Interface to be implemented by some entities that order display can be changed in UI.
 * That order might to be set by {@link  com.haulmont.reports.gui.components.actions.OrderableItemMoveAction}
 *
 */


public interface OrderableEntity extends Entity<UUID> {
    Long getOrderNum();

    void setOrderNum(Long orderNum);
}