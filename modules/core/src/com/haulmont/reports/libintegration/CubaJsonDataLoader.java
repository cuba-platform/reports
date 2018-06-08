package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.global.Resources;
import com.haulmont.reports.entity.JsonSourceType;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.yarg.loaders.impl.JsonDataLoader;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportQuery;
import com.haulmont.yarg.util.groovy.Scripting;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.haulmont.reports.entity.DataSet.*;
import static java.lang.String.format;

public class CubaJsonDataLoader extends JsonDataLoader {

    protected Scripting scripting;

    @Inject
    protected GroovyScriptParametersProvider groovyScriptParametersProvider;

    @Inject
    protected Resources resources;

    @Inject
    public CubaJsonDataLoader(Scripting scripting) {
        this.scripting = scripting;
    }

    @Override
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, BandData parentBand, Map<String, Object> reportParams) {

        List<Map<String, Object>> result = null;

        JsonSourceType jsonSourceType = (JsonSourceType) reportQuery.getAdditionalParams().get(JSON_SOURCE_TYPE);

        switch (jsonSourceType) {
            case GROOVY_SCRIPT:
                result = loadDataFromGroovyScript(reportQuery, parentBand, reportParams);
                break;
            case URL:
                result = loadDataFromUrl(reportQuery);
                break;
            case PARAMETER:
                result = loadDataFromParameter(reportQuery, reportParams);
                break;
        }

        return result;
    }

    protected List<Map<String, Object>> loadDataFromParameter(ReportQuery reportQuery, Map<String, Object> reportParams) {
        String jsonPathScript = getJsonPathScript(reportQuery);
        String json = readJsonFromParameter(reportQuery, reportParams);
        return super.extractScriptResult(json, jsonPathScript, reportQuery);
    }

    protected List<Map<String, Object>> loadDataFromGroovyScript(ReportQuery reportQuery, BandData parentBand,
                                                                 Map<String, Object> reportParams) {
        String jsonPathScript = getJsonPathScript(reportQuery);
        String json = readJsonFromGroovyScript(reportQuery, parentBand, reportParams);
        return super.extractScriptResult(json, jsonPathScript, reportQuery);
    }


    protected List<Map<String, Object>> loadDataFromUrl(ReportQuery reportQuery) {
        String jsonPathScript = getJsonPathScript(reportQuery);
        String json = readJsonFromUrl(getJsonSourceText(reportQuery));
        return super.extractScriptResult(json, jsonPathScript, reportQuery);
    }

    protected String readJsonFromGroovyScript(ReportQuery reportQuery, BandData parentBand, Map<String, Object> reportParams) {
        String jsonSourceText = getJsonSourceText(reportQuery);

        jsonSourceText = StringUtils.trim(jsonSourceText);
        if (jsonSourceText.endsWith(".groovy")) {
            jsonSourceText = resources.getResourceAsString(jsonSourceText);
        }

        Map<String, Object> scriptParams = groovyScriptParametersProvider.prepareParameters(reportQuery, parentBand, reportParams);
        return scripting.evaluateGroovy(jsonSourceText, scriptParams);
    }

    protected String readJsonFromUrl(String url) {
        String json;

        HttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager();
        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .build();

        HttpGet httpGet = new HttpGet(url);

        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            int httpStatus = httpResponse.getStatusLine().getStatusCode();
            if (httpStatus == HttpStatus.SC_OK) {
                HttpEntity httpEntity = httpResponse.getEntity();
                if (httpEntity != null) {
                    json = IOUtils.toString(httpEntity.getContent(), StandardCharsets.UTF_8);
                } else {
                    throw new RuntimeException(format("Unable to read json from %s\nHttpEntity is null", url));
                }
            } else {
                throw new RuntimeException(format("Unable to read json from %s\n%s", url, httpResponse.getStatusLine()));
            }
        } catch (IOException e) {
            throw new RuntimeException(format("Unable to read json from %s", url), e);
        } finally {
            connectionManager.shutdown();
        }

        return json;
    }

    protected String getJsonSourceText(ReportQuery reportQuery) {
        return (String) reportQuery.getAdditionalParams().get(JSON_SOURCE_TEXT);
    }

    protected String getJsonPathScript(ReportQuery reportQuery) {
        return (String) reportQuery.getAdditionalParams().get(JSON_PATH_QUERY);
    }

    protected String readJsonFromParameter(ReportQuery reportQuery, Map<String, Object> reportParams) {
        ReportInputParameter jsonSourceInputParameter = (ReportInputParameter) reportQuery.getAdditionalParams().get(JSON_INPUT_PARAMETER);
        return reportParams.get(jsonSourceInputParameter.getAlias()).toString();
    }
}
