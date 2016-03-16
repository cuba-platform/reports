/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.app;

import java.io.Serializable;

/**
 * That class is used in report wizard creator cause that wizard need info about entity attributes
 *
 */
public class EntityTreeStructureInfo implements Serializable{
    private static final long serialVersionUID = -7636338880001636048L;
    protected boolean entityTreeHasSimpleAttrs;
    protected boolean entityTreeRootHasCollections;

    public boolean isEntityTreeHasSimpleAttrs() {
        return entityTreeHasSimpleAttrs;
    }

    public void setEntityTreeHasSimpleAttrs(boolean entityTreeHasSimpleAttrs) {
        this.entityTreeHasSimpleAttrs = entityTreeHasSimpleAttrs;
    }

    public boolean isEntityTreeRootHasCollections() {
        return entityTreeRootHasCollections;
    }

    public void setEntityTreeRootHasCollections(boolean entityTreeRootHasCollections) {
        this.entityTreeRootHasCollections = entityTreeRootHasCollections;
    }

}
