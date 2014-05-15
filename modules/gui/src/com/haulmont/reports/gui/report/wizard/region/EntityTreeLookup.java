/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.report.wizard.region;

import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.impl.AbstractTreeDatasource;
import com.haulmont.reports.entity.wizard.EntityTreeNode;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

public class EntityTreeLookup extends AbstractLookup {

    @Named("entityTreeFrame.reportEntityTreeNodeDs")
    protected AbstractTreeDatasource reportEntityTreeNodeDs;
    @Named("entityTreeFrame.entityTree")
    protected Tree entityTree;
    @Named("entityTreeFrame.reportPropertyName")
    protected TextField reportPropertyName;
    @Named("entityTreeFrame.reportPropertyNameSearchButton")
    protected Button reportPropertyNameSearchButton;

    @Inject
    protected Configuration configuration;

    protected EntityTreeNode rootNode;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        params.put("component$reportPropertyName", reportPropertyName);
        reportEntityTreeNodeDs.refresh(params);
        rootNode = (EntityTreeNode) params.get("rootEntity");
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
        Action search = new AbstractAction("search", configuration.getConfig(ClientConfig.class).getFilterApplyShortcut()) {
            @Override
            public void actionPerform(Component component) {
                reportEntityTreeNodeDs.refresh();
                if (!reportEntityTreeNodeDs.getItemIds().isEmpty()) {
                    if (StringUtils.isEmpty(reportPropertyName.<String>getValue())) {
                        entityTree.collapseTree();
                        entityTree.expand(rootNode.getId());
                    } else
                        entityTree.expandTree();
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

    @Override
    protected boolean preClose(String actionId) {

        return super.preClose(actionId);
    }
}