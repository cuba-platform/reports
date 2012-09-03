/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.cuba.web.ui.report.edit

import com.haulmont.chile.core.model.MetaPropertyPath
import com.haulmont.cuba.core.app.FileStorageService
import com.haulmont.cuba.core.entity.Entity
import com.haulmont.cuba.core.entity.FileDescriptor
import com.haulmont.cuba.gui.AppConfig
import com.haulmont.cuba.gui.ServiceLocator
import com.haulmont.cuba.gui.WindowManager
import com.haulmont.cuba.gui.WindowManager.OpenType
import com.haulmont.cuba.gui.components.actions.CreateAction
import com.haulmont.cuba.gui.components.actions.EditAction
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction
import com.haulmont.cuba.gui.components.actions.RemoveAction
import com.haulmont.cuba.gui.config.WindowConfig
import com.haulmont.cuba.gui.config.WindowInfo
import com.haulmont.cuba.gui.data.CollectionDatasource
import com.haulmont.cuba.gui.data.Datasource
import com.haulmont.cuba.gui.data.DsContext.CommitListener
import com.haulmont.cuba.gui.data.impl.CollectionPropertyDatasourceImpl
import com.haulmont.cuba.gui.data.impl.HierarchicalPropertyDatasourceImpl
import javax.inject.Inject
import org.apache.commons.lang.ObjectUtils
import org.apache.commons.lang.StringUtils
import com.haulmont.cuba.core.global.*
import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.report.*
import com.haulmont.cuba.gui.data.HierarchicalDatasource
import com.haulmont.cuba.security.entity.Role
import javax.inject.Named
import com.haulmont.cuba.gui.data.impl.DatasourceImpl

/**
 * @author degtyarjov
 * @version $Id$
 */
public class ReportEditor extends AbstractEditor {

    protected Report report

    protected deletedFiles = [:]

    @Named('generalFrame.serviceTree')
    protected Tree bandTree

    @Inject
    protected WindowConfig windowConfig;

    @Inject
    protected Datasource<Report> reportDs

    @Inject
    protected CollectionDatasource<ReportGroup, UUID> groupsDs

    @Inject
    protected CollectionDatasource<ReportInputParameter, UUID> parametersDs

    @Inject
    protected CollectionDatasource<ReportScreen, UUID> reportScreensDs

    @Inject
    protected CollectionDatasource<Role, UUID> rolesDs

    @Inject
    protected CollectionDatasource<Role, UUID> lookupRolesDs

    @Inject
    protected HierarchicalDatasource<BandDefinition, UUID> treeDs

    def ReportEditor(IFrame frame) {
        super(frame);
    }

