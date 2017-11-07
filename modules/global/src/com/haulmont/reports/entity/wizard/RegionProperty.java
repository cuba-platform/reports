/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.entity.wizard;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;

import javax.persistence.Transient;

/**
 * Immutable property class
 *
 */
@MetaClass(name = "report$WizardReportRegionProperty")
@SystemLevel
public class RegionProperty extends BaseUuidEntity implements OrderableEntity {

    private static final long serialVersionUID = 8528946767216568803L;

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


