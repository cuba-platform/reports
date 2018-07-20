/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.web.restapi.v1;

import com.haulmont.cuba.core.global.FileTypesHelper;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.reports.gui.ReportPrintHelper;
import com.haulmont.restapi.exception.RestAPIException;
import com.haulmont.yarg.structure.ReportOutputType;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController("report_ReportRestV1Controller")
@RequestMapping("/reports/v1")
public class ReportRestV1Controller {

    private static final Logger log = LoggerFactory.getLogger(ReportRestV1Controller.class);

    @Inject
    protected ReportRestV1ControllerManager controllerManager;

    @GetMapping(value = "/report", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String loadReportsList() {
        return controllerManager.loadReportsList();
    }

    @GetMapping(value = "/report/{entityId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String loadReport(@PathVariable String entityId) {
        return controllerManager.loadReport(entityId);
    }

    @PostMapping(value = "/run/{entityId}")
    public void runReport(@PathVariable String entityId,
                          @RequestBody String body, HttpServletResponse response) {

        ReportRestResult result = controllerManager.runReport(entityId, body);

        try {
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Content-Type", getContentType(result.getReportOutputType()));
            response.setHeader("Content-Disposition", (BooleanUtils.isTrue(result.attachment) ? "attachment" : "inline")
                    + "; filename=\"" + result.getDocumentName() + "\"");

            ServletOutputStream os = response.getOutputStream();
            IOUtils.copy(new ByteArrayInputStream(result.getContent()), os);
            os.flush();
        } catch (IOException e) {
            log.error("Error on downloading the report {}", entityId, e);
            throw new RestAPIException("Error on downloading the report", "", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    protected String getContentType(ReportOutputType outputType) {
        ExportFormat exportFormat = ReportPrintHelper.getExportFormat(outputType);
        return exportFormat == null ? FileTypesHelper.DEFAULT_MIME_TYPE : exportFormat.getContentType();
    }
}
