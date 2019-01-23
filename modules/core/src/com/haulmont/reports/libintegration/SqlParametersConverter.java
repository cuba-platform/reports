/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.chile.core.datatypes.impl.EnumClass;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.entity.BaseDbGeneratedIdEntity;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.IdProxy;
import com.haulmont.yarg.loaders.ReportParametersConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SqlParametersConverter implements ReportParametersConverter {

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
                            entityIds.add(dbSpecificConvert(((IdProxy) id).getNN()));
                        } else {
                            entityIds.add(dbSpecificConvert(id));
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
                            entityIds.add(dbSpecificConvert(((IdProxy) id).getNN()));
                        } else {
                            entityIds.add(dbSpecificConvert(id));
                        }
                    }

                    return (T) entityIds;
                }
            }
        } else if (input instanceof Entity) {
            Object id = ((Entity) input).getId();
            if (id instanceof IdProxy) {
                return (T) dbSpecificConvert(((IdProxy) id).getNN());
            } else {
                return (T) dbSpecificConvert(id);
            }
        }

        return (T) dbSpecificConvert(input);
    }

    private Object dbSpecificConvert(Object object) {
        return persistence.getDbTypeConverter().getSqlObject(object);
    }
}
