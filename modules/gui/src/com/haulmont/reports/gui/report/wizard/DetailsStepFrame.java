/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.report.wizard;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.MessageTools;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.filter.*;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.Action.Status;
import com.haulmont.cuba.gui.components.Component.ValueChangeListener;
import com.haulmont.cuba.gui.components.DialogAction.Type;
import com.haulmont.cuba.gui.components.filter.ConditionsTree;
import com.haulmont.cuba.gui.components.filter.FakeFilterSupport;
import com.haulmont.cuba.gui.components.filter.FilterParser;
import com.haulmont.cuba.gui.components.filter.Param;
import com.haulmont.cuba.gui.components.filter.condition.AbstractCondition;
import com.haulmont.cuba.gui.components.filter.edit.FilterEditor;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.security.entity.FilterEntity;
import com.haulmont.reports.entity.ParameterType;
import com.haulmont.reports.entity.PredefinedTransformation;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.ReportData.ReportType;
import com.haulmont.reports.entity.wizard.TemplateFileType;
import com.haulmont.reports.gui.report.run.ParameterClassResolver;
import com.haulmont.reports.gui.report.run.ShowChartController;
import com.haulmont.reports.gui.report.wizard.step.StepFrame;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.util.*;

public class DetailsStepFrame extends StepFrame {
    protected ConditionsTree conditionsTree;
    protected Filter filter;
    protected FilterEntity filterEntity;

    public DetailsStepFrame(ReportWizardCreator wizard) {
        super(wizard, wizard.getMessage("reportDetails"), "detailsStep");
        isFirst = true;
        initFrameHandler = new InitDetailsStepFrameHandler();
        beforeShowFrameHandler = new BeforeShowDetailsStepFrameHandler();
    }

    protected class InitDetailsStepFrameHandler implements InitStepFrameHandler {
        @Override
        public void initFrame() {
            initReportTypeOptionGroup();
            initTemplateFormatLookupField();
            initEntityLookupField();

            wizard.entity.addValueChangeListener(new ChangeReportNameListener());

            wizard.setQueryButton.setAction(new SetQueryAction());
        }

