package com.haulmont.reports.formatters.xls.options;

import com.haulmont.reports.formatters.xls.StyleOption;
import org.apache.poi.hssf.usermodel.HSSFSheet;

/**
 * @author kozyaikin
 * @version $Id$
 */
public class CustomWidthOption implements StyleOption {

    private HSSFSheet resultSheet;

    private int resultColumnIndex;
    private int width;

    public CustomWidthOption(HSSFSheet resultSheet, int resultColumnIndex, int width) {
        this.resultSheet = resultSheet;
        this.resultColumnIndex = resultColumnIndex;
        this.width=width;
    }

    @Override
    public void apply() {
        resultSheet.setColumnWidth(resultColumnIndex, width);
    }
}
