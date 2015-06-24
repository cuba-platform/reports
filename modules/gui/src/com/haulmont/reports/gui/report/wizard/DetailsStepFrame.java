/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.report.wizard;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.MessageTools;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.FrameContextImpl;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.filter.ConditionsTree;
import com.haulmont.cuba.gui.components.filter.FilterParser;
import com.haulmont.cuba.gui.components.filter.Param;
import com.haulmont.cuba.gui.components.filter.edit.FilterEditor;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.gui.data.impl.DsContextImpl;
import com.haulmont.cuba.gui.filter.QueryFilter;
import com.haulmont.cuba.gui.xml.ParameterInfo;
import com.haulmont.cuba.security.entity.FilterEntity;
import com.haulmont.reports.entity.ParameterType;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.TemplateFileType;
import com.haulmont.reports.gui.report.run.ParameterClassResolver;
import com.haulmont.reports.gui.report.run.ShowChartController;
import com.haulmont.reports.gui.report.wizard.step.StepFrame;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */
class DetailsStepFrame extends StepFrame {
    protected ConditionsTree conditionsTree;
    protected Filter filter;
    protected FilterEntity filterEntity;

    public DetailsStepFrame(ReportWizardCreator wizard) {
        super(wizard, wizard.getMessage("reportDetails"), "detailsStep");
        isFirst = true;
        initFrameHandler = new InitDetailsStepFrameHandler();
    }

    protected class InitDetailsStepFrameHandler implements InitStepFrameHandler {
        @Override
        public void initFrame() {
            initReportTypeOptionGroup();
            initTemplateFormatLookupField();
            initEntityLookupField();

            wizard.entity.addListener(new ChangeReportNameListener());

            wizard.setQueryButton.setAction(new SetQueryAction());
        }

        protected void initEntityLookupField() {
            wizard.entity.setOptionsMap(getAvailableEntities());
            wizard.entity.addListener(new ClearRegionListener(
                    new DialogActionWithChangedValue(DialogAction.Type.YES) {
                        @Override
                        public void actionPerform(Component component) {
                            wizard.getItem().getReportRegions().clear();
                            wizard.regionsTable.refresh(); //for web6
                            wizard.needUpdateEntityModel = true;
                            wizard.entity.setValue(newValue);

                            clearQueryAndFilter();
                        }
                    }));
        }

        protected void initTemplateFormatLookupField() {
            wizard.templateFileFormat.setOptionsMap(getAvailableTemplateFormats());
            wizard.templateFileFormat.setValue(TemplateFileType.DOCX);//select docx as default value
        }

        protected void initReportTypeOptionGroup() {
            wizard.reportTypeOptionGroup.setOptionsMap(getListedReportOptionsMap());
            wizard.reportTypeOptionGroup.setValue(ReportData.ReportType.SINGLE_ENTITY);
            wizard.reportTypeOptionGroup.addListener(new ClearRegionListener(
                    new DialogActionWithChangedValue(DialogAction.Type.YES) {
                        @Override
                        public void actionPerform(Component component) {
                            wizard.getItem().getReportRegions().clear();
                            wizard.regionsTable.refresh(); //for web6
                            wizard.reportTypeOptionGroup.setValue(newValue);
                        }
                    }));
        }

        protected Map<String, Object> getListedReportOptionsMap() {
            Map<String, Object> result = new LinkedHashMap<>(3);
            result.put(wizard.getMessage("singleEntityReport"), ReportData.ReportType.SINGLE_ENTITY);
            result.put(wizard.getMessage("listOfEntitiesReport"), ReportData.ReportType.LIST_OF_ENTITIES);
            result.put(wizard.getMessage("listOfEntitiesReportWithQuery"), ReportData.ReportType.LIST_OF_ENTITIES_WITH_QUERY);
            return result;
        }

        protected Map<String, Object> getAvailableTemplateFormats() {
            Messages messages = AppBeans.get(Messages.NAME);
            Map<String, Object> result = new LinkedHashMap<>(4);
            result.put(messages.getMessage(TemplateFileType.XLSX), TemplateFileType.XLSX);
            result.put(messages.getMessage(TemplateFileType.DOCX), TemplateFileType.DOCX);
            result.put(messages.getMessage(TemplateFileType.HTML), TemplateFileType.HTML);
            WindowConfig windowConfig = AppBeans.get(WindowConfig.NAME);
            if (windowConfig.hasWindow(ShowChartController.JSON_CHART_SCREEN_ID)) {
                result.put(messages.getMessage(TemplateFileType.CHART), TemplateFileType.CHART);
            }
            return result;
        }

