/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.entity.wizard;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;

import javax.persistence.Transient;

/**
 * Immutable property class
 *
 * @author fedorchenko
 * @version $Id$
 */
@MetaClass(name = "report$WizardReportRegionProperty")
@SystemLevel
public class RegionProperty extends AbstractNotPersistentEntity implements OrderableEntity {
    @MetaProperty
    @Transient
    protected EntityTreeNode entityTreeNode;
    @MetaProperty
    @Transient
    protected Long orderNum;

    public EntityTreeNode getEntityTreeNode() {
        return entityTreeNode;
    }

    public void setEntityTreeNode(EntityTreeNode entityTreeNode) {
        this.entityTreeNode = entityTreeNode;
    }

    @Override
    public Long getOrderNum() {
        return orderNum;
    }

    @Override
    public void setOrderNum(Long orderNum) {
        this.orderNum = orderNum;
    }

    @MetaProperty
    @Transient
    public String getName() {
        return entityTreeNode.getName();
    }

    @MetaProperty
    @Transient
    public String getLocalizedName() {
        return entityTreeNode.getLocalizedName();
    }

    @MetaProperty
    @Transient
    public String getHierarchicalName() {
        return entityTreeNode.getHierarchicalName();
    }

    @MetaProperty
    @Transient
    public String getHierarchicalNameExceptRoot() {
        return entityTreeNode.getHierarchicalNameExceptRoot();
    }

    @MetaProperty
    @Transient
    public String getHierarchicalLocalizedName() {
        return entityTreeNode.getHierarchicalLocalizedName();
    }

    @MetaProperty
    @Transient
    public String getHierarchicalLocalizedNameExceptRoot() {
        return entityTreeNode.getHierarchicalLocalizedNameExceptRoot();
    }
}


