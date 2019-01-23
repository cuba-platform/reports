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

import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.yarg.exception.OpenOfficeException;
import com.haulmont.yarg.exception.ReportingInterruptedException;
import com.haulmont.yarg.formatters.impl.doc.connector.NoFreePortsException;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeConnection;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeIntegration;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeTask;
import com.sun.star.comp.helper.BootstrapException;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

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
        } catch (InterruptedException ex) {
            throw new ReportingInterruptedException("LibreOffice task interrupted");
        } catch (ExecutionException ex) {
            connection.close();
            if (ex.getCause() instanceof BootstrapException) {
                throw new OpenOfficeException("Failed to connect to LibreOffice. Please check LibreOffice path " + openOfficePath, ex);
            }

            if (ex.getCause() instanceof OpenOfficeException) {
                throw (OpenOfficeException)ex.getCause();
            }

            throw new RuntimeException(ex.getCause());
        } catch (OpenOfficeException ex) {
            connection.close();
            throw ex;
        } catch (TimeoutException tex) {
            try {
                if (Thread.interrupted()) {
                    throw new ReportingInterruptedException("LibreOffice task interrupted");
                }
            } finally {
                connection.close();
            }
            if (tex.getCause() instanceof BootstrapException) {
                throw new OpenOfficeException("Failed to connect to LibreOffice. Please check LibreOffice path " + openOfficePath, tex);
            }
            throw new OpenOfficeException(tex);
        } catch (Throwable ex) {
            connection.close();
            if (ex.getCause() instanceof BootstrapException) {
                throw new OpenOfficeException("Failed to connect to LibreOffice. Please check LibreOffice path " + openOfficePath, ex);
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