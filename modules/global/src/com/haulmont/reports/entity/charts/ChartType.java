/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.entity.charts;

import com.haulmont.chile.core.datatypes.impl.EnumClass;
import org.apache.commons.lang.ObjectUtils;

/**
 * @author degtyarjov
 * @version $Id$
 */
public enum ChartType implements EnumClass<String> {
    PIE("pie"), SERIAL("serial");

    private String id;

    @Override
    public String getId() {
        return id;
    }

    private ChartType(String id) {
        this.id = id;
    }

    public static ChartType fromId(String id) {
        for (ChartType type : ChartType.values()) {
            if (ObjectUtils.equals(type.getId(), id)) {
                return type;
            }
        }
        return null;
    }
}