    @Override
    def void setItem(Entity item) {
        Report report = (Report) item;
        BandDefinition rootDefinition = null

        if (PersistenceHelper.isNew(item)) {
            report.setReportType(ReportType.SIMPLE)

            rootDefinition = new BandDefinition()
            rootDefinition.setName('Root')
            report.setBands(new HashSet<BandDefinition>([rootDefinition]))

            groupsDs.refresh()
            if (groupsDs.getItemIds() != null) {
                def id = groupsDs.getItemIds().iterator().next()
                report.setGroup((ReportGroup) groupsDs.getItem(id))
            }
        }
        if (!StringUtils.isEmpty(report.name)) {
            caption = MessageProvider.formatMessage(getClass(), 'reportEditor.format', report.name)
        }

        super.setItem(item);
        this.report = (Report) getItem();

        if (PersistenceHelper.isNew(item))
            rootDefinition.setReport(report)

        ((CollectionPropertyDatasourceImpl) treeDs).setModified(false)
        ((DatasourceImpl) reportDs).setModified(false)

        bandTree.datasource.refresh()
        bandTree.expandTree()
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        initGeneral()
        initTemplates()
        initParameters()
        initRoles()
        initScreens()
        initValuesFormats()

        getDsContext().addListener(new CommitListener() {
            @Override
            void beforeCommit(CommitContext context) {
                List<FileDescriptor> fileDescriptors = new ArrayList<FileDescriptor>()
                // delete descriptors from db
                // persist related file descriptors
                for (Entity entity: context.commitInstances) {
                    if (ReportTemplate.isInstance(entity)) {
                        java.util.List deletedFilesList = (java.util.List) deletedFiles.get(entity)
                        if ((deletedFilesList != null) && (deletedFilesList.size() > 0)) {
                            context.removeInstances.add((Entity) deletedFilesList.get(0))
                        }
                        ReportTemplate template = (ReportTemplate) entity
                        if (template.getTemplateFileDescriptor() != null)
                            fileDescriptors.add(template.getTemplateFileDescriptor())
                    }
                }
                context.commitInstances.addAll(fileDescriptors)
            }

            @Override
            void afterCommit(CommitContext context, Set<Entity> result) {
                FileStorageService storageService = ServiceLocator.lookup(FileStorageService.NAME)

                for (Entity entity: context.commitInstances) {
                    if (ReportTemplate.isInstance(entity) && result.contains(entity)) {
                        java.util.List deletedFilesList = (java.util.List) deletedFiles.get(entity)
                        for (FileDescriptor fileDescriptor: deletedFilesList) {
                            removeQuietly(storageService, fileDescriptor)
                        }
                    }
                }

                for (Entity entity: context.removeInstances) {
                    if (ReportTemplate.isInstance(entity) && result.contains(entity)) {
                        java.util.List deletedFilesList = (java.util.List) deletedFiles.get(entity)
                        for (FileDescriptor fileDescriptor: deletedFilesList) {
                            removeQuietly(storageService, fileDescriptor)
                        }
                        ReportTemplate template = (ReportTemplate) entity
                        removeQuietly(storageService, template.templateFileDescriptor)
                    }
                }
            }

            private void removeQuietly(storageService, fileDescriptor) {
                try {
                    storageService.removeFile(fileDescriptor)
                } catch (FileStorageException ignored) { }
            }
        })
    }

    private def initParameters() {
        com.haulmont.chile.core.model.MetaClass metaClass = MetadataProvider.getSession().getClass(ReportInputParameter.class)
        MetaPropertyPath mpp = new MetaPropertyPath(metaClass, metaClass.getProperty('position'))

        Table parametersTable = getComponent('generalFrame.parametersFrame.inputParametersTable')
        parametersTable.addAction(
                new CreateAction(parametersTable, WindowManager.OpenType.DIALOG) {
                    @Override
                    public Map<String, Object> getInitialValues() {
                        return new HashMap(['position': parametersDs.itemIds.size(), 'report': report])
                    }
                }
        )
        parametersTable.addAction(new RemoveAction(parametersTable, false));
        parametersTable.addAction(new EditAction(parametersTable, WindowManager.OpenType.DIALOG));

        Button upButton = getComponent('generalFrame.parametersFrame.up')
        Button downButton = getComponent('generalFrame.parametersFrame.down')

        upButton.action = new ItemTrackingAction('generalFrame.up') {
            @Override
            void actionPerform(Component component) {
                ReportInputParameter parameter = (ReportInputParameter) parametersDs.getItem()
                if (parameter) {
                    Collection parametersList = report.getInputParameters()
                    int index = parameter.position
                    if (index > 0) {
                        ReportInputParameter previousParameter = null
                        for (ReportInputParameter _param: parametersList) {
                            if (_param.position == index - 1) {
                                previousParameter = _param;
                                break;
                            }
                        }
                        if (previousParameter) {
                            parameter.position = previousParameter.position
                            previousParameter.position = index
                            parametersTable.sortBy(mpp, true)
                        }
                    }
                }
            }

            @Override
            void itemChanged(Datasource ds, Entity prevItem, Entity item) {
                super.itemChanged(ds, prevItem, item)
                turnEnabled(item)
            }

            @Override
            void valueChanged(Object source, String property, Object prevValue, Object value) {
                if ('position'.equals(property))
                    turnEnabled(parametersDs.item)
            }

            def turnEnabled(Entity item) {
                ReportInputParameter parameter = (ReportInputParameter) item;
                setEnabled(item != null && parameter.position > 0)
            }
        }

        downButton.action = new ItemTrackingAction('generalFrame.down') {
            @Override
            void actionPerform(Component component) {
                ReportInputParameter parameter = (ReportInputParameter) parametersDs.getItem()
                if (parameter) {
                    Collection parametersList = report.getInputParameters()
                    int index = parameter.position
                    if (index < parametersDs.itemIds.size() - 1) {
                        ReportInputParameter nextParameter = null
                        for (ReportInputParameter _param: parametersList) {
                            if (_param.position == index + 1) {
                                nextParameter = _param;
                                break;
                            }
                        }
                        if (nextParameter) {
                            parameter.position = nextParameter.position
                            nextParameter.position = index
                            parametersTable.sortBy(mpp, true)
                        }
                    }
                }
            }

            @Override
            void itemChanged(Datasource ds, Entity prevItem, Entity item) {
                super.itemChanged(ds, prevItem, item)
                turnEnabled(item)
            }

            @Override
            void valueChanged(Object source, String property, Object prevValue, Object value) {
                if ('position'.equals(property))
                    turnEnabled(parametersDs.item)
            }

            def turnEnabled(Entity item) {
                ReportInputParameter parameter = (ReportInputParameter) item;
                setEnabled(item != null && parameter.position < parametersDs.size() - 1)
            }
        }

        parametersTable.addAction(upButton.action)
        parametersTable.addAction(downButton.action)
    }

