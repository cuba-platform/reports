package com.haulmont.reports.libintegration.extraction.controller;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.BandDefinition;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.reports.fixture.yml.YmlBandUtil;
import com.haulmont.reports.util.ReportHelper;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.springframework.core.io.ResourceLoader;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

@Ignore
public abstract class AbstractControllerTest {

    @Inject
    protected ReportService reportService;

    @Inject
    protected ReportHelper reportHelper;

    @Inject
    protected Persistence persistence;

    @Inject
    protected ResourceLoader resourceLoader;

    protected ReportOutputDocument createDocument(String bandFile, String template) throws IOException, TemplateGenerationException, URISyntaxException {
        BandDefinition root = YmlBandUtil.bandFrom(
                resourceLoader
                        .getResource(bandFile).getFile());
        Map<String, Object> params = new HashMap<>();
        processEntityType(root, params);

        Report report = reportHelper.create(root, resourceLoader
                .getResource(template).getFile());

        return reportService.createReport(report, params);
    }

    protected void processEntityType(BandDefinition root, Map<String, Object> params) {
        Optional.ofNullable(root.getChildren()).orElse(Collections.emptyList()).forEach(b-> {
            if (CollectionUtils.isNotEmpty(b.getReportQueries())) {
                b.getReportQueries().stream()
                        .map(DataSet.class::cast)
                        .forEach(ds-> {
                            switch (ds.getType()) {
                                case SINGLE:
                                    params.put(
                                            ds.getEntityParamName(),
                                            persistence.callInTransaction(em->
                                                    em.createQuery(String.format("select e from %s e", ds.getScript()))
                                                            .setMaxResults(1)
                                                            .getFirstResult()
                                            ));
                                    break;
                                case MULTI:
                                    params.put(
                                            ds.getListEntitiesParamName(),
                                            persistence.callInTransaction(em->
                                                    em.createQuery(String.format("select e from %s e", ds.getScript()))
                                                            .getResultList()
                                            ));
                                    break;
                            }
                        });
            }
            processEntityType((BandDefinition)b, params);
        });
    }

    protected void assertCrossDataDocument(ReportOutputDocument document) throws IOException, InvalidFormatException {
        assertCrossDataDocument(persistence.callInTransaction(em ->
                em.createQuery("select e.login from test$User e", String.class).getResultList()), document);
    }

    protected void assertCrossDataDocument(List<String> users, ReportOutputDocument document) throws IOException, InvalidFormatException {
        Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(document.getContent()));
        Sheet sheet = workbook.getSheetAt(0);
        Set<String> months = persistence.callInTransaction(em -> new HashSet<>(
                em.createQuery("select e.name from test$Month e", String.class).getResultList()));
        Assert.assertEquals(users.size() + 1, sheet.getPhysicalNumberOfRows());
        for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
            Assert.assertTrue(users.contains(sheet.getRow(i).getCell(0).getStringCellValue()));
            for (int j = 1; j <= months.size(); j++) {
                if (CellType.NUMERIC == CellType.forInt(sheet.getRow(i).getCell(j).getCellType())) {
                    Assert.assertTrue(sheet.getRow(i).getCell(j).getNumericCellValue() >= 0);
                }
            }
        }

        for (int i = 1; i <= months.size(); i++) {
            Assert.assertTrue(months.contains(sheet.getRow(0).getCell(i).getStringCellValue()));
        }
    }
}
