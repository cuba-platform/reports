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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.reports.ReportingConfig;
import com.haulmont.reports.entity.CustomTemplateDefinedBy;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.reports.exception.ReportingException;
import com.haulmont.yarg.formatters.CustomReport;
import com.haulmont.yarg.structure.BandData;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class CustomFormatter implements com.haulmont.yarg.formatters.CustomReport {
    private static final Logger log = LoggerFactory.getLogger(CustomFormatter.class);

    public static final String PARAMS = "params";
    private static final String ROOT_BAND = "rootBand";
    private static final String PATH_GROOVY_FILE = "(\\w[\\w\\d_-]*/)*(\\w[\\w\\d-_]*\\.groovy)";

    protected static final ScheduledExecutorService executor =
            Executors.newScheduledThreadPool(3,
                    new ThreadFactoryBuilder()
                            .setNameFormat("ReportCustomFormatter-%d")
                            .build()
            );

    protected Report report;
    protected ReportTemplate template;
    protected Map<String, Object> params;
    protected Scripting scripting;
    protected Configuration configuration;
    protected ReportingConfig reportingConfig;

    public CustomFormatter(Report report, ReportTemplate template) {
        this.report = report;
        this.template = template;
        this.scripting = AppBeans.get(Scripting.class);
        this.configuration = AppBeans.get(Configuration.class);
        this.reportingConfig = configuration.getConfig(ReportingConfig.class);
    }

    @Override
    public byte[] createReport(com.haulmont.yarg.structure.Report report, BandData rootBand, Map<String, Object> params) {
        this.params = params;//we set params here because they might change inside YARG (for instance - default values)
        return createDocument(rootBand);
    }

    public byte[] createDocument(BandData rootBand) {
        String customDefinition = template.getCustomDefinition();
        CustomTemplateDefinedBy definedBy = template.getCustomDefinedBy();
        if (CustomTemplateDefinedBy.CLASS == definedBy) {
            return generateReportWithClass(rootBand, customDefinition);
        } else if (CustomTemplateDefinedBy.SCRIPT == definedBy) {
            return generateReportWithScript(rootBand, customDefinition);
        } else if (CustomTemplateDefinedBy.URL == definedBy) {
            return generateReportWithUrl(rootBand, customDefinition);
        } else {
            throw new ReportingException(
                    format("The value of \"Defined by\" field is not supported [%s]", definedBy));
        }
    }

    protected byte[] generateReportWithClass(BandData rootBand, String customDefinition) {
        Class clazz = scripting.loadClassNN(customDefinition);
        try {
            CustomReport customReport = (CustomReport) clazz.newInstance();
            return customReport.createReport(report, rootBand, params);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ReportingException(
                    format("Could not instantiate class for custom template [%s]. Report name [%s]",
                            template.getCustomDefinition(), report.getName()), e);
        }
    }

    protected byte[] generateReportWithScript(BandData rootBand, String customDefinition) {
        Object result;

        if (customDefinition.startsWith("/")) {
            customDefinition = StringUtils.removeStart(customDefinition, "/");
        }

        Map<String, Object> scriptParams = new HashMap<>();
        scriptParams.put(PARAMS, params);
        scriptParams.put(ROOT_BAND, rootBand);

        if(Pattern.matches(PATH_GROOVY_FILE, customDefinition)) {
            result = scripting.runGroovyScript(customDefinition, scriptParams);
        } else {
            result = scripting.evaluateGroovy(customDefinition, scriptParams);
        }

        if (result instanceof byte[]) {
            return (byte[]) result;
        } else if (result instanceof CharSequence) {
            return result.toString().getBytes(StandardCharsets.UTF_8);
        } else {
            throw new ReportingException(
                    format("Result returned from custom report is of type %s " +
                            "but only byte[] and strings are supported", result.getClass()));
        }
    }

    protected byte[] generateReportWithUrl(BandData rootBand, String customDefinition) {
        Map<String, Object> convertedParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() instanceof Date) {
                convertedParams.put(entry.getKey(), new FormattedDate((Date) entry.getValue()));
            } else {
                convertedParams.put(entry.getKey(), entry.getValue());
            }
        }

        convertedParams.put(ROOT_BAND, rootBand);

        String url = scripting.evaluateGroovy("return \"" + customDefinition + "\"", convertedParams).toString();

        try {
            Future<byte[]> future = executor.submit(() ->
                    doReadBytesFromUrl(url)
            );

            byte[] bytes = future.get(reportingConfig.getCurlTimeout(), TimeUnit.SECONDS);

            return bytes;
        } catch (InterruptedException e) {
            throw new ReportingException(format("Reading data from url [%s] has been interrupted", url), e);
        } catch (ExecutionException e) {
            throw new ReportingException(format("An error occurred while reading data from url [%s]", url), e);
        } catch (TimeoutException e) {
            throw new ReportingException(format("Reading data from url [%s] has been terminated by timeout", url), e);
        }
    }

    protected static class FormattedDate extends Date {
        private static final long serialVersionUID = 6328140953372636008L;
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss aaa");

        public FormattedDate(Date date) {
            super(date.getTime());
        }

        @Override
        public String toString() {
            return dateFormat.format(this);
        }
    }

    protected byte[] doReadBytesFromUrl(String url) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        Process proc = null;
        try {
            Runtime runtime = Runtime.getRuntime();
            String curlToolPath = reportingConfig.getCurlPath();
            String curlToolParams = reportingConfig.getCurlParams();
            String command = format("%s %s %s", curlToolPath, curlToolParams, url);
            log.info("Reporting::CustomFormatter::Trying to load report from URL: [{}]", url);
            proc = runtime.exec(command);

            inputStream = proc.getInputStream();

            String tmpFileName = configuration.getConfig(GlobalConfig.class)
                    .getTempDir() + "/" + RandomStringUtils.randomAlphanumeric(12);

            outputStream = new FileOutputStream(tmpFileName);
            IOUtils.copy(inputStream, outputStream);
            IOUtils.closeQuietly(outputStream);

            File tempFile = new File(tmpFileName);
            byte[] bytes = FileUtils.readFileToByteArray(tempFile);
            FileUtils.deleteQuietly(tempFile);

            return bytes;
        } catch (IOException e) {
            throw new ReportingException(format("Error while accessing remote url: [%s].", url), e);
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(inputStream);

            if (proc != null) {
                proc.destroy();
            }
        }
    }
}