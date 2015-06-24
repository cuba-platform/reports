/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.template.edit;

import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.reports.entity.BandDefinition;
import com.haulmont.reports.entity.charts.*;
import com.haulmont.reports.gui.report.run.ShowChartController;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class ChartEditFrameController extends AbstractFrame {
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
    protected Table seriesTable;
    @Inject
    protected FieldGroup pieChartFieldGroup;
    @Inject
    protected FieldGroup serialChartFieldGroup;

    public interface Companion {
        void setWindowWidth(Window window, int width);

        void center(Window window);
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        pieChartDs.setItem(new PieChartDescription());
        serialChartDs.setItem(new SerialChartDescription());
        type.setOptionsList(Arrays.asList(ChartType.values()));
        type.addListener(new ValueListener() {
            @Override
            public void valueChanged(Object source, String property, @Nullable Object prevValue, @Nullable Object value) {
                pieChartFieldGroup.setVisible(ChartType.PIE == value);
                serialChartFieldGroup.setVisible(ChartType.SERIAL == value);
                seriesTable.setVisible(ChartType.SERIAL == value);
                Companion companion = getCompanion();
                if (companion != null) {
                    companion.center((Window) getFrame());
                }

                showChartPreviewBox();
            }
        });
        pieChartFieldGroup.setVisible(false);
        serialChartFieldGroup.setVisible(false);
        seriesTable.setVisible(false);

        seriesTable.addAction(new CreateAction(seriesTable) {
            @Override
            public void actionPerform(Component component) {
                ChartSeries chartSeries = new ChartSeries();
                seriesDs.addItem(chartSeries);
                seriesTable.refresh();
            }
        });

        pieChartDs.addListener(new DsListenerAdapter<PieChartDescription>() {
            @Override
            public void valueChanged(PieChartDescription source, String property, @Nullable Object prevValue, @Nullable Object value) {
                showChartPreviewBox();
            }
        });

        serialChartDs.addListener(new DsListenerAdapter<SerialChartDescription>() {
            @Override
            public void valueChanged(SerialChartDescription source, String property, @Nullable Object prevValue, @Nullable Object value) {
                showChartPreviewBox();
            }
        });

        seriesDs.addListener(new CollectionDsListenerAdapter<ChartSeries>() {
            @Override
            public void valueChanged(ChartSeries source, String property, @Nullable Object prevValue, @Nullable Object value) {
                showChartPreviewBox();
            }

            @Override
            public void collectionChanged(CollectionDatasource ds, Operation operation, List items) {
                showChartPreviewBox();
            }
        });

        FieldGroup.CustomFieldGenerator bandSelectorGenerator = new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                LookupField lookupField = componentsFactory.createComponent(LookupField.NAME);
                lookupField.setDatasource(datasource, propertyId);
                return lookupField;
            }
        };
        pieChartFieldGroup.addCustomField("bandName", bandSelectorGenerator);
        serialChartFieldGroup.addCustomField("bandName", bandSelectorGenerator);
    }

    protected void previewChart(BoxLayout previewBox) {
        List<String> colors = Arrays.asList("red", "green", "blue", "yellow", "orange", "black", "magenta");
        String chartJson = null;
        if (ChartType.SERIAL == type.getValue()) {
            SerialChartDescription chartDescription = serialChartDs.getItem();
            String categoryField = chartDescription.getCategoryField();

            List<Map<String, Object>> data = new ArrayList<>();
            for (int i = 1; i < 6; i++) {
                HashMap<String, Object> map = new HashMap<>();
                data.add(map);

                map.put(categoryField, getMessage("caption.category") + i);

                for (ChartSeries chartSeries : chartDescription.getSeries()) {
                    String valueField = chartSeries.getValueField();
                    String colorField = chartSeries.getColorField();
                    map.put(valueField, Math.abs(RandomUtils.nextInt(100)));
                    map.put(colorField, colors.get(RandomUtils.nextInt(6)));
                }
            }

            ChartToJsonConverter chartToJsonConverter = new ChartToJsonConverter();
            chartJson = chartToJsonConverter.convertSerialChart(chartDescription, data);
        } else if (ChartType.PIE == type.getValue()) {
            PieChartDescription chartDescription = pieChartDs.getItem();
            String titleField = chartDescription.getTitleField();
            String valueField = chartDescription.getValueField();
            String colorField = chartDescription.getColorField();

            List<Map<String, Object>> data = new ArrayList<>();
            for (int i = 1; i < 6; i++) {
                HashMap<String, Object> map = new HashMap<>();
                data.add(map);

                map.put(titleField, getMessage("caption.category") + i);
                map.put(valueField, Math.abs(RandomUtils.nextInt(100)));
                map.put(colorField, colors.get(RandomUtils.nextInt(6)));
            }

            ChartToJsonConverter chartToJsonConverter = new ChartToJsonConverter();
            chartJson = chartToJsonConverter.convertPieChart(chartDescription, data);
        }

        openFrame(previewBox, ShowChartController.JSON_CHART_SCREEN_ID, Collections.<String, Object>singletonMap("Chart", chartJson));
    }

    public BoxLayout showChartPreviewBox() {
        Window parent = getFrame();
        BoxLayout previewBox = parent.getComponentNN("chartPreviewBox");
        previewBox.setVisible(true);
        previewBox.setHeight("100%");
        previewBox.setWidth("100%");
        previewBox.removeAll();
        Companion companion = getCompanion();
        if (companion != null) {
            companion.setWindowWidth(parent, 1280);
        }
        previewChart(previewBox);
        return previewBox;
    }

    public void hideChartPreviewBox() {
        Window parent = getFrame();
        BoxLayout previewBox = parent.getComponentNN("chartPreviewBox");
        previewBox.setVisible(false);
        previewBox.removeAll();
        Companion companion = getCompanion();
        if (companion != null) {
            companion.setWindowWidth(parent, 700);
        }
    }

    @Nullable
    public AbstractChartDescription getChartDescription() {
        if (ChartType.SERIAL == type.getValue()) {
            return serialChartDs.getItem();
        } else if (ChartType.PIE == type.getValue()) {
            return pieChartDs.getItem();
        }

        return null;
    }

    public void setChartDescription(@Nullable AbstractChartDescription chartDescription) {
        if (chartDescription != null) {
            if (ChartType.SERIAL == chartDescription.getType()) {
                serialChartDs.setItem((SerialChartDescription) chartDescription);
            } else if (ChartType.PIE == chartDescription.getType()) {
                pieChartDs.setItem((PieChartDescription) chartDescription);
            }
            type.setValue(chartDescription.getType());
        }
    }

    public void setBands(Collection<BandDefinition> bands) {
        List<String> bandNames = new ArrayList<String>();
        for (BandDefinition bandDefinition : bands) {
            bandNames.add(bandDefinition.getName());
        }

        LookupField pieChartBandName = (LookupField) pieChartFieldGroup.getFieldComponent("bandName");
        LookupField serialChartBandName = (LookupField) serialChartFieldGroup.getFieldComponent("bandName");

        pieChartBandName.setOptionsList(bandNames);
        serialChartBandName.setOptionsList(bandNames);
    }
}
