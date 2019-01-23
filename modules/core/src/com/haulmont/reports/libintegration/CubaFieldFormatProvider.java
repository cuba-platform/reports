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

import com.haulmont.chile.core.datatypes.Datatype;
import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.chile.core.model.Instance;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.yarg.formatters.impl.DefaultFormatProvider;

import javax.inject.Inject;

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