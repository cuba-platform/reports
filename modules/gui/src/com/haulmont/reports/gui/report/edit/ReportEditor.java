/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.gui.report.edit;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.cuba.core.app.FileStorageService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.app.core.file.FileUploadDialog;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import com.haulmont.cuba.gui.data.impl.CollectionPropertyDatasourceImpl;
import com.haulmont.cuba.gui.data.impl.DatasourceImpl;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.gui.data.impl.HierarchicalPropertyDatasourceImpl;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.*;
import com.haulmont.reports.gui.definition.edit.BandDefinitionEditor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class ReportEditor extends AbstractEditor<Report> {

    @Named("generalFrame.propertiesFieldGroup")
    protected FieldGroup propertiesFieldGroup;

    @Named("generalFrame.bandEditor")
    protected BandDefinitionEditor bandEditor;

    @Named("generalFrame.bandEditor.name")
    protected TextField bandName;

    @Named("generalFrame.bandEditor.orientation")
    protected LookupField bandOrientation;

    @Named("generalFrame.bandEditor.parentBand")
    protected LookupField parentBand;

    @Named("securityFrame.screenIdLookup")
    protected LookupField screenIdLookup;

    @Named("securityFrame.screenTable")
    protected Table screenTable;

    @Named("generalFrame.serviceTree")
    protected Tree tree;

    @Named("templatesFrame.templatesTable")
    protected Table templatesTable;

    @Named("saveAndRun")
    protected Button saveAndRun;

    @Named("generalFrame.createBandDefinition")
    protected Button createBandDefinitionButton;

    @Named("generalFrame.removeBandDefinition")
    protected Button removeBandDefinitionButton;

    @Named("generalFrame.up")
    protected Button bandUpButton;

    @Named("generalFrame.down")
    protected Button bandDownButton;

    @Named("securityFrame.addReportScreenBtn")
    protected Button addReportScreenBtn;

    @Named("securityFrame.addRoleBtn")
    protected Button addRoleBtn;

    @Named("securityFrame.rolesTable")
    protected Table rolesTable;

    @Named("parametersFrame.inputParametersTable")
    protected Table parametersTable;

    @Named("formatsFrame.valuesFormatsTable")
    Table formatsTable;

    @Named("parametersFrame.up")
    protected Button paramUpButton;

    @Named("parametersFrame.down")
    protected Button paramDownButton;

    @Named("generalFrame.serviceTree")
    protected Tree bandTree;

    @Inject
    protected WindowConfig windowConfig;

    @Inject
    protected Datasource<Report> reportDs;

    @Inject
    protected CollectionDatasource<ReportGroup, UUID> groupsDs;

    @Inject
    protected CollectionDatasource<ReportInputParameter, UUID> parametersDs;

    @Inject
    protected CollectionDatasource<ReportScreen, UUID> reportScreensDs;

    @Inject
    protected CollectionDatasource<Role, UUID> rolesDs;

    @Inject
    protected CollectionDatasource<Role, UUID> lookupRolesDs;

    @Inject
    protected CollectionDatasource<DataSet, UUID> dataSetsDs;

    @Inject
    protected HierarchicalDatasource<BandDefinition, UUID> treeDs;

    @Inject
    protected CollectionDatasource<ReportTemplate, UUID> templatesDs;

    @Inject
    protected FileStorageService fileStorageService;

    @Inject
    protected ComponentsFactory componentsFactory;

    @Inject
    protected FileUploadingAPI fileUpload;

    @Inject
    protected ReportService reportService;

    @Override
    protected void initItem(Report report) {
        if (PersistenceHelper.isNew(report)) {
            report.setReportType(ReportType.SIMPLE);

            BandDefinition rootDefinition = new BandDefinition();
            rootDefinition.setName("Root");
            rootDefinition.setPosition(0);
            report.setBands(new HashSet<BandDefinition>());
            report.getBands().add(rootDefinition);

            rootDefinition.setReport(report);

            groupsDs.refresh();
            if (groupsDs.getItemIds() != null) {
                UUID id = groupsDs.getItemIds().iterator().next();
                report.setGroup(groupsDs.getItem(id));
            }
        }
    }

    @Override
    protected void postInit() {
        if (!StringUtils.isEmpty(getItem().getName())) {
            setCaption(AppBeans.get(Messages.class).formatMessage(getClass(), "reportEditor.format", getItem().getName()));
        }

        ((CollectionPropertyDatasourceImpl) treeDs).setModified(false);
        ((DatasourceImpl) reportDs).setModified(false);

        bandTree.getDatasource().refresh();
        bandTree.expandTree();
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        initGeneral();
        initTemplates();
        initParameters();
        initRoles();
        initScreens();
        initValuesFormats();
    }

    protected void initParameters() {
        final MetaClass metaClass = AppBeans.get(Metadata.class).getSession().getClass(ReportInputParameter.class);
        final MetaPropertyPath mpp = new MetaPropertyPath(metaClass, metaClass.getProperty("position"));

        parametersTable.addAction(
                new CreateAction(parametersTable, OpenType.DIALOG) {
                    @Override
                    public Map<String, Object> getInitialValues() {
                        Map<String, Object> params = new HashMap<>();
                        params.put("position", parametersDs.getItemIds().size());
                        params.put("report", getItem());
                        return params;
                    }

                    @Override
                    public void actionPerform(Component component) {
                        orderParameters();
                        super.actionPerform(component);
                    }
                }
        );

        parametersTable.addAction(new RemoveAction(parametersTable, false) {
            @Override
            protected void afterRemove(Set selected) {
                super.afterRemove(selected);
                orderParameters();
            }
        });
        parametersTable.addAction(new EditAction(parametersTable, OpenType.DIALOG));

        paramUpButton.setAction(new ItemTrackingAction("generalFrame.up") {
            @Override
            public void actionPerform(Component component) {
                ReportInputParameter parameter = parametersDs.getItem();
                if (parameter != null) {
                    List<ReportInputParameter> inputParameters = getItem().getInputParameters();
                    int index = parameter.getPosition();
                    if (index > 0) {
                        ReportInputParameter previousParameter = null;
                        for (ReportInputParameter _param : inputParameters) {
                            if (_param.getPosition() == index - 1) {
                                previousParameter = _param;
                                break;
                            }
                        }
                        if (previousParameter != null) {
                            parameter.setPosition(previousParameter.getPosition());
                            previousParameter.setPosition(index);
                            parametersTable.sortBy(mpp, true);
                        }
                    }
                }
            }

            @Override
            public boolean isApplicableTo(Datasource.State state, Entity item) {
                return super.isApplicableTo(state, item) && ((ReportInputParameter) item).getPosition() > 0;
            }
        });

        paramDownButton.setAction(new ItemTrackingAction("generalFrame.down") {
            @Override
            public void actionPerform(Component component) {
                ReportInputParameter parameter = parametersDs.getItem();
                if (parameter != null) {
                    List<ReportInputParameter> inputParameters = getItem().getInputParameters();
                    int index = parameter.getPosition();
                    if (index < parametersDs.getItemIds().size() - 1) {
                        ReportInputParameter nextParameter = null;
                        for (ReportInputParameter _param : inputParameters) {
                            if (_param.getPosition() == index + 1) {
                                nextParameter = _param;
                                break;
                            }
                        }
                        if (nextParameter != null) {
                            parameter.setPosition(nextParameter.getPosition());
                            nextParameter.setPosition(index);
                            parametersTable.sortBy(mpp, true);
                        }
                    }
                }
            }

            @Override
            public boolean isApplicableTo(Datasource.State state, Entity item) {
                return super.isApplicableTo(state, item) &&
                        ((ReportInputParameter) item).getPosition() < parametersDs.size() - 1;
            }
        });

        parametersTable.addAction(paramUpButton.getAction());
        parametersTable.addAction(paramDownButton.getAction());
    }

    protected void initValuesFormats() {
        formatsTable.addAction(
                new CreateAction(formatsTable, OpenType.DIALOG) {
                    @Override
                    public Map<String, Object> getInitialValues() {
                        return Collections.<String, Object>singletonMap("report", getItem());
                    }
                }
        );
        formatsTable.addAction(new RemoveAction(formatsTable, false));
        formatsTable.addAction(new EditAction(formatsTable, OpenType.DIALOG));
    }

    protected void initRoles() {
        rolesTable.addAction(new RemoveAction(rolesTable, false));

        addRoleBtn.setAction(new AbstractAction("actions.Add") {
            @Override
            public void actionPerform(Component component) {
                if (lookupRolesDs.getItem() != null && !rolesDs.containsItem(lookupRolesDs.getItem().getId())) {
                    rolesDs.addItem(lookupRolesDs.getItem());
                }
            }
        });
    }

    protected void initScreens() {
        screenTable.addAction(new RemoveAction(screenTable, false));
        List<WindowInfo> windowInfoCollection = new ArrayList<>(windowConfig.getWindows());
        // sort by screenId
        Collections.sort(windowInfoCollection, new Comparator<WindowInfo>() {
            @Override
            public int compare(WindowInfo w1, WindowInfo w2) {
                int w1DollarIndex = w1.getId().indexOf("$");
                int w2DollarIndex = w2.getId().indexOf("$");

                if ((w1DollarIndex > 0 && w2DollarIndex > 0) || (w1DollarIndex < 0 && w2DollarIndex < 0)) {
                    return w1.getId().compareTo(w2.getId());
                } else if (w1DollarIndex > 0) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        Map<String, Object> screens = new LinkedHashMap<>();
        for (WindowInfo windowInfo : windowInfoCollection) {
            String id = windowInfo.getId();
            String menuId = "menu-config." + id;
            String localeMsg = AppBeans.get(Messages.class).getMessage(AppConfig.getMessagesPack(), menuId);
            String title = menuId.equals(localeMsg) ? id : id + " ( " + localeMsg + " )";
            screens.put(title, id);
        }
        screenIdLookup.setOptionsMap(screens);

        addReportScreenBtn.setAction(new AbstractAction("actions.Add") {
            @Override
            public void actionPerform(Component component) {
                if (screenIdLookup.getValue() != null) {
                    String screenId = screenIdLookup.getValue();

                    boolean exists = false;
                    for (UUID id : reportScreensDs.getItemIds()) {
                        ReportScreen item = reportScreensDs.getItem(id);
                        if (screenId.equalsIgnoreCase(item.getScreenId())) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        ReportScreen reportScreen = new ReportScreen();
                        reportScreen.setReport(getItem());
                        reportScreen.setScreenId(screenId);
                        reportScreensDs.addItem(reportScreen);
                    }
                }
            }
        });
    }

    protected void initGeneral() {
        treeDs.addListener(new DsListenerAdapter<BandDefinition>() {
            @Override
            public void itemChanged(Datasource<BandDefinition> ds, @Nullable BandDefinition prevItem, @Nullable BandDefinition item) {
                bandEditor.setBandDefinition(item);
                bandName.setEnabled(item != null);
                bandOrientation.setEnabled(item != null);
                parentBand.setEnabled(item != null);
            }
        });

        bandEditor.getBandDefinitionDs().addListener(new DsListenerAdapter<BandDefinition>() {
            @Override
            public void valueChanged(BandDefinition source, String property, @Nullable Object prevValue, @Nullable Object value) {
                if ("parentBandDefinition".equals(property)) {
                    if (value == source) {
                        source.setParentBandDefinition((BandDefinition) prevValue);
                    } else {
                        treeDs.refresh();
                    }
                }
            }
        });

        propertiesFieldGroup.addCustomField("defaultTemplate", new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                LookupPickerField lookupPickerField = componentsFactory.createComponent(LookupPickerField.NAME);

                lookupPickerField.setOptionsDatasource(templatesDs);

                lookupPickerField.addAction(new AbstractAction("download") {

                    @Override
                    public String getCaption() {
                        return getMessage("report.download");
                    }

                    @Override
                    public String getIcon() {
                        return "icons/reports-template-download.png";
                    }

                    @Override
                    public void actionPerform(Component component) {
                        ReportTemplate defaultTemplate = getItem().getDefaultTemplate();
                        if (defaultTemplate != null) {
                            ExportDisplay exportDisplay = AppConfig.createExportDisplay(ReportEditor.this);
                            byte[] reportTemplate = defaultTemplate.getContent();
                            exportDisplay.show(new ByteArrayDataProvider(reportTemplate), defaultTemplate.getName(), ExportFormat.getByExtension(defaultTemplate.getExt()));
                        } else {
                            showNotification(getMessage("notification.defaultTemplateIsEmpty"), NotificationType.HUMANIZED);
                        }
                    }
                });

                lookupPickerField.addAction(new AbstractAction("upload") {
                    @Override
                    public String getCaption() {
                        return getMessage("report.upload");
                    }

                    @Override
                    public String getIcon() {
                        return "icons/reports-template-upload.png";
                    }

                    @Override
                    public void actionPerform(Component component) {
                        final ReportTemplate defaultTemplate = getItem().getDefaultTemplate();
                        if (defaultTemplate != null) {
                            final FileUploadDialog dialog = openWindow("fileUploadDialog", OpenType.DIALOG);
                            dialog.addListener(new CloseListener() {
                                @Override
                                public void windowClosed(String actionId) {
                                    if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                                        File file = fileUpload.getFile(dialog.getFileId());
                                        try {
                                            byte[] data = FileUtils.readFileToByteArray(file);
                                            defaultTemplate.setContent(data);
                                            defaultTemplate.setName(dialog.getFileName());
                                            templatesDs.modifyItem(defaultTemplate);
                                        } catch (IOException e) {
                                            throw new RuntimeException(String.format("An error occurred while uploading file for template [%s]", defaultTemplate.getCode()));
                                        }
                                    }
                                }
                            });
                        } else {
                            showNotification(getMessage("notification.defaultTemplateIsEmpty"), NotificationType.HUMANIZED);
                        }
                    }
                });

                lookupPickerField.addAction(new AbstractAction("edit") {
                    @Override
                    public String getIcon() {
                        return "icons/reports-template-view.png";
                    }

                    @Override
                    public void actionPerform(Component component) {
                        ReportTemplate defaultTemplate = getItem().getDefaultTemplate();
                        if (defaultTemplate != null) {
                            final Editor editor = openEditor("report$ReportTemplate.edit", defaultTemplate, OpenType.DIALOG, templatesDs);
                            editor.addListener(new CloseListener() {
                                @Override
                                public void windowClosed(String actionId) {
                                    if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                                        ReportTemplate item = (ReportTemplate) editor.getItem();
                                        getItem().setDefaultTemplate(item);
                                        templatesDs.modifyItem(item);
                                    }
                                }
                            });
                        } else {
                            showNotification(getMessage("notification.defaultTemplateIsEmpty"), NotificationType.HUMANIZED);
                        }
                    }
                });

                return lookupPickerField;
            }
        });


        ((HierarchicalPropertyDatasourceImpl) treeDs).setSortPropertyName("position");

        createBandDefinitionButton.setAction(new AbstractAction("create") {
            @Override
            public String getCaption() {
                return "";
            }

            @Override
            public void actionPerform(Component component) {
                BandDefinition parentDefinition = treeDs.getItem();
                Report report = getItem();
                // Use root band as parent if no items selected
                if (parentDefinition == null) {
                    parentDefinition = report.getRootBandDefinition();
                }
                if (parentDefinition.getChildrenBandDefinitions() == null) {
                    parentDefinition.setChildrenBandDefinitions(new ArrayList<BandDefinition>());
                }

                //
                orderBandDefinitions(parentDefinition);

                BandDefinition newBandDefinition = new BandDefinition();
                newBandDefinition.setName("new Band");
                newBandDefinition.setOrientation(Orientation.HORIZONTAL);
                newBandDefinition.setParentBandDefinition(parentDefinition);
                newBandDefinition.setPosition(parentDefinition.getChildrenBandDefinitions() != null ? parentDefinition.getChildrenBandDefinitions().size() : 0);
                newBandDefinition.setReport(report);
                parentDefinition.getChildrenBandDefinitions().add(newBandDefinition);

                treeDs.addItem(newBandDefinition);

                treeDs.refresh();
                tree.expandTree();
            }
        });

        removeBandDefinitionButton.setAction(new RemoveAction(bandTree, false, "generalFrame.removeBandDefinition") {
            @Override
            public String getCaption() {
                return "";
            }

            @Override
            public boolean isApplicableTo(Datasource.State state, Entity item) {
                return super.isApplicableTo(state, item) && !ObjectUtils.equals(getItem().getRootBandDefinition(), item);
            }

            @Override
            protected void doRemove(Set selected, boolean autocommit) {

                if (selected != null) {
                    removeChildrenCascade(selected);
                    for (Object object : selected) {
                        BandDefinition definition = (BandDefinition) object;
                        if (definition.getParentBandDefinition() != null) {
                            orderBandDefinitions(((BandDefinition) object).getParentBandDefinition());
                        }
                    }
                }
            }

            private void removeChildrenCascade(Collection selected) {
                for (Object o : selected) {
                    BandDefinition definition = (BandDefinition) o;
                    BandDefinition parentDefinition = definition.getParentBandDefinition();
                    if (parentDefinition != null) {
                        definition.getParentBandDefinition().getChildrenBandDefinitions().remove(definition);
                    }

                    if (definition.getChildrenBandDefinitions() != null) {
                        removeChildrenCascade(new ArrayList<>(definition.getChildrenBandDefinitions()));
                    }

                    if (definition.getDataSets() != null) {
                        treeDs.setItem(definition);
                        for (DataSet dataSet : new ArrayList<>(definition.getDataSets())) {
                            if (PersistenceHelper.isNew(dataSet)) {
                                dataSetsDs.removeItem(dataSet);
                            }
                        }
                    }
                    treeDs.removeItem(definition);
                }
            }
        });

        bandUpButton.setAction(new ItemTrackingAction("generalFrame.up") {
            @Override
            public String getCaption() {
                return "";
            }

            @Override
            public void actionPerform(Component component) {
                BandDefinition definition = treeDs.getItem();
                if (definition != null && definition.getParentBandDefinition() != null) {
                    BandDefinition parentDefinition = definition.getParentBandDefinition();
                    List<BandDefinition> definitionsList = parentDefinition.getChildrenBandDefinitions();
                    int index = definitionsList.indexOf(definition);
                    if (index > 0) {
                        BandDefinition previousDefinition = definitionsList.get(index - 1);
                        definition.setPosition(definition.getPosition() - 1);
                        previousDefinition.setPosition(previousDefinition.getPosition() + 1);

                        definitionsList.set(index, previousDefinition);
                        definitionsList.set(index - 1, definition);

                        treeDs.refresh();
                    }
                }
            }

            @Override
            public boolean isApplicableTo(Datasource.State state, Entity item) {
                return super.isApplicableTo(state, item) && ((BandDefinition) item).getPosition() > 0;
            }
        });

        bandDownButton.setAction(new ItemTrackingAction("generalFrame.down") {
            @Override
            public String getCaption() {
                return "";
            }

            @Override
            public void actionPerform(Component component) {
                BandDefinition definition = treeDs.getItem();
                if (definition != null && definition.getParentBandDefinition() != null) {
                    BandDefinition parentDefinition = definition.getParentBandDefinition();
                    List<BandDefinition> definitionsList = parentDefinition.getChildrenBandDefinitions();
                    int index = definitionsList.indexOf(definition);
                    if (index < definitionsList.size() - 1) {
                        BandDefinition nextDefinition = definitionsList.get(index + 1);
                        definition.setPosition(definition.getPosition() + 1);
                        nextDefinition.setPosition(nextDefinition.getPosition() - 1);

                        definitionsList.set(index, nextDefinition);
                        definitionsList.set(index + 1, definition);

                        treeDs.refresh();
                    }
                }
            }

            @Override
            public boolean isApplicableTo(Datasource.State state, Entity item) {
                if (super.isApplicableTo(state, item)) {
                    BandDefinition bandDefinition = (BandDefinition) item;
                    BandDefinition parent = bandDefinition.getParentBandDefinition();
                    return parent != null &&
                            parent.getChildrenBandDefinitions() != null &&
                            bandDefinition.getPosition() < parent.getChildrenBandDefinitions().size() - 1;
                }
                return false;
            }
        });

        bandTree.addAction(createBandDefinitionButton.getAction());
        bandTree.addAction(removeBandDefinitionButton.getAction());
        bandTree.addAction(bandUpButton.getAction());
        bandTree.addAction(bandDownButton.getAction());

        saveAndRun.setAction(new AbstractAction("button.saveAndRun") {
            @Override
            public void actionPerform(Component component) {
                if (ReportEditor.this.commit()) {
                    postInit();
                    ReportEditor.this.openWindow("report$inputParameters", WindowManager.OpenType.DIALOG,
                            Collections.<String, Object>singletonMap("report", getItem()));
                }
            }
        });
    }

    protected void initTemplates() {
        templatesTable.addAction(new CreateAction(templatesTable, OpenType.DIALOG) {
            @Override
            public Map<String, Object> getInitialValues() {
                return Collections.<String, Object>singletonMap("report", getItem());
            }
        });
        templatesTable.addAction(new EditAction(templatesTable, OpenType.DIALOG));
        templatesTable.addAction(new RemoveAction(templatesTable, false));

        templatesTable.addAction(new ItemTrackingAction("defaultTemplate") {
            @Override
            public String getCaption() {
                return getMessage("report.defaultTemplate");
            }

            @Override
            public void actionPerform(Component component) {
                ReportTemplate template = templatesTable.getSingleSelected();
                if (template != null) {
                    template.getReport().setDefaultTemplate(template);
                }
                updateApplicableTo(false);
            }

            @Override
            public boolean isApplicableTo(Datasource.State state, Entity item) {
                return super.isApplicableTo(state, item) && getItem().getDefaultTemplate() != item;
            }
        });
    }

    protected void orderParameters() {
        if (getItem().getInputParameters() == null) {
            getItem().setInputParameters(new ArrayList<ReportInputParameter>());
        }

        for (int i = 0; i < getItem().getInputParameters().size(); i++) {
            getItem().getInputParameters().get(i).setPosition(i);
        }
    }

    protected void orderBandDefinitions(BandDefinition parent) {
        if (parent.getChildrenBandDefinitions() != null) {
            List<BandDefinition> childrenBandDefinitions = parent.getChildrenBandDefinitions();
            for (int i = 0, childrenBandDefinitionsSize = childrenBandDefinitions.size(); i < childrenBandDefinitionsSize; i++) {
                BandDefinition bandDefinition = childrenBandDefinitions.get(i);
                bandDefinition.setPosition(i);

            }
        }
    }

    protected boolean preCommit() {
        addCommitListeners();

        if (PersistenceHelper.isNew(getItem())) {
            ((CollectionPropertyDatasourceImpl) treeDs).setModified(true);
        }

        return true;
    }

    protected void addCommitListeners() {
        String xml = reportService.convertToXml(getItem());
        getItem().setXml(xml);

        reportDs.getDsContext().addListener(new DsContext.CommitListener() {
            @Override
            public void beforeCommit(CommitContext context) {
                for (Iterator<Entity> iterator = context.getCommitInstances().iterator(); iterator.hasNext(); ) {
                    Entity entity = iterator.next();
                    if (!(entity instanceof Report || entity instanceof ReportTemplate)) {
                        iterator.remove();
                    }
                }
            }

            @Override
            public void afterCommit(CommitContext context, Set<Entity> result) {

            }
        });
    }

    @Override
    protected void postValidate(ValidationErrors errors) {
        if (getItem().getRootBand() == null) {
            errors.add(getMessage("error.rootBandNull"));
        }

        if (CollectionUtils.isNotEmpty(getItem().getRootBandDefinition().getChildrenBandDefinitions())) {
            for (BandDefinition band : getItem().getRootBandDefinition().getChildrenBandDefinitions()) {
                validateBand(errors, band);
            }
        }
    }

    protected void validateBand(ValidationErrors errors, BandDefinition band) {
        if (StringUtils.isBlank(band.getName())) {
            errors.add(getMessage("error.bandNameNull"));
        }

        if (band.getBandOrientation() == null) {
            errors.add(formatMessage("error.bandOrientationNull", band.getName()));
        }

        if (CollectionUtils.isNotEmpty(band.getDataSets())) {
            for (DataSet dataSet : band.getDataSets()) {
                if (StringUtils.isBlank(dataSet.getName())) {
                    errors.add(getMessage("error.dataSetNameNull"));
                }

                if (dataSet.getType() == null) {
                    errors.add(formatMessage("error.dataSetTypeNull", dataSet.getName()));
                }

                if (dataSet.getType() == DataSetType.GROOVY || dataSet.getType() == DataSetType.SQL || dataSet.getType() == DataSetType.JPQL) {
                    if (StringUtils.isBlank(dataSet.getScript())) {
                        errors.add(formatMessage("error.dataSetScriptNull", dataSet.getName()));
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(band.getChildrenBandDefinitions())) {
            for (BandDefinition child : band.getChildrenBandDefinitions()) {
                validateBand(errors, child);
            }
        }
    }
}