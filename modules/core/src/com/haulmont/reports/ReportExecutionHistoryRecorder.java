/*
 * Copyright (c) 2008-2019 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports;

import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportExecution;
import com.haulmont.yarg.reporting.ReportOutputDocument;

import java.util.Map;

public interface ReportExecutionHistoryRecorder {
    String NAME = "reporting_ExecutionHistoryRecorder";

    ReportExecution startExecution(Report report, Map<String, Object> params);

    void markAsSuccess(ReportExecution execution, ReportOutputDocument document);

    void markAsError(ReportExecution execution, Exception e);

    void markAsCancelled(ReportExecution execution);

    /**
     * Should be invoked as scheduled task.
     */
    String cleanupHistory();
}
