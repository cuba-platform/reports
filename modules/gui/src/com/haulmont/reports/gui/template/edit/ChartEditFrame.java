/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.template.edit;

import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.reports.entity.BandDefinition;
import com.haulmont.reports.entity.ReportOutputType;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.reports.entity.charts.*;
import com.haulmont.reports.gui.report.run.ShowChartController;
import com.haulmont.reports.gui.template.edit.generator.RandomChartDataGenerator;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

public class ChartEditFrame extends DescriptionEditFrame {
    @Inject
    protected ComponentsFactory componentsFactory;
    @Inject
    protected Datasource<PieChartDescription> pieChartDs;
    @Inject
    protected Datasource<SerialChartDescription> serialChartDs;
    @Inject
    protected CollectionDatasource<ChartSeries, UUID> seriesDs;
    @Inject
    protected LookupField type;
    @Inject
    protected Table<ChartSeries> seriesTable;
    @Inject
    protected GroupBoxLayout seriesBox;
    @Inject
    protected FieldGroup pieChartFieldGroup;
    @Inject
    protected FieldGroup serialChartFieldGroup;

    protected ReportTemplate reportTemplate;

    @Override
    @SuppressWarnings("IncorrectCreateEntity")
    public void init(Map<String, Object> params) {
        super.init(params);
        pieChartDs.setItem(new PieChartDescription());
        serialChartDs.setItem(new SerialChartDescription());
        type.setOptionsList(Arrays.asList(ChartType.values()));

        type.addValueChangeListener(e -> {
            pieChartFieldGroup.setVisible(ChartType.PIE == e.getValue());
            serialChartFieldGroup.setVisible(ChartType.SERIAL == e.getValue());
            seriesBox.setVisible(ChartType.SERIAL == e.getValue());
            showPreview();
        });

        pieChartFieldGroup.setVisible(false);
        serialChartFieldGroup.setVisible(false);
        seriesBox.setVisible(false);

        seriesTable.addAction(new CreateAction(seriesTable) {
            @Override
            public void actionPerform(Component component) {
                @SuppressWarnings("IncorrectCreateEntity")
                ChartSeries chartSeries = new ChartSeries();
                seriesDs.addItem(chartSeries);
            }
        });

        pieChartDs.addItemPropertyChangeListener(e -> showPreview());

        serialChartDs.addItemPropertyChangeListener(e -> showPreview());

        seriesDs.addItemPropertyChangeListener(e -> showPreview());
        seriesDs.addCollectionChangeListener(e -> showPreview());


        LookupField pieChartBandLookupField = componentsFactory.createComponent(LookupField.class);
        pieChartBandLookupField.setDatasource(pieChartDs, "bandName");

        LookupField serialChartBandLookupField = componentsFactory.createComponent(LookupField.class);
        pieChartBandLookupField.setDatasource(serialChartDs, "bandName");

        pieChartFieldGroup.getFieldNN("bandName").setComponent(pieChartBandLookupField);
        serialChartFieldGroup.getFieldNN("bandName").setComponent(serialChartBandLookupField);
    }

    @Override
    public void setItem(ReportTemplate reportTemplate) {
        super.setItem(reportTemplate);
        setBands(reportTemplate.getReport().getBands());
        if (isApplicable(reportTemplate.getReportOutputType())) {
            setChartDescription(reportTemplate.getChartDescription());
        }
    }

    @Override
    public boolean applyChanges() {
        if (validateChart()) {
            AbstractChartDescription chartDescription = getChartDescription();
            reportTemplate.setChartDescription(chartDescription);
            return true;
        }
        return false;
    }

    @Override
    public boolean isApplicable(ReportOutputType reportOutputType) {
        return reportOutputType == ReportOutputType.CHART;
    }

    protected boolean validateChart() {
        AbstractChartDescription chartDescription = getChartDescription();
        if (chartDescription != null && chartDescription.getType() == ChartType.SERIAL) {
            List<ChartSeries> series = ((SerialChartDescription) chartDescription).getSeries();
            if (series == null || series.size() == 0) {
                showNotification(getMessage("validationFail.caption"),
                        getMessage("chartEdit.seriesEmptyMsg"), NotificationType.TRAY);
                return false;
            }
            for (ChartSeries it : series) {
                if (it.getType() == null) {
                    showNotification(getMessage("validationFail.caption"),
                            getMessage("chartEdit.seriesTypeNullMsg"), NotificationType.TRAY);
                    return false;
                }
                if (it.getValueField() == null) {
                    showNotification(getMessage("validationFail.caption"),
                            getMessage("chartEdit.seriesValueFieldNullMsg"), NotificationType.TRAY);
                    return false;
                }
            }
        }
        return true;
    }

    protected void initPreviewContent(BoxLayout previewBox) {
        List<Map<String, Object>> data;
        String chartJson = null;
        if (ChartType.SERIAL == type.getValue()) {
            SerialChartDescription chartDescription = serialChartDs.getItem();
            data = new RandomChartDataGenerator().generateRandomChartData(chartDescription);
            ChartToJsonConverter chartToJsonConverter = new ChartToJsonConverter();
            chartJson = chartToJsonConverter.convertSerialChart(chartDescription, data);
        } else if (ChartType.PIE == type.getValue()) {
            PieChartDescription chartDescription = pieChartDs.getItem();
            data = new RandomChartDataGenerator().generateRandomChartData(chartDescription);
            ChartToJsonConverter chartToJsonConverter = new ChartToJsonConverter();
            chartJson = chartToJsonConverter.convertPieChart(chartDescription, data);
        }
        chartJson = chartJson == null ? "{}" : chartJson;
        openFrame(previewBox, ShowChartController.JSON_CHART_SCREEN_ID,
                Collections.<String, Object>singletonMap(ShowChartController.CHART_JSON_PARAMETER, chartJson));
    }

    @Nullable
    protected AbstractChartDescription getChartDescription() {
        if (ChartType.SERIAL == type.getValue()) {
            return serialChartDs.getItem();
        } else if (ChartType.PIE == type.getValue()) {
            return pieChartDs.getItem();
        }
        return null;
    }

    protected void setChartDescription(@Nullable AbstractChartDescription chartDescription) {
        if (chartDescription != null) {
            if (ChartType.SERIAL == chartDescription.getType()) {
                serialChartDs.setItem((SerialChartDescription) chartDescription);
            } else if (ChartType.PIE == chartDescription.getType()) {
                pieChartDs.setItem((PieChartDescription) chartDescription);
            }
            type.setValue(chartDescription.getType());
        }
    }

    protected void setBands(Collection<BandDefinition> bands) {
        List<String> bandNames = bands.stream()
                .filter(bandDefinition -> bandDefinition.getParentBandDefinition() != null)
                .map(BandDefinition::getName)
                .collect(Collectors.toList());

        LookupField pieChartBandName = (LookupField) pieChartFieldGroup.getComponentNN("bandName");
        LookupField serialChartBandName = (LookupField) serialChartFieldGroup.getComponentNN("bandName");

        pieChartBandName.setOptionsList(bandNames);
        serialChartBandName.setOptionsList(bandNames);
    }
}
