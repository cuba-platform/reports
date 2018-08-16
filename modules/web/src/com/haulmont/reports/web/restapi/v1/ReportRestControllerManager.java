/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.web.restapi.v1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.*;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.ParameterType;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.reports.gui.ReportSecurityManager;
import com.haulmont.reports.gui.report.run.ParameterClassResolver;
import com.haulmont.restapi.exception.RestAPIException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Component("report_ReportRestControllerManager")
public class ReportRestControllerManager {
    @Inject
    protected DataManager dataManager;
    @Inject
    protected ReportService reportService;
    @Inject
    protected Metadata metadata;
    @Inject
    protected EntityStates entityStates;
    @Inject
    protected Security security;
    @Inject
    protected ReportSecurityManager reportSecurityManager;
    @Inject
    protected UserSessionSource userSessionSource;
    @Inject
    protected ParameterClassResolver parameterClassResolver;

    public String loadReportsList() {
        checkCanReadEntity(metadata.getClassNN(Report.class));

        LoadContext<Report> loadContext = new LoadContext<>(Report.class);
        loadContext.setView(
                new View(Report.class)
                        .addProperty("id")
                        .addProperty("name")
                        .addProperty("code"))
                .setQueryString("select r from report$Report r where r.restAccess = true");
        reportSecurityManager.applySecurityPolicies(loadContext, null, userSessionSource.getUserSession().getCurrentOrSubstitutedUser());
        List<Report> reports = dataManager.loadList(loadContext);

        List<ReportInfo> objects = reports.stream()
                .map(this::mapToReportInfo)
                .collect(Collectors.toList());

        return createGson().toJson(objects);
    }

    public String loadReport(String entityId) {
        Report report = loadReportInternal(entityId);
        return createGson().toJson(mapToReportInfo(report));
    }

