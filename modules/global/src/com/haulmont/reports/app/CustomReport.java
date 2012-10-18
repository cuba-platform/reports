/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.app;

import com.haulmont.reports.entity.Report;

import java.util.Map;

/**
 * @author degtyarjov
 * @version $Id$
 */
public interface CustomReport {
    byte[] createReport(Report report, Map<String, Object> params);
}
