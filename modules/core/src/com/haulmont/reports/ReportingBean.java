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

package com.haulmont.reports;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.PersistenceSecurity;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.app.FileStorageAPI;
import com.haulmont.cuba.core.app.dynamicattributes.DynamicAttributesManagerAPI;
import com.haulmont.cuba.core.app.execution.Executions;
import com.haulmont.cuba.core.app.execution.ResourceCanceledException;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.cuba.security.entity.PermissionType;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.converter.GsonConverter;
import com.haulmont.reports.converter.XStreamConverter;
import com.haulmont.reports.entity.*;
import com.haulmont.reports.exception.*;
import com.haulmont.reports.libintegration.CustomFormatter;
import com.haulmont.yarg.formatters.impl.doc.connector.NoFreePortsException;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.reporting.ReportOutputDocumentImpl;
import com.haulmont.yarg.reporting.ReportingAPI;
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.ReportOutputType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.CRC32;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component(ReportingApi.NAME)
public class ReportingBean implements ReportingApi {
    public static final String REPORT_EDIT_VIEW_NAME = "report.edit";
    protected static final int MAX_REPORT_NAME_LENGTH = 255;
    protected static final String IDX_SEPARATOR = ",";

    @Inject
    protected Persistence persistence;
    @Inject
    protected Metadata metadata;
    @Inject
    protected FileStorageAPI fileStorageAPI;
    @Inject
    protected TimeSource timeSource;
    @Inject
    protected ReportingAPI reportingApi;
    @Inject
    protected ReportImportExportAPI reportImportExport;
    @Inject
    protected ReportExecutionHistoryRecorder executionHistoryRecorder;
    @Inject
    protected UuidSource uuidSource;
    @Inject
    protected UserSessionSource userSessionSource;
    @Inject
    protected GlobalConfig globalConfig;
    @Inject
    protected ReportingConfig reportingConfig;
    @Inject
    protected DataManager dataManager;
    @Inject
    protected DynamicAttributesManagerAPI dynamicAttributesManagerAPI;
    @Inject
    protected ViewRepository viewRepository;
    @Inject
    protected Executions executions;
    @Inject
    protected PersistenceSecurity security;
    @Inject
    protected EntityStates entityStates;

    protected PrototypesLoader prototypesLoader = new PrototypesLoader();

    protected GsonConverter gsonConverter = new GsonConverter();
    protected XStreamConverter xStreamConverter = new XStreamConverter();

    //todo eude try to simplify report save logic
    @Override
    public Report storeReportEntity(Report report) {
        Report savedReport = null;
        checkPermission(report);
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            ReportTemplate defaultTemplate = report.getDefaultTemplate();
            List<ReportTemplate> loadedTemplates = report.getTemplates();
            List<ReportTemplate> savedTemplates = new ArrayList<>();

            report.setDefaultTemplate(null);
            report.setTemplates(null);

            ReportGroup reportGroup = report.getGroup();
            if (reportGroup != null) {
                ReportGroup existingGroup = em.createQuery(
                        "select g from report$ReportGroup g where g.title = :title", ReportGroup.class)
                        .setParameter("title", reportGroup.getTitle())
                        .setViewName(View.LOCAL)
                        .getFirstResult();
                if (existingGroup == null) {
                    em.setSoftDeletion(false);
                    existingGroup = em.find(ReportGroup.class, reportGroup.getId(), View.LOCAL);
                    em.setSoftDeletion(true);
                }
                if (existingGroup != null) {
                    if (!entityStates.isDeleted(existingGroup)) {
                        report.setGroup(existingGroup);
                    }
                    else {
                        reportGroup = dataManager.create(ReportGroup.class);
                        UUID newId = reportGroup.getId();
                        reportGroup = metadata.getTools().copy(existingGroup);
                        reportGroup.setVersion(0);
                        reportGroup.setDeleteTs(null);
                        reportGroup.setDeletedBy(null);
                        reportGroup.setId(newId);
                        report.setGroup(reportGroup);
                    }
                } else {
                    em.persist(reportGroup);
                }
            }

            em.setSoftDeletion(false);
            Report existingReport;
            List<ReportTemplate> existingTemplates = null;
            try {
                existingReport = em.find(Report.class, report.getId(), "report.withTemplates");
                if (existingReport != null) {
                    storeIndexFields(report);
                    report.setVersion(existingReport.getVersion());
                    report = em.merge(report);
                    if (existingReport.getTemplates() != null) {
                        existingTemplates = existingReport.getTemplates();
                    }
                    if (existingReport.getDeleteTs() != null) {
                        existingReport.setDeleteTs(null);
                        existingReport.setDeletedBy(null);
                    }
                    report.setDefaultTemplate(null);
                    report.setTemplates(null);
                } else {
                    storeIndexFields(report);
                    report.setVersion(0);
                    report = em.merge(report);
                }

                dynamicAttributesManagerAPI.storeDynamicAttributes(report);

                if (loadedTemplates != null) {
                    if (existingTemplates != null) {
                        for (ReportTemplate template : existingTemplates) {
                            if (!loadedTemplates.contains(template)) {
                                em.remove(template);
                            }
                        }
                    }

                    for (ReportTemplate loadedTemplate : loadedTemplates) {
                        ReportTemplate existingTemplate = em.find(ReportTemplate.class, loadedTemplate.getId());
                        if (existingTemplate != null) {
                            loadedTemplate.setVersion(existingTemplate.getVersion());
                            if (PersistenceHelper.isNew(loadedTemplate)) {
                                PersistenceHelper.makeDetached(loadedTemplate);
                            }
                        } else {
                            loadedTemplate.setVersion(0);
                        }

                        loadedTemplate.setReport(report);
                        savedTemplates.add(em.merge(loadedTemplate));
                    }
                }
            } finally {
                em.setSoftDeletion(true);
            }
            em.flush();

            for (ReportTemplate savedTemplate : savedTemplates) {
                if (savedTemplate.equals(defaultTemplate)) {
                    defaultTemplate = savedTemplate;
                    break;
                }
            }
            report.setDefaultTemplate(defaultTemplate);
            report.setTemplates(savedTemplates);
            savedReport = report;

            tx.commit();
        } finally {
            tx.end();
        }