    private def initValuesFormats() {
        Table formatsTable = getComponent('generalFrame.formatsFrame.valuesFormatsTable')

        formatsTable.addAction(
                new CreateAction(formatsTable, WindowManager.OpenType.DIALOG) {
                    @Override
                    public Map<String, Object> getInitialValues() {
                        return new HashMap(['report': report])
                    }
                }
        )
        formatsTable.addAction(new RemoveAction(formatsTable, false))
        formatsTable.addAction(new EditAction(formatsTable, WindowManager.OpenType.DIALOG))
    }

    private def initRoles() {
        Table rolesTable = getComponent('securityFrame.rolesTable')
        rolesTable.addAction(new RemoveAction(rolesTable, false))

        Button addRoleBtn = getComponent('securityFrame.addRoleBtn')
        addRoleBtn.setAction(new AbstractAction('actions.Add') {
            @Override
            void actionPerform(Component component) {
                if (lookupRolesDs.item) {
                    def existingRole = rolesDs.itemIds.find(
                            { id -> ObjectUtils.equals(lookupRolesDs.item, rolesDs.getItem(id)) }
                    )
                    if (!existingRole)
                        rolesDs.addItem(lookupRolesDs.item)
                }
            }
        })
    }

    private def initScreens() {
        Table screenTable = getComponent('securityFrame.screenTable')
        screenTable.addAction(new RemoveAction(screenTable, false))

        final LookupField screenIdLookup = getComponent('securityFrame.screenIdLookup')
        List<WindowInfo> windowInfoCollection = new ArrayList<WindowInfo>(windowConfig.getWindows());
        // sort by screenId
        Collections.sort(windowInfoCollection, new Comparator<WindowInfo>() {
            @Override
            public int compare(WindowInfo w1, WindowInfo w2) {
                int w1DollarIndex = w1.getId().indexOf('$');
                int w2DollarIndex = w2.getId().indexOf('$');

                if ((w1DollarIndex > 0 && w2DollarIndex > 0) || (w1DollarIndex < 0 && w2DollarIndex < 0)) {
                    return w1.getId().compareTo(w2.getId());
                } else if (w1DollarIndex > 0)
                    return -1;
                else
                    return 1;
            }
        });

        Map<String, Object> screens = new LinkedHashMap<String, Object>();
        for (WindowInfo windowInfo: windowInfoCollection) {
            String id = windowInfo.getId();
            String menuId = 'menu-config.' + id;
            String localeMsg = MessageProvider.getMessage(AppConfig.getMessagesPack(), menuId);
            String title = menuId.equals(localeMsg) ? id : id + ' ( ' + localeMsg + ' )';
            screens.put(title, id);
        }
        screenIdLookup.setOptionsMap(screens);

        Button addReportScreenBtn = getComponent('securityFrame.addReportScreenBtn')
        addReportScreenBtn.setAction(new AbstractAction('actions.Add') {
            @Override
            void actionPerform(Component component) {
                if (screenIdLookup.getValue() != null) {
                    def screenId = (String) screenIdLookup.getValue()

                    def existingReportScreen = reportScreensDs.getItemIds().find(
                            { id -> StringUtils.equals(reportScreensDs.getItem(id).screenId, screenId) }
                    )
                    if (!existingReportScreen) {
                        ReportScreen reportScreen = new ReportScreen()
                        reportScreen.setReport((Report) getItem())
                        reportScreen.setScreenId(screenId)
                        reportScreensDs.addItem(reportScreen)
                    }
                }
            }
        })
    }

