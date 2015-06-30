/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.template.edit;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.reports.entity.charts.*;
import org.apache.commons.lang.math.RandomUtils;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class RandomChartDataGenerator {
    public static final List<String> COLORS = Arrays.asList("red", "green", "blue", "yellow", "orange", "black", "magenta");

    protected Messages messages = AppBeans.get(Messages.NAME);

    @Nullable
    public List<Map<String, Object>> generateRandomChartData(AbstractChartDescription abstractChartDescription) {
        List<Map<String, Object>> data = null;
        if (abstractChartDescription == null) {
            return null;
        }

        if (ChartType.SERIAL == abstractChartDescription.getType()) {
            SerialChartDescription chartDescription = (SerialChartDescription) abstractChartDescription;
            String categoryField = chartDescription.getCategoryField();

            data = new ArrayList<>();
            for (int i = 1; i < 6; i++) {
                HashMap<String, Object> map = new HashMap<>();
                data.add(map);

                map.put(categoryField, messages.getMessage(getClass(), "caption.category") + i);

                for (ChartSeries chartSeries : chartDescription.getSeries()) {
                    String valueField = chartSeries.getValueField();
                    String colorField = chartSeries.getColorField();
                    map.put(valueField, Math.abs(RandomUtils.nextInt(100)));
                    map.put(colorField, COLORS.get(RandomUtils.nextInt(6)));
                }
            }
        } else if (ChartType.PIE == abstractChartDescription.getType()) {
            PieChartDescription chartDescription = (PieChartDescription) abstractChartDescription;
            String titleField = chartDescription.getTitleField();
            String valueField = chartDescription.getValueField();
            String colorField = chartDescription.getColorField();

            data = new ArrayList<>();
            for (int i = 1; i < 6; i++) {
                HashMap<String, Object> map = new HashMap<>();
                data.add(map);

                map.put(titleField, messages.getMessage(getClass(), "caption.category") + i);
                map.put(valueField, Math.abs(RandomUtils.nextInt(100)));
                map.put(colorField, COLORS.get(RandomUtils.nextInt(6)));
            }
        }

        return data;
    }
}
