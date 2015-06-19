/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.reports.entity.charts.*;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.AbstractFormatter;
import com.haulmont.yarg.structure.BandData;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class ChartFormatter extends AbstractFormatter {
    public ChartFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
        this.rootBand = formatterFactoryInput.getRootBand();
        this.reportTemplate = formatterFactoryInput.getReportTemplate();
    }

    @Override
    public void renderDocument() {
        String chartJson = null;
        AbstractChartDescription chartDescription = ((ReportTemplate) reportTemplate).getChartDescription();
        if (chartDescription != null) {
            if (chartDescription.getType() == ChartType.PIE) {
                chartJson = convertPieChart((PieChartDescription) chartDescription);
            } else if (chartDescription.getType() == ChartType.SERIAL) {
                chartJson = convertSerialChart((SerialChartDescription) chartDescription);
            }
        }
        try {
            IOUtils.write(chartJson, outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String convertSerialChart(SerialChartDescription description) {
        List<Map<String, Object>> data = new ArrayList<>();
        List<BandData> childrenByName = rootBand.getChildrenByName(description.getBandName());
        for (BandData bandData : childrenByName) {
            data.add(bandData.getData());
        }

        return new ChartToJsonConverter().convertSerialChart(description, data);
    }

    protected String convertPieChart(PieChartDescription description) {
        List<Map<String, Object>> data = new ArrayList<>();
        List<BandData> childrenByName = rootBand.getChildrenByName(description.getBandName());
        for (BandData bandData : childrenByName) {
            data.add(bandData.getData());
        }

        return new ChartToJsonConverter().convertPieChart(description, data);
    }
}
