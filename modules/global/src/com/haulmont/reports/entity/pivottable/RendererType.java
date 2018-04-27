/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.entity.pivottable;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

public enum RendererType implements EnumClass<String> {
    TABLE("table"),
    TABLE_BAR_CHART("tableBarchart"),
    HEATMAP("heatmap"),
    ROW_HEATMAP("rowHeatmap"),
    COL_HEATMAP("colHeatmap"),
    LINE_CHART("lineChart"),
    BAR_CHART("barChart"),
    STACKED_BAR_CHART("stackedBarChart"),
    AREA_CHART("areaChart"),
    SCATTER_CHART("scatterChart"),
    TREEMAP("treemap"),
    TSV_EXPORT("TSVExport");

    private String id;

    RendererType(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public static RendererType fromId(String id) {
        for (RendererType type : RendererType.values()) {
            if (type.getId().equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}
