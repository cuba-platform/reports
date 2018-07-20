/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.web.restapi.v1;

import com.haulmont.restapi.exception.ErrorInfo;
import com.haulmont.restapi.exception.RestAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice("com.haulmont.reports.web.restapi.v1")
public class ReportsRestControllerExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(com.haulmont.restapi.controllers.RestControllerExceptionHandler.class);

    @ExceptionHandler(RestAPIException.class)
    @ResponseBody
    public ResponseEntity<ErrorInfo> handleRestAPIException(RestAPIException e) {
        if (e.getCause() == null) {
            log.info("RestAPIException: {}, {}", e.getMessage(), e.getDetails());
        } else {
            log.error("RestAPIException: {}, {}", e.getMessage(), e.getDetails(), e.getCause());
        }
        ErrorInfo errorInfo = new ErrorInfo(e.getMessage(), e.getDetails());
        return new ResponseEntity<>(errorInfo, e.getHttpStatus());
    }


    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("Exception in REST controller", e);
        ErrorInfo errorInfo = new ErrorInfo("Server error", "");
        return new ResponseEntity<>(errorInfo, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}