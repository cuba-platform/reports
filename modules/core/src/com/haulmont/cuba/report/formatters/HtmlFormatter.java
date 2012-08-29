/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.cuba.report.formatters;

import com.haulmont.cuba.core.app.FileStorageAPI;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.ConfigProvider;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.report.Band;
import com.haulmont.cuba.report.ReportOutputType;
import com.haulmont.cuba.report.ReportingConfig;
import com.haulmont.cuba.report.exception.ReportingException;
import com.haulmont.cuba.report.exception.UnsupportedFormatException;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Engine for create reports with HTML templates and FreeMarker markup
 *
 * @author artamonov
 * @version $Id$
 */
public class HtmlFormatter extends AbstractFormatter {
    private static Log log = LogFactory.getLog(HtmlFormatter.class);

    public HtmlFormatter() {
        registerReportExtension("htm");
        registerReportExtension("html");

        registerReportOutput(ReportOutputType.HTML);
        registerReportOutput(ReportOutputType.PDF);

        defaultOutputType = ReportOutputType.HTML;
    }

    @Override
    public void createDocument(Band rootBand, ReportOutputType outputType, OutputStream outputStream) {

        if (templateFile == null)
            throw new NullPointerException();

        switch (outputType) {
            case HTML:
                writeHtmlDocument(rootBand, outputStream);
                break;

            case PDF:
                ByteArrayOutputStream htmlOuputStream = new ByteArrayOutputStream();
                writeHtmlDocument(rootBand, htmlOuputStream);

                String htmlContent = new String(htmlOuputStream.toByteArray());

                renderPdfDocument(htmlContent, outputStream);
                break;

            default:
                throw new UnsupportedFormatException();
        }
    }

    private void renderPdfDocument(String htmlContent, OutputStream outputStream) {
        ITextRenderer renderer = new ITextRenderer();
        try {
            File tmpFile = File.createTempFile("htmlReport", ".htm");
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(tmpFile));
            dataOutputStream.write(htmlContent.getBytes(Charset.forName("UTF-8")));
            dataOutputStream.close();

            loadFonts(renderer);

            String url = tmpFile.toURI().toURL().toString();
            renderer.setDocument(url);

            renderer.layout();
            renderer.createPDF(outputStream);

            FileUtils.deleteQuietly(tmpFile);
        } catch (Exception e) {
            throw new ReportingException(e);
        }
    }

    private void loadFonts(ITextRenderer renderer) {
        ReportingConfig serverConfig = ConfigProvider.getConfig(ReportingConfig.class);
        if (StringUtils.isNotBlank(serverConfig.getPdfFontsDirectory())) {
            File systemFontsDir = new File(serverConfig.getPdfFontsDirectory());
            loadFontsFromDirectory(renderer, systemFontsDir);
        }
    }

    private void loadFontsFromDirectory(ITextRenderer renderer, File fontsDir) {
        if (fontsDir.exists()) {
            if (fontsDir.isDirectory()) {
                log.debug("Use fonts from: " + fontsDir.getPath());
                File[] files = fontsDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        String lower = name.toLowerCase();
                        return lower.endsWith(".otf") || lower.endsWith(".ttf");
                    }
                });
                for (File file : files) {
                    try {
                        // Usage of some fonts may be not permitted
                        renderer.getFontResolver().addFont(file.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    } catch (DocumentException | IOException e) {
                        log.warn(e.getMessage());
                    }
                }
            } else
                log.warn(String.format("File %s is not a directory", fontsDir.getAbsolutePath()));
        } else {
            log.debug("Fonts directory does not exist: " + fontsDir.getPath());
        }
    }

    private void writeHtmlDocument(Band rootBand, OutputStream outputStream) {
        Map templateModel = getTemplateModel(rootBand);

        Template htmlTemplate = getTemplate();
        Writer htmlWriter = new OutputStreamWriter(outputStream);

        try {
            htmlTemplate.process(templateModel, htmlWriter);
            htmlWriter.close();
        } catch (TemplateException fmException) {
            log.error("FreeMarker template exception", fmException);
            throw new ReportingException("FreeMarkerException: " + fmException.getMessage());
        } catch (ReportingException e) {
            throw e;
        } catch (Exception e) {
            throw new ReportingException(e);
        }
    }

    private Map getTemplateModel(Band rootBand) {
        Map<String, Object> model = new HashMap<>();
        model.put(rootBand.getName(), getBandModel(rootBand));
        return model;
    }

    private Map getBandModel(Band band) {
        Map<String, Object> model = new HashMap<>();

        Map<String, Object> bands = new HashMap<>();
        for (String bandName : band.getChildrenBands().keySet()) {
            List<Band> subBands = band.getChildrenBands().get(bandName);
            List<Map> bandModels = new ArrayList<>();
            for (Band child : subBands)
                bandModels.add(getBandModel(child));

            bands.put(bandName, bandModels);
        }
        model.put("bands", bands);

        model.put("fields", band.getData());

        return model;
    }

    private Template getTemplate() {
        if (templateFile == null)
            throw new NullPointerException();

        String templateContent;
        FileStorageAPI storageAPI = AppBeans.get(FileStorageAPI.NAME);
        try {
            byte[] templateBytes = storageAPI.loadFile(templateFile);
            templateContent = new String(templateBytes);
        } catch (FileStorageException e) {
            throw new ReportingException(e);
        }
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        stringLoader.putTemplate(templateFile.getFileName(), templateContent);

        Configuration fmConfiguration = new Configuration();
        fmConfiguration.setTemplateLoader(stringLoader);
        fmConfiguration.setDefaultEncoding("UTF-8");

        Template htmlTemplate;
        try {
            htmlTemplate = fmConfiguration.getTemplate(templateFile.getFileName());
        } catch (Exception e) {
            throw new ReportingException(e);
        }
        return htmlTemplate;
    }
}
