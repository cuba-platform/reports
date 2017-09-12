package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.global.*;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportQuery;
import org.codehaus.groovy.runtime.MethodClosure;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component(GroovyScriptParametersProvider.NAME)
public class CubaGroovyScriptParametersProvider implements GroovyScriptParametersProvider {

    @Override
    public Map<String, Object> prepareParameters(ReportQuery reportQuery, BandData parentBand, Map<String, Object> reportParameters) {

        Map<String, Object> scriptParams = new HashMap<>();
        UserSessionSource userSessionSource = AppBeans.get(UserSessionSource.class);
        scriptParams.put("reportQuery", reportQuery);
        scriptParams.put("parentBand", parentBand);
        scriptParams.put("params", reportParameters);
        scriptParams.put("persistence", AppBeans.get(Persistence.class));
        scriptParams.put("metadata", AppBeans.get(Metadata.class));
        scriptParams.put("dataManager", AppBeans.get(DataManager.class));
        scriptParams.put("security", AppBeans.get(Security.class));
        scriptParams.put("timeSource", AppBeans.get(TimeSource.class));
        scriptParams.put("userSession", userSessionSource.getUserSession());
        scriptParams.put("userSessionSource", userSessionSource);
        scriptParams.put("transactional", new MethodClosure(this, "transactional"));
        scriptParams.put("validationException", new MethodClosure(this, "validationException"));

        return scriptParams;
    }
}
