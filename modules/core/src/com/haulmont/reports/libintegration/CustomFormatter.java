/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

/**
 *
 * @author Degtyarjov
 * @version $Id$
 */
package com.haulmont.reports.libintegration;

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
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import static java.lang.String.format;

public class CustomFormatter implements com.haulmont.yarg.formatters.CustomReport {
    protected static ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
    protected static Log log = LogFactory.getLog(CustomFormatter.class);

    protected Report report;
    protected ReportTemplate template;
    protected Map<String, Object> params;
    protected Scripting scripting;
    protected Configuration configuration;
    protected ReportingConfig reportingConfig;

    public CustomFormatter(Report report, ReportTemplate template, Map<String, Object> params) {
        this.report = report;
        this.params = params;
        this.template = template;
        this.scripting = AppBeans.get(Scripting.class);
        this.configuration = AppBeans.get(Configuration.class);
        this.reportingConfig = configuration.getConfig(ReportingConfig.class);
    }

    @Override
    public byte[] createReport(com.haulmont.yarg.structure.Report report, BandData rootBand, Map<String, Object> params) {
        return createDocument(rootBand);
    }

    public byte[] createDocument(BandData rootBand) {
        String customDefinition = template.getCustomDefinition();
        CustomTemplateDefinedBy definedBy = template.getCustomDefinedBy();
        if (CustomTemplateDefinedBy.CLASS == definedBy) {
            return generateReportWithClass(rootBand, customDefinition);
        } else if (CustomTemplateDefinedBy.SCRIPT == definedBy) {
            return generateReportWithScript(customDefinition);
        } else if (CustomTemplateDefinedBy.URL == definedBy) {
            return generateReportWithUrl(customDefinition);
        } else {
            throw new ReportingException(
                    format("The value of \"Defined by\" field is not supported [%s]", definedBy));
        }
    }

    protected byte[] generateReportWithClass(BandData rootBand, String customDefinition) {
        Class clazz = scripting.loadClass(customDefinition);
        try {
            CustomReport customReport = (CustomReport) clazz.newInstance();
            return customReport.createReport(report, rootBand, params);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ReportingException(
                    format("Could not instantiate class for custom template [%s]. Report name [%s]",
                            template.getCustomDefinition(), report.getName()), e);
        }
    }

    protected byte[] generateReportWithScript(String customDefinition) {
        if (customDefinition.startsWith("/")) {
            customDefinition = StringUtils.removeStart(customDefinition, "/");
        }
        Object result = scripting.runGroovyScript(customDefinition,
                Collections.<String, Object>singletonMap("params", params));
        if (result instanceof byte[]) {
            return (byte[]) result;
        } else if (result instanceof CharSequence) {
            return result.toString().getBytes();
        } else {
            throw new ReportingException(
                    format("Result returned from custom report is of type %s " +
                            "but only byte[] and strings are supported", result.getClass()));
        }
    }

    protected byte[] generateReportWithUrl(String customDefinition) {
        Map<String, Object> convertedParams = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() instanceof Date) {
                convertedParams.put(entry.getKey(), new FormattedDate((Date) entry.getValue()));
            } else {
                convertedParams.put(entry.getKey(), entry.getValue());
            }
        }

        final String url = scripting.evaluateGroovy("return \"" + customDefinition + "\"", convertedParams).toString();

        try {
            Future<byte[]> future = executor.submit(new Callable<byte[]>() {
                @Override
                public byte[] call() throws Exception {
                    return doReadBytesFromUrl(url);
                }
            });

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

    private static class FormattedDate extends Date {
        private static final long serialVersionUID = 6328140953372636008L;
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss aaa");

        private FormattedDate(Date date) {
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
            log.info(format("Reporting::CustomFormatter::Trying to load report from URL: [%s]", url));
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