        protected void initEntityLookupField() {
            wizard.entity.setOptionsMap(getAvailableEntities());
            wizard.entity.addValueChangeListener(new ClearRegionListener(
                    new DialogActionWithChangedValue(Type.YES) {
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
            wizard.templateFileFormat.setTextInputAllowed(false);
            wizard.templateFileFormat.setValue(TemplateFileType.DOCX);
        }

        protected void initReportTypeOptionGroup() {
            wizard.reportTypeOptionGroup.setOptionsMap(getListedReportOptionsMap());
            wizard.reportTypeOptionGroup.setValue(ReportType.SINGLE_ENTITY);
            wizard.reportTypeOptionGroup.addValueChangeListener(new ClearRegionListener(
                    new DialogActionWithChangedValue(Type.YES) {
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
            result.put(wizard.getMessage("singleEntityReport"), ReportType.SINGLE_ENTITY);
            result.put(wizard.getMessage("listOfEntitiesReport"), ReportType.LIST_OF_ENTITIES);
            result.put(wizard.getMessage("listOfEntitiesReportWithQuery"), ReportType.LIST_OF_ENTITIES_WITH_QUERY);
            return result;
        }

        protected Map<String, Object> getAvailableTemplateFormats() {
            Messages messages = AppBeans.get(Messages.NAME);
            Map<String, Object> result = new LinkedHashMap<>(4);
            result.put(messages.getMessage(TemplateFileType.XLSX), TemplateFileType.XLSX);
            result.put(messages.getMessage(TemplateFileType.DOCX), TemplateFileType.DOCX);
            result.put(messages.getMessage(TemplateFileType.HTML), TemplateFileType.HTML);
            result.put(messages.getMessage(TemplateFileType.CSV), TemplateFileType.CSV);
            WindowConfig windowConfig = AppBeans.get(WindowConfig.NAME);
            if (windowConfig.hasWindow(ShowChartController.JSON_CHART_SCREEN_ID)) {
                result.put(messages.getMessage(TemplateFileType.CHART), TemplateFileType.CHART);
            }
            return result;
        }

        protected Map<String, Object> getAvailableEntities() {
            Map<String, Object> result = new TreeMap<>(String::compareTo);
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
        if (wizard.reportTypeOptionGroup.getValue() == ReportType.LIST_OF_ENTITIES_WITH_QUERY && wizard.query == null) {
            errors.add(wizard.getMessage("fillReportQuery"));
        }

        return errors;
    }

    protected class ChangeReportNameListener implements ValueChangeListener {

        public ChangeReportNameListener() {
        }

        @Override
        public void valueChanged(Component.ValueChangeEvent e) {
            setGeneratedReportName((MetaClass) e.getPrevValue(), (MetaClass) e.getValue());
            wizard.outputFileName.setValue("");
        }

        protected void setGeneratedReportName(MetaClass prevValue, MetaClass value) {
            String oldReportName = wizard.reportName.getValue();
            MessageTools messageTools = wizard.messageTools;
            if (StringUtils.isBlank(oldReportName)) {
                String newText = wizard.formatMessage("reportNamePattern", messageTools.getEntityCaption(value));
                wizard.reportName.setValue(newText);
            } else {
                if (prevValue != null) {
                    //if old text contains MetaClass name substring, just replace it
                    String prevEntityCaption = messageTools.getEntityCaption(prevValue);
                    if (StringUtils.contains(oldReportName, prevEntityCaption)) {
                        String newText = StringUtils.replace(oldReportName, prevEntityCaption, messageTools.getEntityCaption(value));
                        wizard.reportName.setValue(newText);
                        if (!oldReportName.equals(wizard.formatMessage("reportNamePattern", prevEntityCaption))) {
                            //if user changed auto generated report name and we have changed it, we show message to him
                            wizard.showNotification(wizard.getMessage("reportNameChanged"), Frame.NotificationType.TRAY);
                        }
                    }
                }
            }
        }
    }

    protected class SetQueryAction extends AbstractAction {
        public SetQueryAction() {
            super("setQuery");
        }

        @Override
        public boolean isVisible() {
            return ReportType.LIST_OF_ENTITIES_WITH_QUERY == wizard.reportTypeOptionGroup.getValue();
        }

        @Override
        public void actionPerform(Component component) {
            MetaClass entityMetaClass = wizard.entity.getValue();
            if (entityMetaClass == null) {
                wizard.showNotification(wizard.getMessage("fillEntityMsg"), Frame.NotificationType.TRAY_HTML);
                return;
            }

            FakeFilterSupport fakeFilterSupport = new FakeFilterSupport(wizard, entityMetaClass);
            if (filter == null) {
                filter = fakeFilterSupport.createFakeFilter();
                filterEntity = fakeFilterSupport.createFakeFilterEntity(null);
                conditionsTree = fakeFilterSupport.createFakeConditionsTree(filter, filterEntity);
            }

            Map<String, Object> params = new HashMap<>();
            params.put("filterEntity", filterEntity);
            params.put("filter", filter);
            params.put("conditions", conditionsTree);
            params.put("useShortConditionForm", true);
            params.put("showConditionHiddenOption", true);

            FilterEditor filterEditor = (FilterEditor) wizard.openWindow("filterEditor", OpenType.DIALOG, params);
            filterEditor.addCloseListener(new Window.CloseListener() {
                private ParameterClassResolver parameterClassResolver = new ParameterClassResolver();

                @Override
                public void windowClosed(String actionId) {
                    if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                        filterEntity = filterEditor.getFilterEntity();
                        collectQueryAndParametersFromFilter();
                    }
                }

                protected void collectQueryAndParametersFromFilter() {
                    FilterParser filterParser = AppBeans.get(FilterParser.class);
                    filterEntity.setXml(filterParser.getXml(filterEditor.getConditions(), Param.ValueProperty.DEFAULT_VALUE));
                    if (filterEntity.getXml() != null) {
                        Element element = Dom4j.readDocument(filterEntity.getXml()).getRootElement();
                        QueryFilter queryFilter = new QueryFilter(element);
                        conditionsTree = filterEditor.getConditionsTree();
                        filter = filterEditor.getFilter();
                        wizard.query = collectQuery(queryFilter);
                        wizard.queryParameters = collectQueryParameters(queryFilter);
                    } else {
                        wizard.showNotification(wizard.getMessage("defaultQueryHasBeenSet"), Frame.NotificationType.HUMANIZED);
                        wizard.query = filter.getDatasource().getQuery();
                        wizard.queryParameters = Collections.emptyList();
                    }

                    wizard.setQueryButton.setCaption(wizard.getMessage("changeQuery"));
                }

                protected List<ReportData.Parameter> collectQueryParameters(QueryFilter queryFilter) {
                    List<ReportData.Parameter> newParametersList = new ArrayList<>();
                    int i = 1;
                    for (ParameterInfo parameterInfo : queryFilter.getParameters()) {
                        Condition condition = findConditionByParameter(queryFilter.getRoot(), parameterInfo);
                        String conditionName = parameterInfo.getConditionName();
                        if (conditionName == null) {
                            conditionName = "parameter";
                        }

                        Boolean hiddenConditionPropertyValue = findHiddenPropertyValueByConditionName(conditionName);

                        conditionName = conditionName.replaceAll("\\.", "_");

                        String parameterName = conditionName + i;
                        i++;
                        Class parameterClass = parameterInfo.getJavaClass();
                        ParameterType parameterType = parameterClassResolver.resolveParameterType(parameterClass);
                        if (parameterType == null) {
                            parameterType = ParameterType.TEXT;
                        }

                        String parameterValue = parameterInfo.getValue();
                        parameterValue = !"NULL".equals(parameterValue) ? parameterValue : null;

                        newParametersList.add(new ReportData.Parameter(
                                parameterName,
                                parameterClass,
                                parameterType,
                                parameterValue,
                                resolveParameterTransformation(condition),
                                hiddenConditionPropertyValue));

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

                protected Condition findConditionByParameter(Condition condition, ParameterInfo parameterInfo) {
                    if (!(condition instanceof LogicalCondition)) {
                        Set<ParameterInfo> parameters = condition.getParameters();
                        if (parameters != null && parameters.contains(parameterInfo)) {
                            return condition;
                        }
                    }
                    if (condition.getConditions() != null) {
                        for (Condition it: condition.getConditions()) {
                            return findConditionByParameter(it, parameterInfo);
                        }
                    }
                    return null;
                }

                protected PredefinedTransformation resolveParameterTransformation(Condition condition) {
                    if (condition instanceof Clause) {
                        Clause clause = (Clause) condition;
                        if (clause.getOperator() != null) {
                            switch (clause.getOperator()) {
                                case STARTS_WITH:
                                    return PredefinedTransformation.STARTS_WITH;
                                case ENDS_WITH:
                                    return PredefinedTransformation.ENDS_WITH;
                                case CONTAINS:
                                    return PredefinedTransformation.CONTAINS;
                                case DOES_NOT_CONTAIN:
                                    return PredefinedTransformation.CONTAINS;
                            }
                        }
                    }
                    return null;
                }

                protected Boolean findHiddenPropertyValueByConditionName(String propertyName) {
                    return conditionsTree.toConditionsList().stream()
                            .filter(condition -> Objects.nonNull(condition.getName()))
                            .filter(condition -> condition.getName().equals(propertyName))
                            .map(AbstractCondition::getHidden)
                            .findFirst()
                            .orElse(Boolean.FALSE);
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

    protected class ClearRegionListener implements Component.ValueChangeListener {
        protected DialogActionWithChangedValue okAction;

        public ClearRegionListener(DialogActionWithChangedValue okAction) {
            this.okAction = okAction;
        }

        @Override
        public void valueChanged(Component.ValueChangeEvent e) {
            if (!wizard.getItem().getReportRegions().isEmpty()) {
                wizard.showOptionDialog(
                        wizard.getMessage("dialogs.Confirmation"),
                        wizard.getMessage("regionsClearConfirm"),
                        Frame.MessageType.CONFIRMATION,
                        new AbstractAction[]{
                                okAction.setValue(e.getValue()),

                                new DialogAction(Type.NO, Status.PRIMARY)
                        });
            } else {
                wizard.needUpdateEntityModel = true;
                clearQueryAndFilter();
            }

            if (wizard.setQueryButton != null) {
                wizard.setQueryButton.setVisible(
                        wizard.reportTypeOptionGroup.getValue() == ReportType.LIST_OF_ENTITIES_WITH_QUERY);
            }
        }
    }

    protected void clearQueryAndFilter() {
        wizard.query = null;
        wizard.queryParameters = null;
        filter = null;
        filterEntity = null;
        conditionsTree = null;
        wizard.setQueryButton.setCaption(wizard.getMessage("setQuery"));
    }

    protected class BeforeShowDetailsStepFrameHandler implements BeforeShowStepFrameHandler {
        @Override
        public void beforeShowFrame() {
            wizard.getDialogOptions()
                    .setHeight(wizard.wizardHeight)
                    .center();
        }
    }
}