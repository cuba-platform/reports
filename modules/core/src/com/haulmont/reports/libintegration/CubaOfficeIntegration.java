/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.yarg.exception.OpenOfficeException;
import com.haulmont.yarg.formatters.impl.doc.connector.*;
import com.sun.star.comp.helper.BootstrapException;

import javax.annotation.PreDestroy;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CubaOfficeIntegration extends OfficeIntegration implements CubaOfficeIntegrationMBean {

    public CubaOfficeIntegration(String openOfficePath, Integer... ports) {
        super(openOfficePath, ports);
    }

    @Override
    public void runTaskWithTimeout(final OfficeTask officeTask, int timeoutInSeconds) throws NoFreePortsException {
        final SecurityContext securityContext = AppContext.getSecurityContext();
        final OfficeConnection connection = acquireConnection();
        Future future = null;
        try {
            Callable<Void> task = () -> {
                AppContext.withSecurityContext(securityContext, () ->{
                    connection.open();
                    officeTask.processTaskInOpenOffice(connection.getOOResourceProvider());
                });
                return null;
            };
            future = executor.submit(task);
            future.get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (ExecutionException ex) {
            connection.close();
            if (ex.getCause() instanceof BootstrapException) {
                throw new OpenOfficeException("Failed to connect to open office. Please check open office path " + openOfficePath, ex);
            }

            if (ex.getCause() instanceof OpenOfficeException) {
                throw (OpenOfficeException)ex.getCause();
            }

            throw new RuntimeException(ex.getCause());
        } catch (OpenOfficeException ex) {
            connection.close();
            throw ex;
        } catch (Throwable ex) {
            connection.close();
            if (ex.getCause() instanceof BootstrapException) {
                throw new OpenOfficeException("Failed to connect to open office. Please check open office path " + openOfficePath, ex);
            }
            throw new OpenOfficeException(ex);
        } finally {
            if (future != null) {
                future.cancel(true);
            }
            releaseConnection(connection);
        }
    }

    @PreDestroy
    protected void destroyOfficeIntegration() {
        connectionsQueue.clear();
        for (OfficeConnection connection : connections) {
            try {
                connection.close();
            } catch (Exception e) {
                //Do nothing
            }
        }
        executor.shutdown();
    }
}