/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports.formatters.xls;

import org.apache.poi.hssf.usermodel.HSSFFont;

import java.util.HashMap;
import java.util.Map;

/**
 * Font cache for XlsFormatter
 *
 * @author artamonov
 * @version $Id$
 */
public class XlsFontCache {
    private Map<HSSFFontCacheKey, HSSFFont> fonts = new HashMap<>();

    public HSSFFont getFontByTemplate(HSSFFont font){
        return fonts.get(new HSSFFontCacheKey(font));
    }

    public void addCachedFont(HSSFFont templateFont, HSSFFont font) {
        fonts.put(new HSSFFontCacheKey(templateFont), font);
    }
}