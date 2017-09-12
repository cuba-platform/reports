package com.haulmont.reports.libintegration;

import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportQuery;

import java.util.Map;


/**
 * Realization of this interface is intended to prepare a map of parameters filled with beans or other objects
 */

public interface GroovyScriptParametersProvider {

    String NAME = "report_GroovyParametersProvider";

    /**
     * Prepares and return the map of objects
     * @param reportParameters - parameters to include into the map of parameters
     * @return map of objects
     */
    Map<String, Object> prepareParameters(ReportQuery reportQuery, BandData parentBand, Map<String, Object> reportParameters);
}
