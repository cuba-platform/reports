/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;
import com.haulmont.yarg.structure.BandOrientation;
import org.apache.commons.lang.ObjectUtils;

public enum Orientation implements EnumClass<Integer> {
    HORIZONTAL(0, BandOrientation.HORIZONTAL),
    VERTICAL(1, BandOrientation.VERTICAL),
    CROSS(2, BandOrientation.CROSS);

    private Integer id;
    private BandOrientation bandOrientation;

    Orientation(Integer id, BandOrientation bandOrientation) {
        this.id = id;
        this.bandOrientation = bandOrientation;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public BandOrientation getBandOrientation() {
        return bandOrientation;
    }

    public static Orientation fromId(Integer id) {
        for (Orientation type : Orientation.values()) {
            if (ObjectUtils.equals(type.getId(), id)) {
                return type;
            }
        }
        return null;
    }

    public static Orientation fromBandOrientation(BandOrientation orientation) {
        for (Orientation type : Orientation.values()) {
            if (type.getBandOrientation() == orientation) {
                return type;
            }
        }
        return null;
    }
}
