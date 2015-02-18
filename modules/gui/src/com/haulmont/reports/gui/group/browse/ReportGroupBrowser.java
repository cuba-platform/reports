/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.group.browse;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractLookup;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.ListComponent;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportGroup;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * @author artamonov
 * @version $Id$
 */
public class ReportGroupBrowser extends AbstractLookup {

    @Inject
    protected Table reportGroupsTable;

    @Named("reportGroupsTable.create")
    protected CreateAction createAction;

    @Named("reportGroupsTable.edit")
    protected EditAction editAction;

    @Inject
    protected Metadata metadata;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        createAction.setOpenType(WindowManager.OpenType.DIALOG);
        editAction.setOpenType(WindowManager.OpenType.DIALOG);

        reportGroupsTable.addAction(new RemoveReportGroupAction(reportGroupsTable));
    }

    protected class RemoveReportGroupAction extends RemoveAction {

        public RemoveReportGroupAction(ListComponent owner) {
            super(owner);
        }

        @Override
        public void actionPerform(Component component) {
            if (!isEnabled()) {
                return;
            }

            ReportGroup group = getTargetSingleSelected();
            if (group != null) {
                if (group.getSystemFlag()) {
                    showNotification(getMessage("unableToDeleteSystemReportGroup"), NotificationType.WARNING);
                } else {
                    MetaClass reportMetaClass = metadata.getSession().getClass(Report.class);
                    LoadContext loadContext = new LoadContext(reportMetaClass);
                    loadContext.setView("report.view");
                    LoadContext.Query query =
                            new LoadContext.Query("select r from report$Report r where r.group.id = :groupId");
                    query.setMaxResults(1);
                    query.setParameter("groupId", group.getId());
                    loadContext.setQuery(query);

                    DataSupplier dataService = getDsContext().getDataSupplier();
                    Report report = dataService.load(loadContext);
                    if (report != null) {
                        showNotification(getMessage("unableToDeleteNotEmptyReportGroup"), NotificationType.WARNING);
                    } else {
                        super.actionPerform(component);
                    }
                }
            }
        }
    }
}