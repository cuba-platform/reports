/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports.libintegration;

import com.haulmont.chile.core.datatypes.Datatype;
import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.chile.core.model.Instance;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.core.sys.jpql.model.Entity;
import com.haulmont.yarg.formatters.impl.DefaultFormatProvider;

import javax.inject.Inject;

/**
 */
public class CubaFieldFormatProvider implements DefaultFormatProvider {

    @Inject
    protected UserSessionSource userSessionSource;

    @Inject
    protected Messages messages;

    @Override
    public String format(Object o) {
        if (o != null) {
            Datatype datatype = Datatypes.get(o.getClass());
            if (datatype != null) {
                if (userSessionSource.checkCurrentUserSession()) {
                    return datatype.format(o, userSessionSource.getLocale());
                } else {
                    return datatype.format(o);
                }
            } else if (o instanceof Enum) {
                return messages.getMessage((Enum) o);
            } else if (o instanceof Instance) {
                return ((Instance) o).getInstanceName();
            } else {
                return String.valueOf(o);
            }
        } else {
            return null;
        }
    }
}