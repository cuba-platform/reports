/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports.formatters.xls;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;

import java.util.HashMap;
import java.util.Map;

/**
 * @author artamonov
 * @version $Id$
 */
public class XlsStyleCache {

    private Map<HSSFStyleCacheKey, HSSFCellStyle> cellStyles = new HashMap<>();

    private Map<HSSFStyleCacheKey, HSSFCellStyle> namedCellStyles = new HashMap<>();

    private Map<String, HSSFCellStyle> styleMap = new HashMap<>();

    public HSSFCellStyle processCellStyle(HSSFCellStyle cellStyle) {
        HSSFCellStyle cachedCellStyle = cellStyles.get(new HSSFStyleCacheKey(cellStyle));
        if (cachedCellStyle == null)
            cellStyles.put(new HSSFStyleCacheKey(cellStyle), cellStyle);
        else
            cellStyle = cachedCellStyle;

        return cellStyle;
    }

    public HSSFCellStyle getCellStyleByTemplate(HSSFCellStyle templateCellStyle) {
        return cellStyles.get(new HSSFStyleCacheKey(templateCellStyle));
    }

    public void addCachedStyle(HSSFCellStyle templateCellStyle, HSSFCellStyle cellStyle) {
        cellStyles.put(new HSSFStyleCacheKey(templateCellStyle), cellStyle);
    }

    public void addNamedStyle(HSSFCellStyle cellStyle) {
        styleMap.put(cellStyle.getUserStyleName(), cellStyle);
    }

    public HSSFCellStyle getStyleByName(String styleName) {
        return styleMap.get(styleName);
    }

    public HSSFCellStyle getNamedCachedStyle(HSSFCellStyle namedCellStyle) {
        return namedCellStyles.get(new HSSFStyleCacheKey(namedCellStyle));
    }

    public void addCachedNamedStyle(HSSFCellStyle namedCellStyle, HSSFCellStyle cellStyle) {
        namedCellStyles.put(new HSSFStyleCacheKey(namedCellStyle), cellStyle);
    }
}