    private def initGeneral() {
        Button createBandDefinitionButton = getComponent('generalFrame.createBandDefinition')
        Button editBandDefinitionButton = getComponent('generalFrame.editBandDefinition')
        Button removeBandDefinitionButton = getComponent('generalFrame.removeBandDefinition')
        Button upButton = getComponent('generalFrame.up')
        Button downButton = getComponent('generalFrame.down')

        ((HierarchicalPropertyDatasourceImpl) treeDs).setSortPropertyName('position')

        Tree tree = getComponent('generalFrame.serviceTree')

        createBandDefinitionButton.action = new CreateAction(tree, OpenType.THIS_TAB, 'generalFrame.editBandDefinition') {

            @Override
            public Map<String, Object> getInitialValues() {
                BandDefinition parentDefinition = (BandDefinition) treeDs.getItem()
                Report report = (Report) getItem()
                // Use root band as parent if no items selected
                if (parentDefinition == null)
                    parentDefinition = report.getRootBandDefinition()
                return (Map<String, Object>) [
                        'parentBandDefinition': parentDefinition,
                        'position': parentDefinition.childrenBandDefinitions != null ?
                            parentDefinition.childrenBandDefinitions.size() : 0,
                        'report': report
                ]
            }

            @Override
            protected void afterWindowClosed(Window window) {
                treeDs.refresh()
            }
        }

        editBandDefinitionButton.action = new EditAction(bandTree, OpenType.THIS_TAB, 'generalFrame.editBandDefinition')

        removeBandDefinitionButton.action = new RemoveAction(bandTree, false, 'generalFrame.removeBandDefinition') {
            @Override
            void itemChanged(Datasource ds, Entity prevItem, Entity item) {
                super.itemChanged(ds, prevItem, item)
                if (isEnabled() && item != null) {
                    Report report = (Report) getItem()
                    setEnabled(!ObjectUtils.equals(report.rootBandDefinition, item))
                }
            }

            @Override
            protected void doRemove(Set selected, boolean autocommit) {
                if (selected) {
                    removeChildrenCascade(selected)

                    if (this.autocommit) {
                        try {
                            treeDs.commit();
                        } catch (RuntimeException e) {
                            treeDs.refresh();
                            throw e;
                        }
                    }
                }
            }

            private void removeChildrenCascade(Collection selected) {
                for (Object o: selected) {
                    BandDefinition definition = (BandDefinition) o;
                    if (definition.childrenBandDefinitions) {
                        removeChildrenCascade(definition.childrenBandDefinitions)
                    }
                    treeDs.removeItem(definition)
                }
            }
        }

        upButton.action = new ItemTrackingAction('generalFrame.up') {
            @Override
            void actionPerform(Component component) {
                BandDefinition definition = (BandDefinition) treeDs.getItem()
                if (definition && definition.getParentBandDefinition()) {
                    BandDefinition parentDefinition = definition.getParentBandDefinition();
                    java.util.List definitionsList = parentDefinition.getChildrenBandDefinitions()
                    int index = definitionsList.indexOf(definition);
                    if (index > 0) {
                        BandDefinition previousDefinition = definitionsList.get(index - 1)
                        definition.position = definition.position - 1
                        previousDefinition.position = previousDefinition.position + 1

                        definitionsList.set(index, previousDefinition)
                        definitionsList.set(index - 1, definition)

                        treeDs.refresh()
                    }
                }
            }

            @Override
            void valueChanged(Object source, String property, Object prevValue, Object value) {
                if ('position'.equals(property))
                    turnEnabled(treeDs.item)
            }

            @Override
            void itemChanged(Datasource ds, Entity prevItem, Entity item) {
                super.itemChanged(ds, prevItem, item)
                turnEnabled(item)
            }

            private def turnEnabled(Entity item) {
                BandDefinition bandDefinition = (BandDefinition) item;
                setEnabled(bandDefinition != null && bandDefinition.position > 0);
            }
        }

        downButton.action = new ItemTrackingAction('generalFrame.down') {
            @Override
            void actionPerform(Component component) {
                BandDefinition definition = (BandDefinition) treeDs.getItem()
                if (definition && definition.getParentBandDefinition()) {
                    BandDefinition parentDefinition = definition.getParentBandDefinition();
                    java.util.List definitionsList = parentDefinition.getChildrenBandDefinitions()
                    int index = definitionsList.indexOf(definition);
                    if (index < definitionsList.size() - 1) {
                        BandDefinition nextDefinition = definitionsList.get(index + 1)
                        definition.position = definition.position + 1
                        nextDefinition.position = nextDefinition.position - 1

                        definitionsList.set(index, nextDefinition)
                        definitionsList.set(index + 1, definition)

                        treeDs.refresh()
                    }
                }
            }

            @Override
            void valueChanged(Object source, String property, Object prevValue, Object value) {
                if ('position'.equals(property))
                    turnEnabled(treeDs.item)
            }

            @Override
            void itemChanged(Datasource ds, Entity prevItem, Entity item) {
                super.itemChanged(ds, prevItem, item)
                turnEnabled(item)
            }

            private def turnEnabled(Entity item) {
                BandDefinition bandDefinition = (BandDefinition) item;
                if (bandDefinition != null) {
                    def parent = bandDefinition.parentBandDefinition
                    setEnabled(parent != null &&
                            parent.childrenBandDefinitions != null &&
                            bandDefinition.position < parent.childrenBandDefinitions.size() - 1);
                }
            }
        }

        bandTree.addAction(createBandDefinitionButton.action)
        bandTree.addAction(editBandDefinitionButton.action)
        bandTree.addAction(removeBandDefinitionButton.action)
        bandTree.addAction(upButton.action)
        bandTree.addAction(downButton.action)
    }

