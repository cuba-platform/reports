/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.app;

import com.haulmont.reports.entity.wizard.EntityTreeNode;

import java.io.Serializable;

/**
 * @author fedorchenko
 * @version $Id$
 */
public class EntityTree implements Serializable{

    private static final long serialVersionUID = -7639009888440026734L;
    protected EntityTreeNode entityTreeRootNode;
    protected EntityTreeStructureInfo entityTreeStructureInfo;

    public EntityTreeNode getEntityTreeRootNode() {
        return entityTreeRootNode;
    }

    public void setEntityTreeRootNode(EntityTreeNode entityTreeRootNode) {
        this.entityTreeRootNode = entityTreeRootNode;
    }

    public EntityTreeStructureInfo getEntityTreeStructureInfo() {
        return entityTreeStructureInfo;
    }

    public void setEntityTreeStructureInfo(EntityTreeStructureInfo entityTreeStructureInfo) {
        this.entityTreeStructureInfo = entityTreeStructureInfo;
    }
}
