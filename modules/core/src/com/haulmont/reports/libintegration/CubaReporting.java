/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.ReportTemplate;
import org.apache.commons.lang.StringUtils;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class CubaReporting extends Reporting {
    public static final String REPORT_FILE_NAME_KEY = "__REPORT_FILE_NAME";

    @Override
    protected String resolveOutputFileName(Report report, ReportTemplate reportTemplate, BandData rootBand) {
        String generatedReportFileName = (String) rootBand.getData().get(REPORT_FILE_NAME_KEY);
        if (StringUtils.isNotBlank(generatedReportFileName)) {
            return generatedReportFileName;
        } else {
            return super.resolveOutputFileName(report, reportTemplate, rootBand);
        }
    }
}