    private def initTemplates() {
        Table templatesTable = getComponent('generalFrame.templatesTable')
        templatesTable.addAction(new CreateAction(templatesTable, OpenType.DIALOG) {
            @Override
            public Map<String, Object> getInitialValues() {
                return new HashMap(['report': report])
            }

            @Override
            public Map<String, Object> getWindowParams() {
                return new HashMap(['deletedContainer': deletedFiles])
            }
        });
        templatesTable.addAction(new EditAction(templatesTable, OpenType.DIALOG) {
            @Override
            public Map<String, Object> getWindowParams() {
                return new HashMap(['deletedContainer': deletedFiles])
            }
        });
        templatesTable.addAction(new RemoveAction(templatesTable, false));

        Button defaultTemplateBtn = getComponent('generalFrame.defaultTemplateBtn')
        defaultTemplateBtn.action = new ItemTrackingAction('report.defaultTemplate') {

            @Override
            void actionPerform(Component component) {
                ReportTemplate template = templatesTable.getSingleSelected()
                if ((template != null) && !template.getDefaultFlag()) {
                    template.setDefaultFlag(true)
                    Collection itemIds = templatesTable.getDatasource().getItemIds()
                    for (id in itemIds) {
                        ReportTemplate temp = (ReportTemplate) templatesTable.getDatasource().getItem(id)
                        if (!template.equals(temp) && (temp.getDefaultFlag()))
                            temp.setDefaultFlag(false)
                    }
                    templatesTable.refresh();
                }
            }
        };

        templatesTable.addAction(defaultTemplateBtn.action)
    }

    @Override
    public void commitAndClose() {
        if (PersistenceHelper.isNew(report)) {
            ((CollectionPropertyDatasourceImpl) treeDs).setModified(true)
        }
        super.commitAndClose();
    }
}