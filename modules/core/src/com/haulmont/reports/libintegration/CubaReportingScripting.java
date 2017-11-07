/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.yarg.util.groovy.Scripting;

import javax.inject.Inject;
import java.util.Map;

public class CubaReportingScripting implements Scripting {

    @Inject
    private com.haulmont.cuba.core.global.Scripting  scripting;

    @Override
    public <T> T evaluateGroovy(String s, Map<String, Object> stringObjectMap) {
        return scripting.evaluateGroovy(s, stringObjectMap);
    }
}