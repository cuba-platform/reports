/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.report.wizard.region;

import com.haulmont.cuba.gui.components.AbstractLookup;
import com.haulmont.cuba.gui.components.Tree;
import com.haulmont.cuba.gui.data.impl.AbstractTreeDatasource;
import com.haulmont.reports.entity.wizard.EntityTreeNode;

import javax.inject.Named;
import java.util.Map;

public class EntityTreeLookup extends AbstractLookup {

    @Named("entityTreeFrame.reportEntityTreeNodeDs")
    protected AbstractTreeDatasource reportEntityTreeNodeDs;
    @Named("entityTreeFrame.entityTree")
    protected Tree entityTree;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        reportEntityTreeNodeDs.refresh(params);
        entityTree.expandTree();
        this.setLookupValidator(new Validator() {

            @Override
            public boolean validate() {
                if (entityTree.getSingleSelected() == null) {
                    showNotification(getMessage("selectItemForContinue"), NotificationType.TRAY);
                    return false;
                } else {
                    if (((EntityTreeNode) entityTree.getSingleSelected()).getParent() == null) {
                        showNotification(getMessage("selectNotARoot"), NotificationType.TRAY);
                        return false;
                    }
                }
                return true;
            }
        });
    }

    @Override
    protected boolean preClose(String actionId) {

        return super.preClose(actionId);
    }
}