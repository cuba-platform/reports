/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.reports.libintegration;


import com.haulmont.yarg.util.groovy.Scripting;

import javax.inject.Inject;
import java.util.Map;

public class ReportingScriptingImpl implements Scripting {

    @Inject
    private com.haulmont.cuba.core.global.Scripting  scripting;

    @Override
    public <T> T evaluateGroovy(String s, Map<String, Object> stringObjectMap) {
        return scripting.evaluateGroovy(s, stringObjectMap);
    }
}
