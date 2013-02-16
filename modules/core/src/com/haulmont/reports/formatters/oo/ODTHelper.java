/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.formatters.oo;

import com.haulmont.cuba.core.app.FileStorageAPI;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.reports.ReportingConfig;
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XOutputStream;
import com.sun.star.lang.XComponent;
import com.sun.star.util.XCloseable;
import org.apache.commons.io.IOUtils;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author fontanenko
 * @version $Id$
 */
public final class ODTHelper {
    public static XInputStream getXInputStream(FileDescriptor fileDescriptor) {
        FileStorageAPI storageAPI = AppBeans.get(FileStorageAPI.NAME);
        try {
            byte[] bytes = IOUtils.toByteArray(storageAPI.openStream(fileDescriptor));
            return new OOInputStream(bytes);
        } catch (FileStorageException | java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static XComponent loadXComponent(XComponentLoader xComponentLoader, XInputStream inputStream) throws com.sun.star.lang.IllegalArgumentException, IOException {
        PropertyValue[] props = new PropertyValue[2];
        props[0] = new PropertyValue();
        props[1] = new PropertyValue();
        props[0].Name = "InputStream";
        props[0].Value = inputStream;
        props[1].Name = "Hidden";
        props[1].Value = true;
        return xComponentLoader.loadComponentFromURL("private:stream", "_blank", 0, props);
    }

    public static void closeXComponent(XComponent xComponent) {
        XCloseable xCloseable = ODTUnoConverter.asXCloseable(xComponent);
        try {
            xCloseable.close(false);
        } catch (com.sun.star.util.CloseVetoException e) {
            xComponent.dispose();
        }
    }

    public static void saveXComponent(XComponent xComponent, XOutputStream xOutputStream, String filterName) throws IOException {
        PropertyValue[] props = new PropertyValue[2];
        props[0] = new PropertyValue();
        props[1] = new PropertyValue();
        props[0].Name = "OutputStream";
        props[0].Value = xOutputStream;
        props[1].Name = "FilterName";
        props[1].Value = filterName;
        XStorable xStorable = ODTUnoConverter.asXStorable(xComponent);
        xStorable.storeToURL("private:stream", props);
    }

    /**
     * Utility method. Converts path to url
     */
    public static String pathToUrl(String sURL) throws java.io.IOException {
        java.io.File sourceFile = new java.io.File(sURL);
        StringBuilder sTmp = new StringBuilder("file:///");
        sTmp.append(sourceFile.getCanonicalPath().replace('\\', '/'));
        return sTmp.toString();
    }

    public static void copy(XDispatchHelper xDispatchHelper, XDispatchProvider xDispatchProvider) {
        xDispatchHelper.executeDispatch(xDispatchProvider, ".uno:Copy", "", 0, new PropertyValue[]{new PropertyValue()});
    }

    public static void paste(XDispatchHelper xDispatchHelper, XDispatchProvider xDispatchProvider) {
        xDispatchHelper.executeDispatch(xDispatchProvider, ".uno:Paste", "", 0, new PropertyValue[]{new PropertyValue()});
    }

    public static void runWithTimeoutAndCloseConnection(OOOConnection connection, Runnable runnable) {
        try {
            OOOConnectorAPI connectorAPI = AppBeans.get(OOOConnectorAPI.NAME);
            Future future = connectorAPI.getExecutor().submit(runnable);
            ReportingConfig reportingConfig = AppBeans.get(Configuration.class).getConfig(ReportingConfig.class);
            future.get(reportingConfig.getDocFormatterTimeout(), TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                //close silently
            }
        }
    }
}
