/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.template.edit;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.entity.KeyValueEntity;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.GroupDatasource;
import com.haulmont.reports.entity.BandDefinition;
import com.haulmont.reports.entity.ReportOutputType;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.reports.entity.pivottable.*;
import com.haulmont.reports.gui.report.run.ShowPivotTableController;
import com.haulmont.reports.gui.template.edit.generator.RandomPivotTableDataGenerator;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PivotTableEditFrame extends DescriptionEditFrame {

    public static final Set<RendererType> C3_RENDERER_TYPES = Sets.newHashSet(
            RendererType.LINE_CHART, RendererType.BAR_CHART, RendererType.STACKED_BAR_CHART,
            RendererType.AREA_CHART, RendererType.SCATTER_CHART);

    public static final Set<RendererType> HEATMAP_RENDERER_TYPES = Sets.newHashSet(
            RendererType.HEATMAP, RendererType.COL_HEATMAP, RendererType.ROW_HEATMAP);

    @Inject
    protected Datasource<PivotTableDescription> pivotTableDs;

    @Inject
    protected CollectionDatasource<PivotTableAggregation, UUID> aggregationsDs;

    @Inject
    protected GroupDatasource<PivotTableProperty, UUID> propertyDs;

    @Inject
    protected GroupTable<PivotTableProperty> propertyTable;

    @Inject
    protected Table<PivotTableAggregation> aggregationsTable;

    @Named("rendererGroup.defaultRenderer")
    protected LookupField defaultRenderer;

    @Inject
    protected LookupField defaultAggregation;

    @Named("pivotTableGroup.bandName")
    protected LookupField bandName;

    @Inject
    protected GroupBoxLayout customC3GroupBox;

    @Inject
    protected GroupBoxLayout customHeatmapGroupBox;

    @Inject
    protected PopupButton propertiesCreateButton;

    protected RandomPivotTableDataGenerator dataGenerator;

    @Override
    @SuppressWarnings("IncorrectCreateEntity")
    public void init(Map<String, Object> params) {
        super.init(params);
        dataGenerator = new RandomPivotTableDataGenerator();
        PivotTableDescription description = createDefaultPivotTableDescription();
        pivotTableDs.setItem(description);
        initAggregationTable();
        initPropertyTable();
        pivotTableDs.addItemPropertyChangeListener(e -> showPreview());
    }

    @Override
    public void setItem(ReportTemplate reportTemplate) {
        super.setItem(reportTemplate);
        setBands(reportTemplate.getReport().getBands());
        if (isApplicable(reportTemplate.getReportOutputType())) {
            if (reportTemplate.getPivotTableDescription() == null) {
                pivotTableDs.setItem(createDefaultPivotTableDescription());
            } else {
                pivotTableDs.setItem(reportTemplate.getPivotTableDescription());
            }
        }
        initRendererTypes();
        propertyTable.expandAll();
    }

    @Override
    public boolean applyChanges() {
        ValidationErrors errors = validatePivotTableDescription(getPivotTableDescription());
        if (!errors.isEmpty()) {
            showValidationErrors(errors);
            return false;
        }
        getReportTemplate().setPivotTableDescription(getPivotTableDescription());
        return true;
    }

    @Override
    public boolean isApplicable(ReportOutputType reportOutputType) {
        return reportOutputType == ReportOutputType.PIVOT_TABLE;
    }

    protected PivotTableDescription createDefaultPivotTableDescription() {
        PivotTableDescription description = new PivotTableDescription();
        if (description.getDefaultRenderer() == null) {
            description.setDefaultRenderer(RendererType.TABLE);
            description.setRenderers(Sets.newHashSet(RendererType.TABLE));
        }
        return description;
    }

    @Override
    protected void initPreviewContent(BoxLayout previewBox) {
        previewBox.removeAll();
        PivotTableDescription pivotTableDescription = getPivotTableDescription();
        ValidationErrors errors = validatePivotTableDescription(pivotTableDescription);
        if (errors.isEmpty()) {
            List<KeyValueEntity> data = dataGenerator.generate(pivotTableDescription, 10);
            Frame frame = openFrame(previewBox, ShowPivotTableController.PIVOT_TABLE_SCREEN_ID, ParamsMap.of(
                    "pivotTableJson", PivotTableDescription.toJsonString(pivotTableDescription),
                    "values", data));
            frame.setHeight("472px");
        }
    }

    protected PivotTableDescription getPivotTableDescription() {
        return pivotTableDs.getItem();
    }

    protected ValidationErrors validatePivotTableDescription(PivotTableDescription description) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (description.getBandName() == null) {
            validationErrors.add(getMessage("pivotTableEdit.bandRequired"));
        }
        if (description.getDefaultRenderer() == null) {
            validationErrors.add(getMessage("pivotTableEdit.rendererRequired"));
        }
        if (description.getAggregations().isEmpty()) {
            validationErrors.add(getMessage("pivotTableEdit.aggregationsRequired"));
        }
        if (description.getProperties().isEmpty()) {
            validationErrors.add(getMessage("pivotTableEdit.propertiesRequired"));
        }
        if (description.getAggregationProperties().isEmpty()) {
            validationErrors.add(getMessage("pivotTableEdit.aggregationPropertiesRequired"));
        }
        if (description.getColumnsProperties().isEmpty() && description.getRowsProperties().isEmpty()) {
            validationErrors.add(getMessage("pivotTableEdit.columnsOrRowsRequired"));
        }
        if (!Collections.disjoint(description.getRowsProperties(), description.getColumnsProperties())
                || !Collections.disjoint(description.getRowsProperties(), description.getAggregationProperties())
                || !Collections.disjoint(description.getColumnsProperties(), description.getAggregationProperties())) {
            validationErrors.add(getMessage("pivotTableEdit.propertyIntersection"));
        } else if (description.getProperties() != null) {
            Set<String> propertyNames = description.getProperties().stream()
                    .map(PivotTableProperty::getName)
                    .collect(Collectors.toSet());
            if (propertyNames.size() != description.getProperties().size()) {
                validationErrors.add(getMessage("pivotTableEdit.propertyIntersection"));
            }
        }
        return validationErrors;
    }

    protected void setBands(Collection<BandDefinition> bands) {
        List<String> bandNames = bands.stream()
                .filter(bandDefinition -> bandDefinition.getParentBandDefinition() != null)
                .map(BandDefinition::getName)
                .collect(Collectors.toList());
        bandName.setOptionsList(bandNames);
    }

    protected void initRendererTypes() {
        initCustomGroups();
        initDefaultRenderer();

        pivotTableDs.addItemPropertyChangeListener(e -> {
            if ("renderers".equals(e.getProperty())) {
                PivotTableDescription description = getPivotTableDescription();
                Set<RendererType> rendererTypes = description.getRenderers();
                if (rendererTypes.size() == 1) {
                    description.setDefaultRenderer(Iterables.getFirst(rendererTypes, null));
                }
                initCustomGroups();
                initDefaultRenderer();
            }
        });
    }

    protected void initAggregationTable() {
        Supplier<Map<String, Object>> paramsSupplier = () -> ParamsMap.of("existingItems", aggregationsDs.getItems());
        CreateAction createAction = CreateAction.create(aggregationsTable);
        createAction.setWindowParamsSupplier(paramsSupplier);
        aggregationsTable.addAction(createAction);
        EditAction editAction = EditAction.create(aggregationsTable);
        editAction.setWindowParamsSupplier(paramsSupplier);
        aggregationsTable.addAction(editAction);
        aggregationsTable.addAction(RemoveAction.create(aggregationsTable));

        aggregationsDs.addCollectionChangeListener(e -> {
            if (e.getOperation() == CollectionDatasource.Operation.REMOVE) {
                defaultAggregation.setOptionsDatasource(aggregationsDs);
            }
        });
    }

    protected void initCustomGroups() {
        Set<RendererType> rendererTypes = getPivotTableDescription().getRenderers();
        customC3GroupBox.setVisible(!Collections.disjoint(rendererTypes, C3_RENDERER_TYPES));
        customHeatmapGroupBox.setVisible(!Collections.disjoint(rendererTypes, HEATMAP_RENDERER_TYPES));
    }

    protected void initDefaultRenderer() {
        List<RendererType> rendererTypes = new ArrayList<>(getPivotTableDescription().getRenderers());
        defaultRenderer.setOptionsList(rendererTypes);
        defaultRenderer.setEnabled(rendererTypes.size() > 1);
    }

    protected void initPropertyTable() {
        propertyDs.addCollectionChangeListener(e -> {
            PivotTableDescription description = getPivotTableDescription();
            description.getAggregationProperties().clear();
            description.getColumnsProperties().clear();
            description.getRowsProperties().clear();

            for (PivotTableProperty property : getPivotTableDescription().getProperties()) {
                if (property.getType() == PivotTablePropertyType.AGGREGATIONS) {
                    description.getAggregationProperties().add(property.getName());
                } else if (property.getType() == PivotTablePropertyType.COLUMNS) {
                    description.getColumnsProperties().add(property.getName());
                } else if (property.getType() == PivotTablePropertyType.ROWS) {
                    description.getRowsProperties().add(property.getName());
                }
            }
            propertyTable.expandAll();
            showPreview();
        });

        propertyTable.addAction(createPropertyRemoveAction());
        propertyTable.addAction(createPropertyEditAction());

        propertiesCreateButton.setCaption(messages.getMainMessage("actions.Create"));

        CreateAction createAction = createPropertyCreateAction(PivotTablePropertyType.ROWS);
        propertyTable.addAction(createAction);
        propertiesCreateButton.addAction(createAction);

        createAction = createPropertyCreateAction(PivotTablePropertyType.COLUMNS);
        propertyTable.addAction(createAction);
        propertiesCreateButton.addAction(createAction);

        createAction = createPropertyCreateAction(PivotTablePropertyType.AGGREGATIONS);
        propertyTable.addAction(createAction);
        propertiesCreateButton.addAction(createAction);

        createAction = createPropertyCreateAction(PivotTablePropertyType.DERIVED);
        propertyTable.addAction(createAction);
        propertiesCreateButton.addAction(createAction);
    }

    protected RemoveAction createPropertyRemoveAction() {
        return RemoveAction.create(propertyTable);
    }

    protected EditAction createPropertyEditAction() {
        EditAction action = EditAction.create(propertyTable);
        action.setAfterCommitHandler(entity -> propertyTable.expandAll());
        return action;
    }

    protected CreateAction createPropertyCreateAction(PivotTablePropertyType propertyType) {
        CreateAction action = CreateAction.create(propertyTable, WindowManager.OpenType.THIS_TAB, "create_" + propertyType.getId());
        Map<String, Object> values = new HashMap<>();
        values.put("type", propertyType);
        action.setInitialValues(values);
        action.setCaption(messages.getMessage(propertyType));
        return action;
    }
}
