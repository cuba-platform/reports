/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.reports.libintegration;

import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.app.DataWorker;
import com.haulmont.cuba.core.app.FileStorageAPI;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.*;
import com.haulmont.reports.ReportingConfig;
import com.haulmont.yarg.exception.ReportFormattingException;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.HtmlFormatter;
import com.haulmont.yarg.structure.BandData;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;
import org.xhtmlrenderer.resource.ImageResource;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.*;

import static java.lang.String.format;

public class CubaHtmlFormatter extends HtmlFormatter {
    protected static final String CUBA_FONTS_DIR = "/cuba/fonts";

    public static final String FS_PROTOCOL_PREFIX = "fs://";
    public static final String WEB_APP_PREFIX = "web://";
    public static final String CORE_APP_PREFIX = "core://";

    protected Log log = LogFactory.getLog(getClass());

    protected final ReportingConfig reportingConfig = AppBeans.get(Configuration.class).getConfig(ReportingConfig.class);
    protected int entityMapMaxDeep = reportingConfig.getEntityTreeModelMaxDeep();
    protected int externalImagesTimeoutSec = reportingConfig.getHtmlExternalResourcesTimeoutSec();

    public CubaHtmlFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
    }

    //todo degtyarjov, artamonov - get rid of custom processing of file descriptors, use field formats
    // we can append <content> with Base64 to html and put reference to <img> for html
    // and some custom reference if we need pdf and then implement ResourcesITextUserAgentCallback which will
    // take base64 from appropriate content
    protected void renderPdfDocument(String htmlContent, OutputStream outputStream) {
        ITextRenderer renderer = new ITextRenderer();
        try {
            File tmpFile = File.createTempFile("htmlReport", ".htm");
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(tmpFile));
            dataOutputStream.write(htmlContent.getBytes(Charset.forName("UTF-8")));
            dataOutputStream.close();

            loadFonts(renderer);

            String url = tmpFile.toURI().toURL().toString();
            renderer.setDocument(url);

            ResourcesITextUserAgentCallback userAgentCallback =
                    new ResourcesITextUserAgentCallback(renderer.getOutputDevice());
            userAgentCallback.setSharedContext(renderer.getSharedContext());

            renderer.getSharedContext().setUserAgentCallback(userAgentCallback);

            renderer.layout();
            renderer.createPDF(outputStream);

            FileUtils.deleteQuietly(tmpFile);
        } catch (Exception e) {
            throw wrapWithReportingException("", e);
        }
    }

    protected void loadFonts(ITextRenderer renderer) {
        Configuration configuration = AppBeans.get(Configuration.class);
        GlobalConfig config = configuration.getConfig(GlobalConfig.class);
        String fontsPath = config.getConfDir() + CUBA_FONTS_DIR;

        File fontsDir = new File(fontsPath);

        loadFontsFromDirectory(renderer, fontsDir);

        ReportingConfig serverConfig = configuration.getConfig(ReportingConfig.class);
        if (StringUtils.isNotBlank(serverConfig.getPdfFontsDirectory())) {
            File systemFontsDir = new File(serverConfig.getPdfFontsDirectory());
            loadFontsFromDirectory(renderer, systemFontsDir);
        }
    }

    protected void loadFontsFromDirectory(ITextRenderer renderer, File fontsDir) {
        if (fontsDir.exists()) {
            if (fontsDir.isDirectory()) {
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
                    } catch (IOException e) {
                        log.warn(e.getMessage());
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                }
            } else
                log.warn(format("File %s is not a directory", fontsDir.getAbsolutePath()));
        } else {
            log.debug("Fonts directory does not exist: " + fontsDir.getPath());
        }
    }

    protected class ResourcesITextUserAgentCallback extends ITextUserAgent {

        public ResourcesITextUserAgentCallback(ITextOutputDevice outputDevice) {
            super(outputDevice);
        }

        @Override
        public ImageResource getImageResource(String uri) {
            if (StringUtils.startsWith(uri, FS_PROTOCOL_PREFIX)) {
                ImageResource resource;
                resource = (ImageResource) _imageCache.get(uri);
                if (resource == null) {
                    InputStream is = resolveAndOpenStream(uri);
                    if (is != null) {
                        try {
                            Image image = Image.getInstance(IOUtils.toByteArray(is));

                            scaleToOutputResolution(image);
                            resource = new ImageResource(uri, new ITextFSImage(image));
                            //noinspection unchecked
                            _imageCache.put(uri, resource);
                        } catch (Exception e) {
                            throw wrapWithReportingException(
                                    format("Can't read image file; unexpected problem for URI '%s'", uri), e);
                        } finally {
                            IOUtils.closeQuietly(is);
                        }
                    }
                }

                if (resource != null) {
                    ITextFSImage image = (ITextFSImage) resource.getImage();

                    com.lowagie.text.Image imageObject;
                    // use reflection for access to internal image
                    try {
                        Field imagePrivateField = image.getClass().getDeclaredField("_image");
                        imagePrivateField.setAccessible(true);

                        imageObject = (Image) imagePrivateField.get(image);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new ReportFormattingException("Error while clone internal image in Itext");
                    }

                    resource = new ImageResource(uri, new ITextFSImage(imageObject));
                } else {
                    resource = new ImageResource(uri, null);
                }

                return resource;
            } else if (StringUtils.startsWith(uri, WEB_APP_PREFIX) || StringUtils.startsWith(uri, CORE_APP_PREFIX)) {
                String resolvedUri = resolveServerPrefix(uri);
                return super.getImageResource(resolvedUri);
            }

            return super.getImageResource(uri);
        }

        protected void scaleToOutputResolution(Image image) {
            float factor = getSharedContext().getDotsPerPixel();
            image.scaleAbsolute(image.getPlainWidth() * factor, image.getPlainHeight() * factor);
        }

        @Override
        protected InputStream resolveAndOpenStream(String uri) {
            if (StringUtils.startsWith(uri, FS_PROTOCOL_PREFIX)) {
                String uuidString = StringUtils.substring(uri, FS_PROTOCOL_PREFIX.length());

                DataWorker dataWorker = AppBeans.get(DataWorker.class);
                LoadContext loadContext = new LoadContext(FileDescriptor.class);
                loadContext.setView(View.LOCAL);

                UUID id = UUID.fromString(uuidString);
                loadContext.setId(id);

                FileDescriptor fd = dataWorker.load(loadContext);
                if (fd == null) {
                    throw new ReportFormattingException(
                            format("File with id [%s] has not been found in file storage", id));
                }

                FileStorageAPI storageAPI = AppBeans.get(FileStorageAPI.class);
                try {
                    return storageAPI.openStream(fd);
                } catch (FileStorageException e) {
                    throw wrapWithReportingException(
                            format("An error occurred while loading file with id [%s] from file storage", id), e);
                }
            } else if (StringUtils.startsWith(uri, WEB_APP_PREFIX) || StringUtils.startsWith(uri, CORE_APP_PREFIX)) {
                String resolvedUri = resolveServerPrefix(uri);
                return getInputStream(resolvedUri);
            } else {
                return getInputStream(uri);
            }
        }

        protected InputStream getInputStream(String uri) {
            uri = resolveURI(uri);
            InputStream inputStream = null;
            try {
                URL url = new URL(uri);
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(externalImagesTimeoutSec * 1000);
                inputStream = urlConnection.getInputStream();
            } catch (java.net.SocketTimeoutException e) {
                throw new ReportFormattingException(format("Loading resource [%s] has been stopped by timeout", uri), e);
            } catch (java.net.MalformedURLException e) {
                throw new ReportFormattingException(format("Bad URL given: [%s]", uri), e);
            } catch (FileNotFoundException e) {
                throw new ReportFormattingException(format("Resource at URL [%s] not found", uri));
            } catch (IOException e) {
                throw new ReportFormattingException(format("An IO problem occurred while loading resource [%s]", uri), e);
            }

            return inputStream;
        }
    }

    protected String resolveServerPrefix(String uri) {
        Configuration configStorage = AppBeans.get(Configuration.NAME);
        GlobalConfig globalConfig = configStorage.getConfig(GlobalConfig.class);
        String coreUrl = String.format("http://%s:%s/%s/",
                globalConfig.getWebHostName(), globalConfig.getWebPort(), globalConfig.getWebContextName());
        String webUrl = globalConfig.getWebAppUrl() + "/";
        return uri.replace(WEB_APP_PREFIX, webUrl).replace(CORE_APP_PREFIX, coreUrl);
    }

    @Override
    protected Map getBandModel(BandData band) {
        Map<String, Object> model = new HashMap<String, Object>();

        Map<String, Object> bands = new HashMap<String, Object>();
        for (String bandName : band.getChildrenBands().keySet()) {
            List<BandData> subBands = band.getChildrenBands().get(bandName);
            List<Map> bandModels = new ArrayList<Map>();
            for (BandData child : subBands)
                bandModels.add(getBandModel(child));

            bands.put(bandName, bandModels);
        }
        model.put("bands", bands);
        Map<String, Object> data = new HashMap<String, Object>();
        for (String key : band.getData().keySet()) {
            if (band.getData().get(key) instanceof Enum)
                data.put(key, defaultFormat(band.getData().get(key)));
            else if (band.getData().get(key) instanceof BaseUuidEntity) {
                data.put(key, transformEntityToMap((BaseUuidEntity) band.getData().get(key), 0));
            } else {
                data.put(key, band.getData().get(key));
            }
        }
        model.put("fields", data);

        return model;
    }

    protected Map<String, Object> transformEntityToMap(BaseUuidEntity entity, int deep) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        for (MetaProperty property : entity.getMetaClass().getProperties()) {
            Object value = null;
            try {
                if (property.getRange().isEnum())
                    value = defaultFormat(entity.getValue(property.getName()));
                else if (property.getRange().isClass() && entity.getValue(property.getName()) instanceof BaseUuidEntity)
                    if (entityMapMaxDeep < deep)
                        value = entity.getValue(property.getName());
                    else value = transformEntityToMap(entity.<BaseUuidEntity>getValue(property.getName()), deep + 1);
                else
                    value = entity.getValue(property.getName());
            } catch (RuntimeException ex) {
                log.debug(ex.getMessage(), ex);
            }
            resultMap.put(property.getName(), value);
        }
        return resultMap;
    }
}
