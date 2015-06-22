/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.definition.edit;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.global.ViewProperty;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.reports.app.EntityTree;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.reports.entity.DataSetType;
import com.haulmont.reports.entity.wizard.ReportRegion;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $Id$
 */
class EditViewAction extends AbstractAction {
    private BandDefinitionEditor bandDefinitionEditor;

    public EditViewAction(BandDefinitionEditor bandDefinitionEditor) {
        super("editView");
        this.bandDefinitionEditor = bandDefinitionEditor;
    }

    @Override
    public void actionPerform(Component component) {
        if (bandDefinitionEditor.dataSets.getSingleSelected() instanceof DataSet) {
            final DataSet dataSet = bandDefinitionEditor.dataSets.getSingleSelected();
            if (dataSet != null && (DataSetType.SINGLE == dataSet.getType() || DataSetType.MULTI == dataSet.getType())) {
                MetaClass forEntityTreeModelMetaClass = findMetaClassByAlias(dataSet);
                if (forEntityTreeModelMetaClass != null) {

                    final EntityTree entityTree = bandDefinitionEditor.reportWizardService.buildEntityTree(forEntityTreeModelMetaClass);
                    ReportRegion reportRegion = dataSetToReportRegion(dataSet, entityTree);

                    if (reportRegion != null) {
                        if (reportRegion.getRegionPropertiesRootNode() == null) {
                            bandDefinitionEditor.showNotification(
                                    bandDefinitionEditor.formatMessage("dataSet.entityAliasInvalid",
                                            getNameForEntityParameter(dataSet)), IFrame.NotificationType.TRAY);
                            //without that root node region editor form will not initialized correctly and became empty. just return
                            return;
                        } else {
                            //Open editor and convert saved in editor ReportRegion item to View
                            Map<String, Object> editorParams = new HashMap<>();
                            editorParams.put("asViewEditor", Boolean.TRUE);
                            editorParams.put("rootEntity", reportRegion.getRegionPropertiesRootNode());
                            editorParams.put("scalarOnly", Boolean.TRUE);

                            final Window.Editor regionEditor =
                                    bandDefinitionEditor.openEditor("report$Report.regionEditor",
                                            reportRegion, WindowManager.OpenType.DIALOG, editorParams, bandDefinitionEditor.dataSetsDs);
                            regionEditor.addListener(new Window.CloseListener() {
                                @Override
                                public void windowClosed(String actionId) {
                                    if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                                        dataSet.setView(reportRegionToView(entityTree, (ReportRegion) regionEditor.getItem()));
                                    }
                                }
                            });
                        }

                    }

                }
            }
        }
    }

    //Detect metaclass by an alias and parameter
    protected MetaClass findMetaClassByAlias(DataSet dataSet) {
        String dataSetAlias = getNameForEntityParameter(dataSet);

        MetaClass byAliasMetaClass = bandDefinitionEditor.reportService.findMetaClassByDataSetEntityAlias(dataSetAlias, dataSet.getType(),
                bandDefinitionEditor.bandDefinitionDs.getItem().getReport().getInputParameters());

        //Lets return some value
        if (byAliasMetaClass == null) {
            //Can`t determine parameter and its metaClass by alias
            bandDefinitionEditor.showNotification(
                    bandDefinitionEditor.formatMessage("dataSet.entityAliasInvalid", dataSetAlias), IFrame.NotificationType.TRAY);
            return null;
            //when byAliasMetaClass is null we return also null
        } else {
            //Detect metaclass by current view for comparison
            MetaClass viewMetaClass = null;
            if (dataSet.getView() != null) {
                viewMetaClass = bandDefinitionEditor.metadata.getClass(dataSet.getView().getEntityClass());
            }
            if (viewMetaClass != null && !byAliasMetaClass.getName().equals(viewMetaClass.getName())) {
                bandDefinitionEditor.showNotification(
                        bandDefinitionEditor.formatMessage("dataSet.entityWasChanged",
                                byAliasMetaClass.getName()), IFrame.NotificationType.TRAY);
            }
            return byAliasMetaClass;
        }
    }

    protected ReportRegion dataSetToReportRegion(DataSet dataSet, EntityTree entityTree) {
        boolean isTabulatedRegion;
        View view = null;
        String collectionPropertyName;
        switch (dataSet.getType()) {
            case SINGLE:
                isTabulatedRegion = false;
                view = dataSet.getView();
                collectionPropertyName = null;
                break;
            case MULTI:
                isTabulatedRegion = true;
                collectionPropertyName = StringUtils.substringAfter(dataSet.getListEntitiesParamName(), "#");
                if (StringUtils.isBlank(collectionPropertyName) && dataSet.getListEntitiesParamName().contains("#")) {
                    bandDefinitionEditor.showNotification(
                            bandDefinitionEditor.formatMessage("dataSet.entityAliasInvalid",
                                    getNameForEntityParameter(dataSet)), IFrame.NotificationType.TRAY);
                    return null;
                }
                if (StringUtils.isNotBlank(collectionPropertyName)) {

                    if (dataSet.getView() != null) {
                        view = findSubViewByCollectionPropertyName(dataSet.getView(), collectionPropertyName);

                    }
                    if (view == null) {
                        //View was never created for current dataset.
                        //We must to create minimal view that contains collection property for ability of creating ReportRegion.regionPropertiesRootNode later
                        MetaClass metaClass = entityTree.getEntityTreeRootNode().getWrappedMetaClass();
                        MetaProperty metaProperty = metaClass.getProperty(collectionPropertyName);
                        if (metaProperty != null && metaProperty.getDomain() != null && metaProperty.getRange().getCardinality().isMany()) {
                            view = new View(metaProperty.getDomain().getJavaClass());
                        } else {
                            bandDefinitionEditor.showNotification(
                                    bandDefinitionEditor.formatMessage("dataSet.cantFindCollectionProperty",
                                            collectionPropertyName, metaClass.getName()), IFrame.NotificationType.TRAY);
                            return null;
                        }
                    }
                } else {
                    view = dataSet.getView();
                }
                break;
            default:
                return null;
        }
        return bandDefinitionEditor.reportWizardService.createReportRegionByView(entityTree, isTabulatedRegion,
                view, collectionPropertyName);
    }

    protected View reportRegionToView(EntityTree entityTree, ReportRegion reportRegion) {
        return bandDefinitionEditor.reportWizardService.createViewByReportRegions(entityTree.getEntityTreeRootNode(), Collections.singletonList(reportRegion));
    }

    public View findSubViewByCollectionPropertyName(View view, final String propertyName) {
        if (view == null) {
            return null;
        }
        for (ViewProperty viewProperty : view.getProperties()) {
            if (propertyName.equals(viewProperty.getName())) {
                if (viewProperty.getView() != null) {
                    return viewProperty.getView();
                }
            }

            if (viewProperty.getView() != null) {
                View foundedView = findSubViewByCollectionPropertyName(viewProperty.getView(), propertyName);
                if (foundedView != null) {
                    return foundedView;
                }
            }
        }
        return null;
    }

    protected String getNameForEntityParameter(DataSet dataSet) {
        String dataSetAlias = null;
        switch (dataSet.getType()) {
            case SINGLE:
                dataSetAlias = dataSet.getEntityParamName();
                break;
            case MULTI:
                dataSetAlias = dataSet.getListEntitiesParamName();
                break;
        }
        return dataSetAlias;
    }
}
