/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.cuba.report.formatters.xls.options;

import com.haulmont.cuba.report.formatters.xls.StyleOption;
import org.apache.poi.hssf.usermodel.HSSFSheet;

/**
 * @author artamonov
 * @version $Id$
 */
public class AutoWidthOption implements StyleOption {

    private HSSFSheet resultSheet;

    private int resultColumnIndex;

    public AutoWidthOption(HSSFSheet resultSheet, int resultColumnIndex) {
        this.resultSheet = resultSheet;
        this.resultColumnIndex = resultColumnIndex;
    }

    @Override
    public void apply() {
        resultSheet.autoSizeColumn(resultColumnIndex);
    }
}
