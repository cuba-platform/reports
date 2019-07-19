/*
 * Copyright (c) 2008-2019 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.app.ServerInfoAPI;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.contracts.Id;
import com.haulmont.cuba.core.global.*;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportExecution;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component(ReportExecutionHistoryRecorder.NAME)
public class ReportExecutionHistoryRecorderBean implements ReportExecutionHistoryRecorder {
    private static Logger log = LoggerFactory.getLogger(ReportExecutionHistoryRecorderBean.class);

    @Inject
    protected Metadata metadata;
    @Inject
    protected DataManager dataManager;
    @Inject
    protected UserSessionSource userSessionSource;
    @Inject
    protected TimeSource timeSource;
    @Inject
    protected ServerInfoAPI serverInfoAPI;
    @Inject
    protected ReportingConfig reportingConfig;
    @Inject
    protected Persistence persistence;

    @Override
    public ReportExecution startExecution(Report report, Map<String, Object> params) {
        ReportExecution execution = metadata.create(ReportExecution.class);

        execution.setReport(report);
        execution.setReportName(report.getName());
        execution.setReportCode(report.getCode());
        execution.setUser(userSessionSource.getUserSession().getUser());
        execution.setStartTime(timeSource.currentTimestamp());
        execution.setServerId(serverInfoAPI.getServerId());
        setParametersString(execution, params);
        handleNewReportEntity(execution);

        execution = dataManager.commit(execution);
        return execution;
    }

    @Override
    public void markAsSuccess(ReportExecution execution) {
        execution.setSuccess(true);
        execution.setFinishTime(timeSource.currentTimestamp());
        dataManager.commit(execution);
    }

    @Override
    public void markAsCancelled(ReportExecution execution) {
        execution.setCancelled(true);
        execution.setFinishTime(timeSource.currentTimestamp());
        dataManager.commit(execution);
    }

    @Override
    public void markAsError(ReportExecution execution, Exception e) {
        execution.setSuccess(false);
        execution.setFinishTime(timeSource.currentTimestamp());
        execution.setErrorMessage(e.getMessage());

        dataManager.commit(execution);
    }

    protected void setParametersString(ReportExecution reportExecution, Map<String, Object> params) {
        if (params.size() <= 0) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = (entry.getValue() instanceof Entity)
                    ? String.format("%s-%s", metadata.getClass(entry.getValue().getClass()), ((Entity) entry.getValue()).getId())
                    : entry.getValue();
            builder.append(String.format("key: %s, value: %s", entry.getKey(), value)).append("\n");
        }
        reportExecution.setParams(builder.toString());
    }

    private void handleNewReportEntity(ReportExecution entity) {
        Report report = entity.getReport();

        // handle case when user runs report that isn't saved yet from Report Editor
        if (PersistenceHelper.isNew(report)) {
            Report reloaded = dataManager.load(Id.of(report))
                    .view(View.MINIMAL)
                    .optional()
                    .orElse(null);
            entity.setReport(reloaded);
        }
    }

    @Override
    public String cleanupHistory() {
        int deleted = 0;

        deleted += deleteHistoryByDays();
        deleted += deleteHistoryGroupedByReport();

        return deleted > 0 ? String.valueOf(deleted) : null;
    }

    private int deleteHistoryByDays() {
        int historyCleanupMaxDays = reportingConfig.getHistoryCleanupMaxDays();
        if (historyCleanupMaxDays <= 0) {
            return 0;
        }

        Date borderDate = DateUtils.addDays(timeSource.currentTimestamp(), -historyCleanupMaxDays);
        log.debug("Deleting report executions older than {}", borderDate);
        int deleted = persistence.callInTransaction(em -> {
            em.setSoftDeletion(false);
            return em.createQuery("delete from report$ReportExecution e where e.startTime < :borderDate")
                    .setParameter("borderDate", borderDate)
                    .executeUpdate();
        });
        return deleted;
    }

    private int deleteHistoryGroupedByReport() {
        int maxItemsPerReport = reportingConfig.getHistoryCleanupMaxItemsPerReport();
        if (maxItemsPerReport <= 0) {
            return 0;
        }

        List<UUID> allReportIds = dataManager.loadValues("select r.id from report$Report r")
                .properties("id")
                .list()
                .stream()
                .map(kve -> (UUID) kve.getValue("id"))
                .collect(Collectors.toList());

        log.debug("Deleting report executions for every report, older than {}th execution", maxItemsPerReport);
        int total = 0;
        for (UUID reportId : allReportIds) {
            int deleted = deleteForOneReport(reportId, maxItemsPerReport);
            total += deleted;
        }
        return total;
    }

    private int deleteForOneReport(UUID reportId, int maxItemsPerReport) {
        return persistence.callInTransaction(em -> {
            em.setSoftDeletion(false);
            int rows = 0;
            Date borderStartTime = em.createQuery(
                    "select e.startTime from report$ReportExecution e"
                            + " where e.report.id = :reportId"
                            + " order by e.startTime desc", Date.class)
                    .setParameter("reportId", reportId)
                    .setFirstResult(maxItemsPerReport)
                    .setMaxResults(1)
                    .getFirstResult();

            if (borderStartTime != null) {
                rows = em.createQuery("delete from report$ReportExecution e"
                        + " where e.report.id = :reportId and e.startTime <= :borderTime")
                        .setParameter("reportId", reportId)
                        .setParameter("borderTime", borderStartTime)
                        .executeUpdate();
            }
            return rows;
        });
    }
}
