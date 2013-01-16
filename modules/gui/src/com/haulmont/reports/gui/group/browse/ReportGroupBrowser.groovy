/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports.gui.group.browse

import com.haulmont.cuba.core.global.LoadContext
import com.haulmont.cuba.core.global.MetadataProvider
import com.haulmont.cuba.gui.WindowManager.OpenType
import com.haulmont.cuba.gui.components.AbstractLookup
import com.haulmont.cuba.gui.components.Component
import com.haulmont.cuba.gui.components.IFrame
import com.haulmont.cuba.gui.components.Table
import com.haulmont.cuba.gui.components.actions.CreateAction
import com.haulmont.cuba.gui.components.actions.EditAction
import com.haulmont.cuba.gui.components.actions.RemoveAction
import com.haulmont.cuba.gui.data.DataSupplier
import com.haulmont.reports.entity.Report
import com.haulmont.reports.entity.ReportGroup

/**
 * @author artamonov
 * @version $Id$
 */
class ReportGroupBrowser extends AbstractLookup {

    @Override
    void init(Map<String, Object> params) {
        super.init(params)

        Table table = getComponent('table');
        table.addAction(new CreateAction(table, OpenType.DIALOG))
        table.addAction(new EditAction(table, OpenType.DIALOG))
        table.addAction(new RemoveAction(table) {
            @Override
            void actionPerform(Component component) {
                if (!isEnabled()) return;
                final Set selected = table.getSelected();
                if (!selected.isEmpty()) {
                    ReportGroup group = table.getSingleSelected()
                    if (group.getSystemFlag())
                        showNotification(getMessage('unableToDeleteSystemReportGroup'),
                                IFrame.NotificationType.WARNING)
                    else {
                        def reportMetaClass = MetadataProvider.getSession().getClass(Report.class)
                        LoadContext loadContext = new LoadContext(reportMetaClass)
                        loadContext.setView('report.view')
                        LoadContext.Query query = new LoadContext.Query(
                                'select r from report$Report r where r.group.id = :groupId')
                        query.setMaxResults(1)
                        query.addParameter('groupId', group.getId())
                        loadContext.setQuery(query)

                        DataSupplier dataService = getDsContext().getDataService()
                        Report report = dataService.load(loadContext)
                        if (report != null) {
                            showNotification(getMessage('unableToDeleteNotEmptyReportGroup'),
                                    IFrame.NotificationType.WARNING)
                        } else
                            super.actionPerform(component)
                    }
                }
            }
        })
    }
}
