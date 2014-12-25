/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.app.service;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.reports.app.ParameterPrototype;
import com.haulmont.reports.entity.DataSetType;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportInputParameter;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.yarg.reporting.ReportOutputDocument;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $id$
 */
public interface ReportService {
    String NAME = "report_ReportService";

    String MAIN_VIEW_NAME = "report.edit";

    String DEFAULT_TEMPLATE_CODE = "DEFAULT";

    ReportOutputDocument createReport(Report report,
                                      Map<String, Object> params) throws IOException;

    ReportOutputDocument createReport(Report report, String templateCode,
                                      Map<String, Object> params) throws IOException;

    ReportOutputDocument createReport(Report report, ReportTemplate template,
                                      Map<String, Object> params) throws IOException;

    FileDescriptor createAndSaveReport(Report report,
                                       Map<String, Object> params, String fileName) throws IOException;

    FileDescriptor createAndSaveReport(Report report, String templateCode,
                                       Map<String, Object> params, String fileName) throws IOException;

    FileDescriptor createAndSaveReport(Report report, ReportTemplate template,
                                       Map<String, Object> params, String fileName) throws IOException;

    /**
     * Exports all reports and their templates into one zip archive. Each report is exported into a separate zip
     * archive with 2 files (report.xml and a template file (for example MyReport.doc)).
     * For example:
     * return byte[] (bytes of zip arhive)
     * -- MegaReport.zip
     * ---- report.xml
     * ---- Mega report.xls
     * -- Other report.zip
     * ---- report.xml
     * ---- other report.odt
     *
     * @param reports Collection of Report objects to be exported.
     * @return ZIP byte array with zip archives inside.
     * @throws com.haulmont.cuba.core.global.FileStorageException
     *                             Exception in file system
     * @throws java.io.IOException Exception in I/O streams
     */
    byte[] exportReports(Collection<Report> reports) throws IOException, FileStorageException;

    /**
     * Imports reports from ZIP archive. Archive file format is described in exportReports method.
     *
     * @param zipBytes ZIP archive as a byte array.
     * @return Collection of imported reports.
     * @throws IOException          Exception in I/O streams
     * @throws FileStorageException Exception in file system
     */
    Collection<Report> importReports(byte[] zipBytes) throws IOException, FileStorageException;

    String convertToXml(Report report);

    Report convertToReport(String xml);

    Report copyReport(Report source);

    ReportOutputDocument bulkPrint(Report report, List<Map<String, Object>> paramsList);

    MetaClass findMetaClassByDataSetEntityAlias(String alias, DataSetType dataSetType, List<ReportInputParameter> reportInputParameters);

    List loadDataForParameterPrototype(ParameterPrototype prototype);
}