        protected Map<String, Object> getAvailableEntities() {
            Map<String, Object> result = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            Collection<MetaClass> classes = wizard.metadataTools.getAllPersistentMetaClasses();
            for (MetaClass metaClass : classes) {
                MetaClass effectiveMetaClass = wizard.metadata.getExtendedEntities().getEffectiveMetaClass(metaClass);
                if (!wizard.reportWizardService.isEntityAllowedForReportWizard(effectiveMetaClass)) {
                    continue;
                }
                result.put(wizard.messageTools.getEntityCaption(effectiveMetaClass) + " (" + effectiveMetaClass.getName() + ")", effectiveMetaClass);
            }
            return result;
        }
    }

    @Override
    public List<String> validateFrame() {
        ArrayList<String> errors = new ArrayList<>(super.validateFrame());
        if (wizard.reportTypeOptionGroup.getValue() == ReportData.ReportType.LIST_OF_ENTITIES_WITH_QUERY && wizard.query == null) {
            errors.add(wizard.getMessage("fillReportQuery"));
        }

        return errors;
    }

    protected class ChangeReportNameListener implements ValueListener {
        @Override
        public void valueChanged(Object source, String property, @Nullable Object prevValue, @Nullable Object value) {
            setGeneratedReportName((MetaClass) prevValue, (MetaClass) value);
            wizard.outputFileName.setValue("");
        }

        private void setGeneratedReportName(MetaClass prevValue, MetaClass value) {
            String oldReportName = wizard.reportName.getValue();
            MessageTools messageTools = wizard.messageTools;
            if (StringUtils.isBlank(oldReportName)) {
                String newText = wizard.formatMessage("reportNamePattern", messageTools.getEntityCaption(value));
                wizard.reportName.setValue(newText);
            } else {
                //if old text contains MetaClass name substring, just replace it
                String prevEntityCaption = messageTools.getEntityCaption(prevValue);
                if (prevValue != null && StringUtils.contains(oldReportName, prevEntityCaption)) {
                    String newText = StringUtils.replace(oldReportName, prevEntityCaption, messageTools.getEntityCaption(value));
                    wizard.reportName.setValue(newText);
                    if (!oldReportName.equals(wizard.formatMessage("reportNamePattern", prevEntityCaption))) {
                        //if user changed auto generated report name and we have changed it, we show message to him
                        wizard.showNotification(wizard.getMessage("reportNameChanged"), IFrame.NotificationType.TRAY);
                    }
                }
            }
        }
    }

    protected class SetQueryAction extends AbstractAction {
        public SetQueryAction() {super("setQuery");}

        @Override
        public boolean isVisible() {
            return ReportData.ReportType.LIST_OF_ENTITIES_WITH_QUERY == wizard.reportTypeOptionGroup.getValue();
        }