    public ReportRestResult runReport(String entityId, String bodyJson) {
        Report report = loadReportInternal(entityId);
        final ReportRunRestBody body;
        try {
            body = createGson().fromJson(bodyJson, ReportRunRestBody.class);
        } catch (JsonSyntaxException e) {
            throw new RestAPIException("Invalid JSON body",
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    e);
        }
        if (body.template != null) {
            report.getTemplates().stream()
                    .filter(t -> Objects.equals(t.getCode(), body.template))
                    .findFirst()
                    .orElseThrow(() -> new RestAPIException("Template not found",
                            String.format("Template with code %s not found for report %s", body.template, entityId), HttpStatus.BAD_REQUEST));
        }
        Map<String, Object> preparedValues = prepareValues(report, body.parameters);
        if (body.template != null) {
            try {
                return new ReportRestResult(reportService.createReport(report, body.template, preparedValues), body.attachment);
            } catch (RemoteException e) {
                throw new RestAPIException("Run report error",
                        e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            try {
                return new ReportRestResult(reportService.createReport(report, preparedValues), body.attachment);
            } catch (RemoteException e) {
                throw new RestAPIException("Run report error",
                        e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    protected Report loadReportInternal(String entityId) {
        checkCanReadEntity(metadata.getClassNN(Report.class));

        LoadContext<Report> loadContext = new LoadContext<>(Report.class);
        loadContext.setView(ReportService.MAIN_VIEW_NAME)
                .setQueryString("select r from report$Report r where r.id = :id and r.restAccess = true")
                .setParameter("id", getReportIdFromString(entityId));
        reportSecurityManager.applySecurityPolicies(loadContext, null, userSessionSource.getUserSession().getCurrentOrSubstitutedUser());

        Report report = dataManager.load(loadContext);

        checkEntityIsNotNull(metadata.getClassNN(Report.class).getName(), entityId, report);
        return report;
    }

    protected Map<String, Object> prepareValues(Report report, List<ParameterValueInfo> paramValues) {
        Map<String, Object> preparedValues = new HashMap<>();
        if (paramValues != null) {
            for (ReportInputParameter inputParam : report.getInputParameters()) {
                paramValues.stream().filter(paramValue -> Objects.equals(paramValue.name, inputParam.getAlias()))
                        .findFirst()
                        .ifPresent(paramValue -> preparedValues.put(paramValue.name, prepareValue(inputParam, paramValue)));
            }
        }
        return preparedValues;
    }

    protected Gson createGson() {
        return new GsonBuilder().create();
    }


    protected Object prepareValue(ReportInputParameter inputParam, ParameterValueInfo paramValue) {
        ParameterType parameterType = inputParam.getType();
        if (parameterType == ParameterType.ENTITY) {
            if (paramValue.value != null) {
                MetaClass entityClass = metadata.getClassNN(inputParam.getEntityMetaClass());
                checkCanReadEntity(entityClass);
                Object entityId = getIdFromString(paramValue.value, entityClass);
                //noinspection unchecked
                Entity entity = dataManager.load(entityClass.getJavaClass())
                        .view(View.MINIMAL)
                        .id(entityId).one();
                checkEntityIsNotNull(entityClass.getName(), paramValue.value, entity);
                return entity;
            }
        } else if (parameterType == ParameterType.ENTITY_LIST) {
            if (paramValue.values != null) {
                MetaClass entityClass = metadata.getClassNN(inputParam.getEntityMetaClass());
                checkCanReadEntity(entityClass);
                List<Entity> entities = new ArrayList<>();
                for (String value : paramValue.values) {
                    Object entityId = getIdFromString(value, entityClass);
                    //noinspection unchecked
                    Entity entity = dataManager.load(entityClass.getJavaClass())
                            .view(View.MINIMAL)
                            .id(entityId).one();
                    checkEntityIsNotNull(entityClass.getName(), value, entity);
                    entities.add(entity);
                }
                return entities;
            }
        } else if (paramValue.value != null) {
            Class paramClass = parameterClassResolver.resolveClass(inputParam);
            return reportService.convertFromString(paramClass, paramValue.value);
        }
        return null;
    }

    protected ReportInfo mapToReportInfo(Report report) {
        ReportInfo reportInfo = new ReportInfo();
        reportInfo.id = report.getId().toString();
        reportInfo.code = report.getCode();
        reportInfo.name = report.getName();

        if (entityStates.isLoaded(report, "templates")) {
            if (report.getTemplates() != null) {
                reportInfo.templates = report.getTemplates().stream()
                        .map(this::mapTemplateInfo)
                        .collect(Collectors.toList());
            }
        }

        if (entityStates.isLoaded(report, "xml")) {
            if (report.getInputParameters() != null) {
                reportInfo.inputParameters = report.getInputParameters().stream()
                        .map(this::mapInputParameterInfo)
                        .collect(Collectors.toList());
            }
        }
        return reportInfo;
    }

    protected TemplateInfo mapTemplateInfo(ReportTemplate template) {
        TemplateInfo templateInfo = new TemplateInfo();
        templateInfo.code = template.getCode();
        templateInfo.outputType = template.getReportOutputType().toString();
        return templateInfo;
    }

    protected InputParameterInfo mapInputParameterInfo(ReportInputParameter parameter) {
        InputParameterInfo inputParameterInfo = new InputParameterInfo();
        inputParameterInfo.name = parameter.getName();
        inputParameterInfo.alias = parameter.getAlias();
        if (parameter.getType() != null) {
            inputParameterInfo.type = parameter.getType().toString();
        }
        inputParameterInfo.required = Boolean.TRUE.equals(parameter.getRequired());
        inputParameterInfo.hidden = Boolean.TRUE.equals(parameter.getHidden());
        if (parameter.getEntityMetaClass() != null) {
            inputParameterInfo.entityMetaClass = parameter.getEntityMetaClass();
        }
        if (parameter.getEnumerationClass() != null) {
            inputParameterInfo.enumerationClass = parameter.getEnumerationClass();
        }
        return inputParameterInfo;
    }

    protected UUID getReportIdFromString(String entityId) {
        return (UUID) getIdFromString(entityId, metadata.getClassNN(Report.class));
    }

    protected Object getIdFromString(String entityId, MetaClass metaClass) {
        try {
            if (BaseDbGeneratedIdEntity.class.isAssignableFrom(metaClass.getJavaClass())) {
                if (BaseIdentityIdEntity.class.isAssignableFrom(metaClass.getJavaClass())) {
                    return IdProxy.of(Long.valueOf(entityId));
                } else if (BaseIntIdentityIdEntity.class.isAssignableFrom(metaClass.getJavaClass())) {
                    return IdProxy.of(Integer.valueOf(entityId));
                } else {
                    Class<?> clazz = metaClass.getJavaClass();
                    while (clazz != null) {
                        Method[] methods = clazz.getDeclaredMethods();
                        for (Method method : methods) {
                            if (method.getName().equals("getDbGeneratedId")) {
                                Class<?> idClass = method.getReturnType();
                                if (Long.class.isAssignableFrom(idClass)) {
                                    return Long.valueOf(entityId);
                                } else if (Integer.class.isAssignableFrom(idClass)) {
                                    return Integer.valueOf(entityId);
                                } else if (Short.class.isAssignableFrom(idClass)) {
                                    return Long.valueOf(entityId);
                                } else if (UUID.class.isAssignableFrom(idClass)) {
                                    return UUID.fromString(entityId);
                                }
                            }
                        }
                        clazz = clazz.getSuperclass();
                    }
                }
                throw new UnsupportedOperationException("Unsupported ID type in entity " + metaClass.getName());
            } else {
                //noinspection unchecked
                Method getIdMethod = metaClass.getJavaClass().getMethod("getId");
                Class<?> idClass = getIdMethod.getReturnType();
                if (UUID.class.isAssignableFrom(idClass)) {
                    return UUID.fromString(entityId);
                } else if (Integer.class.isAssignableFrom(idClass)) {
                    return Integer.valueOf(entityId);
                } else if (Long.class.isAssignableFrom(idClass)) {
                    return Long.valueOf(entityId);
                } else {
                    return entityId;
                }
            }
        } catch (Exception e) {
            throw new RestAPIException("Invalid entity ID",
                    String.format("Cannot convert %s into valid entity ID", entityId),
                    HttpStatus.BAD_REQUEST,
                    e);
        }
    }

    protected void checkCanReadEntity(MetaClass metaClass) {
        if (!security.isEntityOpPermitted(metaClass, EntityOp.READ)) {
            throw new RestAPIException("Reading forbidden",
                    String.format("Reading of the %s is forbidden", metaClass.getName()),
                    HttpStatus.FORBIDDEN);
        }
    }

    protected void checkEntityIsNotNull(String entityName, String entityId, Entity entity) {
        if (entity == null) {
            throw new RestAPIException("Entity not found",
                    String.format("Entity %s with id %s not found", entityName, entityId),
                    HttpStatus.NOT_FOUND);
        }
    }

    protected class ReportInfo {
        protected String id;
        protected String name;
        protected String code;

        protected List<TemplateInfo> templates;
        protected List<InputParameterInfo> inputParameters;
    }

    protected static class TemplateInfo {
        protected String code;
        protected String outputType;
    }

    protected static class InputParameterInfo {
        protected String name;
        protected String alias;
        protected String type;
        protected boolean required;
        protected boolean hidden;
        protected String entityMetaClass;
        protected String enumerationClass;
    }

    protected static class ParameterValueInfo {
        protected String name;
        protected String value;
        protected List<String> values;
    }

    protected static class ReportRunRestBody {
        protected String template;
        protected boolean attachment;
        protected List<ParameterValueInfo> parameters;
    }
}
