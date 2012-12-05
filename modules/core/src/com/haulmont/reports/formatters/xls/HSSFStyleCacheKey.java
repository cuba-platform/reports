/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.reports.formatters.xls;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;

import java.io.Serializable;

/**
 * @author artamonov
 * @version $Id$
 */
public class HSSFStyleCacheKey implements Serializable {

    protected final HSSFCellStyle style;

    public HSSFStyleCacheKey(HSSFCellStyle style) {
        this.style = style;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HSSFStyleCacheKey)
            return style.formatEquals(((HSSFStyleCacheKey) obj).style);
        else
            return false;
    }

    @Override
    public int hashCode() {
        return style.formatHashCode();
    }
}