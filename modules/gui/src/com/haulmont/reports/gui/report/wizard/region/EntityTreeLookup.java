/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.reports.gui.report.wizard.region;

import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.impl.AbstractTreeDatasource;
import com.haulmont.reports.entity.wizard.EntityTreeNode;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

public class EntityTreeLookup extends AbstractLookup {

    @Named("entityTreeFrame.reportEntityTreeNodeDs")
    protected AbstractTreeDatasource reportEntityTreeNodeDs;
    @Named("entityTreeFrame.entityTree")
    protected Tree entityTree;
    @Named("entityTreeFrame.reportPropertyName")
    protected TextField<String> reportPropertyName;
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
        this.setLookupValidator(() -> {
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
        });

        Action search = new AbstractAction("search", configuration.getConfig(ClientConfig.class).getFilterApplyShortcut()) {
            @Override
            public void actionPerform(Component component) {
                reportEntityTreeNodeDs.refresh();
                if (!reportEntityTreeNodeDs.getItemIds().isEmpty()) {
                    if (StringUtils.isEmpty(reportPropertyName.getValue())) {
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