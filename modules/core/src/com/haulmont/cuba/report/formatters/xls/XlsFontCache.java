/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.cuba.report.formatters.xls;

import org.apache.commons.lang.ObjectUtils;
import org.apache.poi.hssf.usermodel.HSSFFont;

import java.util.ArrayList;
import java.util.List;

/**
 * Font cache for XlsFormatter
 *
 * @author artamonov
 * @version $Id$
 */
public class XlsFontCache {

    private List<HSSFFont> fonts = new ArrayList<>();

    public XlsFontCache() { }

    public HSSFFont getFont(HSSFFont font){
        for (HSSFFont cacheFont : fonts) {
            if (fontEquals(cacheFont, font))
                return cacheFont;
        }
        return null;
    }

    public HSSFFont processFont(HSSFFont font){
        for (HSSFFont cacheFont : fonts) {
            if (fontEquals(cacheFont, font))
                return cacheFont;
        }
        fonts.add(font);
        return font;
    }

    private boolean fontEquals(HSSFFont aFont, HSSFFont bFont) {
        if (aFont == bFont)
            return true;
        if (ObjectUtils.equals(aFont, bFont))
            return true;
        if (aFont != null){
            if (aFont.getItalic() != bFont.getItalic())
                return false;
            if (aFont.getStrikeout() != bFont.getStrikeout())
                return false;
            if (aFont.getBoldweight() != bFont.getBoldweight())
                return false;
            if (aFont.getCharSet() != bFont.getCharSet())
                return false;
            if (aFont.getColor() != bFont.getColor())
                return false;
            if (aFont.getFontHeight() != bFont.getFontHeight())
                return false;
            if (aFont.getFontHeightInPoints() != bFont.getFontHeightInPoints())
                return false;
            if (!aFont.getFontName().equals(bFont.getFontName()))
                return false;
            if (aFont.getTypeOffset() != bFont.getTypeOffset())
                return false;
            if (aFont.getUnderline() != bFont.getUnderline())
                return false;
            return true;
        }
        return false;
    }
}
