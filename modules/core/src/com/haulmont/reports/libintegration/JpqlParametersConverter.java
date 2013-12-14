/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.reports.exception.ReportingException;
import com.haulmont.yarg.loaders.ReportParametersConverter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class JpqlParametersConverter implements ReportParametersConverter {

    @Inject
    protected Persistence persistence;

    @Override
    public <T> T convert(Object input) {
        if (input instanceof Collection) {
            Collection collection = (Collection) input;
            if (CollectionUtils.isNotEmpty(collection)) {
                Object firstObject = collection.iterator().next();
                if (firstObject instanceof Entity) {
                    List<Object> entityIds = new ArrayList<>();
                    for (Object object : collection) {
                        entityIds.add(((Entity) object).getId());
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
                        entityIds.add(((Entity) object).getId());
                    }

                    return (T) entityIds;
                }
            }
        } else if (input instanceof Entity) {
            return (T) ((Entity) input).getId();
        }

        return (T) input;
    }

    private Object dbSpecificConvert(Object object) {
        try {

            return persistence.getDbTypeConverter().getSqlObject(object);
        } catch (SQLException e) {
            throw new ReportingException("An error occurred ");
        }
    }
}