/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Degtyarjov
 * Created: 26.09.13 16:41
 *
 * $Id$
 */
package com.haulmont.reports.libintegration;

import com.haulmont.chile.core.datatypes.Datatype;
import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.yarg.formatters.impl.DefaultFormatProvider;

import javax.inject.Inject;

public class CubaFieldFormatProvider implements DefaultFormatProvider {
    @Inject
    protected UserSessionSource userSessionSource;

    @Override
    public String format(Object o) {
        if (o != null) {
            Datatype datatype = Datatypes.get(o.getClass());
            return datatype != null ? datatype.format(o, userSessionSource.getLocale()) : o.toString();
        } else {
            return null;
        }
    }
}