        @Override
        public void actionPerform(Component component) {
            if (wizard.entity.getValue() == null) {
                wizard.showNotification(wizard.getMessage("fillEntityMsg"), IFrame.NotificationType.TRAY_HTML);
                return;
            }

            WindowInfo windowInfo = wizard.windowConfig.getWindowInfo("filterEditor");

            filter = createFakeFilter();
            filterEntity = createFakeFilterEntity(filter);
            conditionsTree = createFakeConditionsTree();

            Map<String, Object> params = new HashMap<>();
            params.put("filterEntity", filterEntity);
            params.put("filter", filter);
            params.put("conditions", conditionsTree);
            params.put("useShortConditionForm", true);

            final FilterEditor filterEditor =
                    wizard.windowManagerProvider.get().openWindow(windowInfo, WindowManager.OpenType.DIALOG, params);
            filterEditor.addListener(new Window.CloseListener() {
                private ParameterClassResolver parameterClassResolver = new ParameterClassResolver();

                @Override
                public void windowClosed(String actionId) {
                    if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                        filterEntity = filterEditor.getFilterEntity();
                        collectQueryAndParametersFromFilter();
                    }
                }

                protected void collectQueryAndParametersFromFilter() {
                    filterEntity.setXml(FilterParser.getXml(filterEditor.getConditions(), Param.ValueProperty.DEFAULT_VALUE));
                    if (filterEntity.getXml() != null) {
                        Element element = Dom4j.readDocument(filterEntity.getXml()).getRootElement();
                        QueryFilter queryFilter = new QueryFilter(element, filter.getDatasource().getMetaClass().getName());
                        conditionsTree = filterEditor.getConditionsTree();
                        filter = filterEditor.getFilter();
                        wizard.query = collectQuery(queryFilter);
                        wizard.queryParameters = collectQueryParameters(queryFilter);
                    } else {
                        wizard.showNotification(wizard.getMessage("defaultQueryHasBeenSet"), IFrame.NotificationType.HUMANIZED);
                        wizard.query = filter.getDatasource().getQuery();
                        wizard.queryParameters = Collections.emptyList();
                    }

                    wizard.setQueryButton.setCaption(wizard.getMessage("changeQuery"));
                }

                protected List<ReportData.Parameter> collectQueryParameters(QueryFilter queryFilter) {
                    List<ReportData.Parameter> newParametersList = new ArrayList<>();
                    int i = 1;
                    for (ParameterInfo parameterInfo : queryFilter.getParameters()) {
                        String conditionName = parameterInfo.getConditionName();
                        if (conditionName == null) {
                            conditionName = "parameter";
                        }
                        conditionName = conditionName.replaceAll("\\.", "_");

                        String parameterName = conditionName + i;
                        i++;
                        Class parameterClass = parameterInfo.getJavaClass();
                        ParameterType parameterType = parameterClassResolver.resolveParameterType(parameterClass);
                        if (parameterType == null) {
                            parameterType = ParameterType.TEXT;
                        }

                        newParametersList.add(new ReportData.Parameter(
                                parameterName,
                                parameterClass,
                                parameterType));

                        wizard.query = wizard.query.replace(":" + parameterInfo.getName(), "${" + parameterName + "}");
                    }
                    return newParametersList;
                }

                protected String collectQuery(QueryFilter queryFilter) {
                    Collection<ParameterInfo> parameterDescriptorsFromFilter = queryFilter.getParameters();
                    Map<String, Object> params = new HashMap<>();
                    for (ParameterInfo parameter : parameterDescriptorsFromFilter) {
                        params.put(parameter.getName(), "___");
                    }
                    return queryFilter.processQuery(filter.getDatasource().getQuery(), params);
                }
            });
        }
    }

    protected class DialogActionWithChangedValue extends DialogAction {
        protected Object newValue;

        public DialogActionWithChangedValue(Type type) {
            super(type);

        }

        public DialogActionWithChangedValue setValue(Object value) {
            this.newValue = value;
            return this;
        }
    }

    protected class ClearRegionListener implements ValueListener {
        protected DialogActionWithChangedValue okAction;

        public ClearRegionListener(DialogActionWithChangedValue okAction) {
            this.okAction = okAction;
        }

        @Override
        public void valueChanged(Object source, String property, @Nullable final Object prevValue, @Nullable final Object value) {
            if (!wizard.getItem().getReportRegions().isEmpty()) {
                wizard.showOptionDialog(
                        wizard.getMessage("dialogs.Confirmation"),
                        wizard.getMessage("regionsClearConfirm"),
                        IFrame.MessageType.CONFIRMATION,
                        new AbstractAction[]{
                                okAction.setValue(value),
                                new DialogAction(DialogAction.Type.NO) {
                                }
                        });
            } else {
                wizard.needUpdateEntityModel = true;
                clearQueryAndFilter();
            }

            if (wizard.setQueryButton != null) {
                wizard.setQueryButton.setVisible(
                        wizard.reportTypeOptionGroup.getValue() == ReportData.ReportType.LIST_OF_ENTITIES_WITH_QUERY);
            }
        }
    }

    protected Filter createFakeFilter() {
        if (filter != null) {
            return filter;
        }

        final Filter fakeFilter = wizard.componentsFactory.createComponent(Filter.class);
        fakeFilter.setXmlDescriptor(Dom4j.readDocument("<filter></filter>").getRootElement());
        MetaClass metaClass = wizard.entity.getValue();
        CollectionDatasourceImpl fakeDatasource = new CollectionDatasourceImpl();
        DsContextImpl fakeDsContext = new DsContextImpl(getFrame().getDsContext().getDataSupplier());
        FrameContextImpl fakeFrameContext = new FrameContextImpl(getFrame(), Collections.<String, Object>emptyMap());
        fakeDsContext.setFrameContext(fakeFrameContext);
        fakeDatasource.setDsContext(fakeDsContext);
        //Attention: this query should match the logic in com.haulmont.reports.wizard.ReportingWizardBean.createJpqlDataSet()
        fakeDatasource.setQuery("select queryEntity from " + metaClass.getName() + " queryEntity");
        fakeDatasource.setMetaClass(metaClass);
        fakeFilter.setDatasource(fakeDatasource);
        fakeFilter.setFrame(this.frame);
        return fakeFilter;
    }

    protected ConditionsTree createFakeConditionsTree() {
        return conditionsTree != null ? conditionsTree : new ConditionsTree();
    }

    protected FilterEntity createFakeFilterEntity(Filter fakeFilter) {
        if (filterEntity != null) return filterEntity;

        FilterEntity fakeFilterEntity = new FilterEntity();
        fakeFilterEntity.setXml(fakeFilter.getXmlDescriptor().asXML());
        return fakeFilterEntity;
    }

    protected void clearQueryAndFilter() {
        wizard.query = null;
        wizard.queryParameters = null;
        filter = null;
        filterEntity = null;
        conditionsTree = null;
        wizard.setQueryButton.setCaption(wizard.getMessage("setQuery"));
    }
}
