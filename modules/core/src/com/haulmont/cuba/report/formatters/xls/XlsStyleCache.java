/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.cuba.report.formatters.xls;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author artamonov
 * @version $Id$
 */
public class XlsStyleCache {

    private List<HSSFCellStyle> cellStyles = new ArrayList<>();

    private Map<String, HSSFCellStyle> styleMap = new HashMap<>();

    public XlsStyleCache() {
    }

    public HSSFCellStyle processCellStyle(HSSFCellStyle cellStyle) {
        for (HSSFCellStyle cacheStyle : cellStyles) {
            if (cacheStyle.formatEquals(cellStyle))
                return cacheStyle;
        }
        cellStyles.add(cellStyle);
        return cellStyle;
    }

    public void addNamedStyle(HSSFCellStyle cellStyle) {
        styleMap.put(cellStyle.getUserStyleName(), cellStyle);
    }

    public HSSFCellStyle getStyleByName(String styleName) {
        return styleMap.get(styleName);
    }
}