        View reportEditView = viewRepository.findView(savedReport.getMetaClass(), "report.edit");
        return dataManager.reload(savedReport, reportEditView, savedReport.getMetaClass(), true);
    }

    @Override
    public ReportOutputDocument createReport(Report report, Map<String, Object> params) {
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
        ReportTemplate reportTemplate = getDefaultTemplate(report);
        return createReportDocument(new ReportRunParams().setReport(report).setReportTemplate(reportTemplate).setParams(params));
    }

    @Override
    public ReportOutputDocument createReport(Report report, Map<String, Object> params, com.haulmont.reports.entity.ReportOutputType outputType) {
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
        ReportTemplate template = getDefaultTemplate(report);
        return createReportDocument(new ReportRunParams().setReport(report).setReportTemplate(template).setOutputType(outputType).setParams(params));
    }

    @Override
    public ReportOutputDocument createReport(Report report, String templateCode, Map<String, Object> params) {
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
        ReportTemplate template = report.getTemplateByCode(templateCode);
        return createReportDocument(new ReportRunParams().setReport(report).setReportTemplate(template).setParams(params));
    }

    @Override
    public ReportOutputDocument createReport(Report report, String templateCode, Map<String, Object> params, com.haulmont.reports.entity.ReportOutputType outputType) {
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
        ReportTemplate template = report.getTemplateByCode(templateCode);
        return createReportDocument(new ReportRunParams().setReport(report).setReportTemplate(template).setOutputType(outputType).setParams(params));
    }

    @Override
    public ReportOutputDocument createReport(ReportRunParams reportRunParams) {
        Report report = reportRunParams.getReport();
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
        reportRunParams.setReport(report);
        return createReportDocument(reportRunParams);
    }

    @Override
    public ReportOutputDocument bulkPrint(Report report, List<Map<String, Object>> paramsList) {
        return bulkPrint(report, null, null, paramsList);
    }

    @Override
    public ReportOutputDocument bulkPrint(Report report, String templateCode, com.haulmont.reports.entity.ReportOutputType outputType, List<Map<String, Object>> paramsList) {
        try {
            report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream);
            zipOutputStream.setMethod(ZipArchiveOutputStream.STORED);
            zipOutputStream.setEncoding(ReportImportExport.ENCODING);

            ReportTemplate reportTemplate = getDefaultTemplate(report);
            ReportTemplate template = report.getTemplateByCode(templateCode);
            reportTemplate = (template != null) ? template : reportTemplate;

            Map<String, Integer> alreadyUsedNames = new HashMap<>();

            for (Map<String, Object> params : paramsList) {
                ReportOutputDocument reportDocument =
                        createReportDocument(new ReportRunParams().setReport(report).setReportTemplate(reportTemplate).setOutputType(outputType).setParams(params));

                String documentName = reportDocument.getDocumentName();
                if (alreadyUsedNames.containsKey(documentName)) {
                    int newCount = alreadyUsedNames.get(documentName) + 1;
                    alreadyUsedNames.put(documentName, newCount);
                    documentName = StringUtils.substringBeforeLast(documentName, ".")
                            + newCount
                            + "."
                            + StringUtils.substringAfterLast(documentName, ".");
                    alreadyUsedNames.put(documentName, 1);
                } else {
                    alreadyUsedNames.put(documentName, 1);
                }

                ArchiveEntry singleReportEntry = newStoredEntry(documentName, reportDocument.getContent());
                zipOutputStream.putArchiveEntry(singleReportEntry);
                zipOutputStream.write(reportDocument.getContent());
            }

            zipOutputStream.closeArchiveEntry();
            zipOutputStream.close();

            //noinspection UnnecessaryLocalVariable
            ReportOutputDocument reportOutputDocument =
                    new ReportOutputDocumentImpl(report, byteArrayOutputStream.toByteArray(), "Reports.zip", ReportOutputType.custom);
            return reportOutputDocument;
        } catch (IOException e) {
            throw new ReportingException("An error occurred while zipping report contents", e);
        }
    }

    protected ArchiveEntry newStoredEntry(String name, byte[] data) {
        ZipArchiveEntry zipEntry = new ZipArchiveEntry(name);
        zipEntry.setSize(data.length);
        zipEntry.setCompressedSize(zipEntry.getSize());
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        zipEntry.setCrc(crc32.getValue());
        return zipEntry;
    }

    @Override
    public ReportOutputDocument createReport(Report report, ReportTemplate template, Map<String, Object> params) {
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
        return createReportDocument(new ReportRunParams().setReport(report).setReportTemplate(template).setParams(params));
    }

    protected void checkPermission(Report report) {
        if (entityStates.isNew(report)) {
            if (!security.isEntityOpPermitted(metadata.getClassNN(Report.class), EntityOp.CREATE))
                throw new AccessDeniedException(PermissionType.ENTITY_OP, EntityOp.CREATE, metadata.getClassNN(Report.class).getName());
        } else {
            if (!security.isEntityOpPermitted(metadata.getClassNN(Report.class), EntityOp.UPDATE))
                throw new AccessDeniedException(PermissionType.ENTITY_OP, EntityOp.UPDATE, metadata.getClassNN(Report.class).getName());
        }
    }

    protected ReportOutputDocument createReportDocument(ReportRunParams reportRunParams) {
        if (!reportingConfig.isHistoryRecordingEnabled()) {
            return createReportDocumentInternal(reportRunParams);
        }

        ReportExecution reportExecution =
                executionHistoryRecorder.startExecution(reportRunParams.getReport(), reportRunParams.getParams());
        try {
            ReportOutputDocument document = createReportDocumentInternal(reportRunParams);
            executionHistoryRecorder.markAsSuccess(reportExecution, document);
            return document;
        } catch (ReportCanceledException e) {
            executionHistoryRecorder.markAsCancelled(reportExecution);
            throw e;
        } catch (Exception e) {
            executionHistoryRecorder.markAsError(reportExecution, e);
            throw e;
        }
    }

    protected ReportOutputDocument createReportDocumentInternal(ReportRunParams reportRunParams) {
        Report report = reportRunParams.getReport();
        ReportTemplate template = reportRunParams.getReportTemplate();
        com.haulmont.reports.entity.ReportOutputType outputType = reportRunParams.getOutputType();
        Map<String, Object> params = reportRunParams.getParams();
        String outputNamePattern = reportRunParams.getOutputNamePattern();

        StopWatch stopWatch = null;
        MDC.put("user", userSessionSource.getUserSession().getUser().getLogin());
        MDC.put("webContextName", globalConfig.getWebContextName());
        executions.startExecution(report.getId().toString(), "Reporting");
        try {
            stopWatch = new Slf4JStopWatch("Reporting#" + report.getName());
            List<String> prototypes = new LinkedList<>();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (param.getValue() instanceof ParameterPrototype)
                    prototypes.add(param.getKey());
            }
            Map<String, Object> resultParams = new HashMap<>(params);

            for (String paramName : prototypes) {
                ParameterPrototype prototype = (ParameterPrototype) params.get(paramName);
                List data = loadDataForParameterPrototype(prototype);
                resultParams.put(paramName, data);
            }

            if (template.isCustom()) {
                CustomFormatter customFormatter = new CustomFormatter(report, template);
                template.setCustomReport(customFormatter);
            }

            ReportOutputType resultOutputType = (outputType != null) ? outputType.getOutputType() : template.getOutputType();

            return reportingApi.runReport(new RunParams(report).template(template).params(resultParams).output(resultOutputType).outputNamePattern(outputNamePattern));
        } catch (NoFreePortsException nfe) {
            throw new NoOpenOfficeFreePortsException(nfe.getMessage());
        } catch (com.haulmont.yarg.exception.OpenOfficeException ooe) {
            throw new FailedToConnectToOpenOfficeException(ooe.getMessage());
        } catch (com.haulmont.yarg.exception.UnsupportedFormatException fe) {
            throw new UnsupportedFormatException(fe.getMessage());
        } catch (com.haulmont.yarg.exception.ValidationException ve) {
            throw new ValidationException(ve.getMessage());
        } catch (com.haulmont.yarg.exception.ReportingInterruptedException ie) {
            throw new ReportCanceledException(String.format("Report is canceled. %s", ie.getMessage()));
        } catch (com.haulmont.yarg.exception.ReportingException re) {
            Throwable rootCause = ExceptionUtils.getRootCause(re);
            if (rootCause instanceof ResourceCanceledException) {
                throw new ReportCanceledException(String.format("Report is canceled. %s", rootCause.getMessage()));
            }
            //noinspection unchecked
            List<Throwable> list = ExceptionUtils.getThrowableList(re);
            StringBuilder sb = new StringBuilder();
            for (Iterator<Throwable> it = list.iterator(); it.hasNext(); ) {
                //noinspection ThrowableResultOfMethodCallIgnored
                sb.append(it.next().getMessage());
                if (it.hasNext())
                    sb.append("\n");
            }

            throw new ReportingException(sb.toString());
        } finally {
            executions.endExecution();
            MDC.remove("user");
            MDC.remove("webContextName");
            if (stopWatch != null) {
                stopWatch.stop();
            }
        }
    }

    @Override
    public List loadDataForParameterPrototype(ParameterPrototype prototype) {
        return prototypesLoader.loadData(prototype);
    }

    @Override
    public Report copyReport(Report source) {
        source = reloadEntity(source, REPORT_EDIT_VIEW_NAME);
        Report copiedReport = metadata.getTools().deepCopy(source);
        copiedReport.setId(uuidSource.createUuid());
        copiedReport.setName(generateReportName(source.getName()));
        copiedReport.setCode(null);
        for (ReportTemplate copiedTemplate : copiedReport.getTemplates()) {
            copiedTemplate.setId(uuidSource.createUuid());
        }

        storeReportEntity(copiedReport);
        return copiedReport;
    }

    protected String generateReportName(String sourceName, int iteration) {
        if (iteration == 1) {
            iteration++; //like in win 7: duplicate of file 'a.txt' is 'a (2).txt', NOT 'a (1).txt'
        }
        String reportName = StringUtils.stripEnd(sourceName, null);
        if (iteration > 0) {
            String newReportName = String.format("%s (%s)", reportName, iteration);
            if (newReportName.length() > MAX_REPORT_NAME_LENGTH) {

                String abbreviatedReportName = StringUtils.abbreviate(reportName, MAX_REPORT_NAME_LENGTH -
                        String.valueOf(iteration).length() - 3);// 3 cause it us " ()".length

                reportName = String.format("%s (%s)", abbreviatedReportName, iteration);
            } else {
                reportName = newReportName;
            }

        }

        Transaction tx = persistence.createTransaction();
        try {

            Long countOfReportsWithSameName = (Long) persistence.getEntityManager()
                    .createQuery("select count(r) from report$Report r where r.name = :name")
                    .setParameter("name", reportName)
                    .getSingleResult();
            tx.commit();
            if (countOfReportsWithSameName > 0) {
                return generateReportName(sourceName, ++iteration);
            }
        } finally {
            tx.end();
        }
        return reportName;
    }

    @Override
    public byte[] exportReports(Collection<Report> reports) {
        return reportImportExport.exportReports(reports);
    }

    @Override
    public FileDescriptor createAndSaveReport(Report report, Map<String, Object> params, String fileName) {
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
        ReportTemplate template = getDefaultTemplate(report);
        return createAndSaveReport(report, template, params, fileName);
    }

    @Override
    public FileDescriptor createAndSaveReport(Report report, String templateCode,
                                              Map<String, Object> params, String fileName) {
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
        ReportTemplate template = report.getTemplateByCode(templateCode);
        return createAndSaveReport(report, template, params, fileName);
    }

    @Override
    public FileDescriptor createAndSaveReport(Report report, ReportTemplate template,
                                              Map<String, Object> params, String fileName) {
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
        ReportRunParams reportRunParams = new ReportRunParams()
                .setReport(report)
                .setReportTemplate(template)
                .setParams(params)
                .setOutputNamePattern(fileName);
        return createAndSaveReportDocument(reportRunParams);
    }

    @Override
    public FileDescriptor createAndSaveReport(ReportRunParams reportRunParams) {
        Report report = reportRunParams.getReport();
        report = reloadEntity(report, REPORT_EDIT_VIEW_NAME);
        reportRunParams.setReport(report);
        return createAndSaveReportDocument(reportRunParams);
    }

    protected FileDescriptor createAndSaveReportDocument(ReportRunParams reportRunParams) {
        ReportOutputDocument reportOutputDocument = createReportDocument(reportRunParams);
        byte[] reportData = reportOutputDocument.getContent();
        String documentName = reportOutputDocument.getDocumentName();
        String ext = reportRunParams.getReportTemplate().getReportOutputType().toString().toLowerCase();

        return saveReport(reportData, documentName, ext);
    }

    protected FileDescriptor saveReport(byte[] reportData, String fileName, String ext) {
        FileDescriptor file = metadata.create(FileDescriptor.class);
        file.setCreateDate(timeSource.currentTimestamp());
        file.setName(getFileDescriptorName(fileName, ext));
        file.setExtension(ext);
        file.setSize((long) reportData.length);

        try {
            fileStorageAPI.saveFile(file, reportData);
        } catch (FileStorageException e) {
            throw new ReportingException("An error occurred while saving the report to the file storage", e);
        }

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            em.persist(file);
            tx.commit();
        } finally {
            tx.end();
        }
        return file;
    }

    @Override
    public Collection<Report> importReports(byte[] zipBytes) {
        return reportImportExport.importReports(zipBytes);
    }

    @Override
    public Collection<Report> importReports(byte[] zipBytes, EnumSet<ReportImportOption> importOptions) {
        return reportImportExport.importReports(zipBytes, importOptions);
    }

    @Override
    public ReportImportResult importReportsWithResult(byte[] zipBytes, EnumSet<ReportImportOption> importOptions) {
        return reportImportExport.importReportsWithResult(zipBytes, importOptions);
    }

    @Override
    public String convertToString(Report report) {
        return gsonConverter.convertToString(report);
    }

    @Override
    public Report convertToReport(String serializedReport) {
        if (!serializedReport.startsWith("<")) {//for old xml reports
            return gsonConverter.convertToReport(serializedReport);
        } else {
            return xStreamConverter.convertToReport(serializedReport);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> T reloadEntity(T entity, View view) {
        if (entity instanceof Report && ((Report) entity).getIsTmp()) {
            return entity;
        }
        return dataManager.reload(entity, view);
    }

    @Override
    public MetaClass findMetaClassByDataSetEntityAlias(final String alias, final DataSetType dataSetType, final List<ReportInputParameter> reportInputParameters) {
        if (reportInputParameters.isEmpty() || StringUtils.isBlank(alias)) {
            return null;
        }

        String realAlias;
        boolean isCollectionAlias;

        if (DataSetType.MULTI == dataSetType) {

            realAlias = StringUtils.substringBefore(alias, "#");
        } else {
            realAlias = alias;
        }
        isCollectionAlias = !alias.equals(realAlias);

        class ReportInputParameterAliasFilterPredicate implements Predicate {
            final DataSetType dataSetType;
            final String realAlias;
            final boolean isCollectionAlias;

            ReportInputParameterAliasFilterPredicate(DataSetType dataSetType, String realAlias, boolean isCollectionAlias) {
                this.dataSetType = dataSetType;
                this.realAlias = realAlias;
                this.isCollectionAlias = isCollectionAlias;
            }

            @Override
            public boolean evaluate(Object object) {
                ReportInputParameter filterCandidateParameter = null;
                if (object instanceof ReportInputParameter) {
                    filterCandidateParameter = (ReportInputParameter) object;
                }

                if (realAlias.equals(filterCandidateParameter.getAlias())) {
                    if (DataSetType.MULTI == dataSetType) {
                        //find param that is matched for a MULTI dataset
                        if (isCollectionAlias) {
                            if (ParameterType.ENTITY == filterCandidateParameter.getType()) {
                                return true;
                            }
                        } else {
                            if (ParameterType.ENTITY_LIST == filterCandidateParameter.getType()) {
                                return true;
                            }
                        }
                    } else if (DataSetType.SINGLE == dataSetType) {
                        //find param that is matched for a SINGLE dataset
                        if (ParameterType.ENTITY == filterCandidateParameter.getType()) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }
        Predicate predicate = new ReportInputParameterAliasFilterPredicate(dataSetType, realAlias, isCollectionAlias);

        List<ReportInputParameter> filteredParams = new ArrayList<>(reportInputParameters);
        CollectionUtils.filter(filteredParams, predicate);
        if (filteredParams.size() == 1) {
            return metadata.getClass(filteredParams.get(0).getEntityMetaClass());
        } else {
            return null;
        }
    }

    @Override
    public String generateReportName(String sourceName) {
        return generateReportName(sourceName, 0);
    }

    @Override
    public void cancelReportExecution(UUID userSessionId, UUID reportId) {
        executions.cancelExecution(userSessionId, "Reporting", reportId.toString());
    }

    @Override
    public Date currentDateOrTime(ParameterType parameterType) {
        Date now = timeSource.currentTimestamp();
        switch (parameterType) {
            case TIME:
                now = truncateToTime(now);
                break;
            case DATETIME:
                break;
            case DATE:
                now = truncateToDay(now);
                break;
            default:
                throw new ReportingException("Not Date/Time related parameter types are not supported.");
        }
        return now;
    }

    @SuppressWarnings("unchecked")
    protected <T extends Entity> T reloadEntity(T entity, String viewName) {
        if (entity instanceof Report && ((Report) entity).getIsTmp()) {
            return entity;
        }

        return dataManager.reload(entity, viewName);
    }

    protected ReportTemplate getDefaultTemplate(Report report) {
        ReportTemplate defaultTemplate = report.getDefaultTemplate();
        if (defaultTemplate == null)
            throw new ReportingException(String.format("No default template specified for report [%s]", report.getName()));
        return defaultTemplate;
    }

    protected void storeIndexFields(Report report) {
        if (PersistenceHelper.isLoaded(report, "xml")) {
            StringBuilder entityTypes = new StringBuilder(IDX_SEPARATOR);
            if (report.getInputParameters() != null) {
                for (ReportInputParameter parameter : report.getInputParameters()) {
                    if (isNotBlank(parameter.getEntityMetaClass())) {
                        entityTypes.append(parameter.getEntityMetaClass())
                                .append(IDX_SEPARATOR);
                    }
                }
            }
            report.setInputEntityTypesIdx(entityTypes.length() > 1 ? entityTypes.toString() : null);

            StringBuilder screens = new StringBuilder(IDX_SEPARATOR);
            if (report.getReportScreens() != null) {
                for (ReportScreen reportScreen : report.getReportScreens()) {
                    screens.append(reportScreen.getScreenId())
                            .append(IDX_SEPARATOR);
                }
            }
            report.setScreensIdx(screens.length() > 1 ? screens.toString() : null);

            StringBuilder roles = new StringBuilder(IDX_SEPARATOR);
            if (report.getRoles() != null) {
                for (Role role : report.getRoles()) {
                    if (role.isPredefined()) {
                        roles.append(role.getName())
                                .append(IDX_SEPARATOR);
                    } else {
                        roles.append(role.getId().toString())
                                .append(IDX_SEPARATOR);
                    }
                }
            }
            report.setRolesIdx(roles.length() > 1 ? roles.toString() : null);
        }
    }

    protected Date truncateToDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    protected Date truncateToTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.YEAR, 1970);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    protected String getFileDescriptorName(String fileName, String ext) {
        if (fileName != null && ext != null
                && !fileName.toLowerCase(Locale.ROOT).endsWith("." + ext.toLowerCase(Locale.ROOT))) {
            return fileName + "." + ext;
        }
        return fileName;
    }
}