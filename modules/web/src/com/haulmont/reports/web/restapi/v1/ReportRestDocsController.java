/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.web.restapi.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.haulmont.cuba.core.global.Resources;
import com.haulmont.restapi.exception.RestAPIException;
import com.haulmont.restapi.swagger.SwaggerGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.io.IOException;

@RestController("report_ReportRestDocsController")
@RequestMapping("/reports/v1/docs")
public class ReportRestDocsController {

    @Inject
    protected Resources resources;

    @Inject
    protected SwaggerGenerator swaggerGenerator;

    @RequestMapping(value = "/swagger.yaml", method = RequestMethod.GET, produces = "application/yaml")
    public String getSwaggerYaml() {
        return resources.getResourceAsString("classpath:reports-rest-api-swagger.yaml");
    }

    @RequestMapping(value = "/swagger.json", method = RequestMethod.GET, produces = "application/json")
    public String getSwaggerJson() {
        String yaml = getSwaggerYaml();
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj;
        try {
            obj = yamlReader.readValue(yaml, Object.class);
            ObjectMapper jsonWriter = new ObjectMapper();
            return jsonWriter.writeValueAsString(obj);
        } catch (IOException e) {
            throw new RestAPIException("Internal server error", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}