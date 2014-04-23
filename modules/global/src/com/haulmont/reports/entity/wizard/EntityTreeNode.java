/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.entity.wizard;

import com.haulmont.chile.core.annotations.Composition;
import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fedorchenko
 * @version $Id$
 */
@MetaClass(name = "report$WizardReportEntityTreeNode")
@SystemLevel
public class EntityTreeNode extends AbstractNotPersistentEntity {
    @MetaProperty
    @Transient
    protected String name;
    @MetaProperty
    @Transient
    protected String localizedName;
    @MetaProperty
    @Transient
    protected EntityTreeNode parent;
    @MetaProperty
    @Composition
    @Transient
    protected List<EntityTreeNode> children = new ArrayList<>();
    @Transient
    protected com.haulmont.chile.core.model.MetaClass wrappedMetaClass;//'wrappedMetaClass' name cause 'metaClass' field already exists in superclass
    @Transient
    protected com.haulmont.chile.core.model.MetaProperty wrappedMetaProperty;

    @MetaProperty
    @Transient
    public Integer getNodeDepth() {
        if (getParent() == null) {
            return 1;
        } else {
            return getParent().getNodeDepth() + 1;
        }
    }

    /**
     * Calculates depth of child nodes. Can to be used in sorting
     * @return
     */
    @MetaProperty
    @Transient
    public Integer getNodeChildrenDepth() {
        if (getChildren().isEmpty()) {
            return getNodeDepth();
        } else {
            int maxDepth = 0;
            for (EntityTreeNode entityTreeNode : getChildren()) {
                int depthOfChildren = entityTreeNode.getNodeChildrenDepth();
                if (maxDepth < depthOfChildren) {
                    maxDepth = depthOfChildren;
                }
            }
            return maxDepth;
        }
    }

    @MetaProperty
    @Transient
    public String getHierarchicalName() {
        if (getParent() == null) {
            return name;
        } else {
            return getParent().getHierarchicalName() + "." + name;
        }
    }

    @MetaProperty
    @Transient
    public String getHierarchicalLocalizedName() {
        if (getParent() == null) {
            return localizedName;
        } else {
            return getParent().getHierarchicalLocalizedName() + "." + localizedName;
        }
    }

    @MetaProperty
    @Transient
    public String getHierarchicalNameExceptRoot() {
        if (getParent() == null) {
            return "";
        } else {
            if (!"".equals(getParent().getHierarchicalNameExceptRoot())) {
                return getParent().getHierarchicalNameExceptRoot() + "." + name;
            } else {
                return name;
            }
        }
    }

    @MetaProperty
    @Transient
    public String getHierarchicalLocalizedNameExceptRoot() {
        if (getParent() == null) {
            return "";
        } else {
            if (!"".equals(getParent().getHierarchicalLocalizedNameExceptRoot())) {
                return getParent().getHierarchicalLocalizedNameExceptRoot() + "." + localizedName;
            } else {
                return localizedName;
            }
        }
    }

    /*public Boolean getIsAttribute() {
        return isAttribute;
    }

    public void setIsAttribute(Boolean isAttribute) {
        this.isAttribute = isAttribute;
    } */

    public String getLocalizedName() {
        return localizedName;
    }

    public void setLocalizedName(String localizedName) {
        this.localizedName = localizedName;
    }

    public String getName() {
        return name;
        /*if (isAttribute) {
            return wrappedMetaProperty.getFullName();
        } else {
            return wrappedMetaClass.getFullName();
        }*/
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityTreeNode getParent() {
        return parent;
    }

    public void setParent(EntityTreeNode parent) {
        this.parent = parent;
    }

    public List<EntityTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<EntityTreeNode> children) {
        this.children = children;
    }

    public com.haulmont.chile.core.model.MetaClass getWrappedMetaClass() {
        return wrappedMetaClass;
    }

    public void setWrappedMetaClass(com.haulmont.chile.core.model.MetaClass wrappedMetaClass) {
        this.wrappedMetaClass = wrappedMetaClass;
    }

    public com.haulmont.chile.core.model.MetaProperty getWrappedMetaProperty() {
        return wrappedMetaProperty;
    }

    public void setWrappedMetaProperty(com.haulmont.chile.core.model.MetaProperty wrappedMetaProperty) {
        this.wrappedMetaProperty = wrappedMetaProperty;
    }
}
