/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.chile.core.datatypes.impl.EnumClass;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.IdProxy;
import com.haulmont.yarg.loaders.ReportParametersConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JpqlParametersConverter implements ReportParametersConverter {

    @Inject
    protected Persistence persistence;

    @Override
    public <T> T convert(Object input) {
        if (input instanceof EnumClass) {
            return (T) ((EnumClass) input).getId();
        } else if (input instanceof Collection) {
            Collection collection = (Collection) input;
            if (CollectionUtils.isNotEmpty(collection)) {
                Object firstObject = collection.iterator().next();
                if (firstObject instanceof Entity) {
                    List<Object> entityIds = new ArrayList<>();
                    for (Object object : collection) {
                        Object id = ((Entity) object).getId();
                        if (id instanceof IdProxy) {
                            entityIds.add(((IdProxy) id).getNN());
                        } else {
                            entityIds.add(id);
                        }
                    }

                    return (T) entityIds;
                }
            }
        } else if (input instanceof Object[]) {
            Object[] objects = (Object[]) input;
            if (ArrayUtils.isNotEmpty(objects)) {
                Object firstObject = objects[0];
                if (firstObject instanceof Entity) {
                    List<Object> entityIds = new ArrayList<>();
                    for (Object object : objects) {
                        Object id = ((Entity) object).getId();
                        if (id instanceof IdProxy) {
                            entityIds.add(((IdProxy) id).getNN());
                        } else {
                            entityIds.add(id);
                        }
                    }

                    return (T) entityIds;
                }
            }
        } else if (input instanceof Entity) {
            Object id = ((Entity) input).getId();
            if (id instanceof IdProxy) {
                return (T) ((IdProxy) id).getNN();
            } else {
                return (T) id;
            }
        }

        return (T) input;
    }

    private Object dbSpecificConvert(Object object) {
        return persistence.getDbTypeConverter().getSqlObject(object);
    }
}