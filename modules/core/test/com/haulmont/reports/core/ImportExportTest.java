/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.core;

/**
 * @author artamonov
 * @version $Id$
 */
public class ImportExportTest extends ReportsTestCase {
    public void testNothing() throws Exception {

    }
    /*
    private Report report = null;
    private ReportingApi reportingApi;
    private byte[] reportExportBytes;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        reportingApi = AppBeans.get(ReportingApi.NAME);

        View exportView = metadata.getViewRepository().getView(Report.class, "report.edit");

        Transaction tx = persistence.createTransaction();

        try {
            EntityManager em = persistence.getEntityManager();
            em.setView(exportView);

            createReport(em);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    private void createReport(EntityManager em) throws FileStorageException {
        ReportGroup defaultGroup = em.createQuery(
                "select rg from report$ReportGroup rg where rg.code = 'ReportGroup.default'",
                ReportGroup.class).getSingleResult();

        report = new Report();
        report.setName("TestReport");
        report.setCode("TEST_REPORT");
        report.setGroup(defaultGroup);
        report.setLocaleNames("en=TestReport");
        report.setReportType(ReportType.SIMPLE);

        BandDefinition rootBand = new BandDefinition();
        rootBand.setName("Root");
        rootBand.setOrientation(Orientation.HORIZONTAL);
        rootBand.setReport(report);

        BandDefinition testBand = new BandDefinition();
        testBand.setName("TestBand");
        testBand.setOrientation(Orientation.HORIZONTAL);

        DataSet testDataSet = new DataSet();
        testDataSet.setName("TestBand DS");
        testDataSet.setType(DataSetType.GROOVY);
        testDataSet.setText("retutn [[:]]");
        testDataSet.setBandDefinition(testBand);

        testBand.setDataSets(new ArrayList<>(Collections.singleton(testDataSet)));
        testBand.setParentBandDefinition(rootBand);
        testBand.setReport(report);

        rootBand.setChildrenBandDefinitions(new ArrayList<>(Collections.singleton(testBand)));

        em.persist(testDataSet);
        em.persist(testBand);
        em.persist(rootBand);

        ReportTemplate template = new ReportTemplate();
        template.setReport(report);
        template.setCode("DEFAULT");
        template.setReportOutputType(ReportOutputType.HTML);

        String templateContent = "<html></html>";

        template.setContent(templateContent.getBytes());
        template.setName("testTemplate.html");
        template.setCustomFlag(false);

        em.persist(template);

        report.setTemplates(new ArrayList<>(Collections.singleton(template)));

        ReportInputParameter parameter = new ReportInputParameter();
        parameter.setAlias("testParam");
        parameter.setName("testParam");
        parameter.setType(ParameterType.NUMERIC);
        parameter.setPosition(0);
        parameter.setLocaleNames("en=TestParam");
        parameter.setReport(report);

        em.persist(parameter);

        report.setInputParameters(new ArrayList<>(Collections.singleton(parameter)));

        ReportValueFormat format = new ReportValueFormat();
        format.setFormatString("#,##0");
        format.setValueName("testValue");
        format.setReport(report);

        em.persist(format);

        report.setValuesFormats(new ArrayList<>(Collections.singleton(format)));

        ReportScreen screen = new ReportScreen();
        screen.setReport(report);
        screen.setScreenId("report$Report.browse");

        em.persist(screen);

        report.setReportScreens(new ArrayList<>(Collections.singleton(screen)));

        em.persist(report);
    }

    public void testExportImport() throws Exception {
        exportReport();

        deleteRecord("REPORT_REPORT", report.getId());

        importReport();
    }

    private void exportReport() throws FileStorageException, IOException {
        reportExportBytes = reportingApi.exportReports(Collections.singleton(report));
        assertNotNull(reportExportBytes);
        assertTrue(reportExportBytes.length > 0);
    }

    private void importReport() throws IOException, FileStorageException {
        Collection<Report> reports = reportingApi.importReports(reportExportBytes);
        assertNotNull(reports);
        assertEquals(reports.size(), 1);

        Report report = reports.iterator().next();
        assertEquals(report.getName(), "TestReport");
        assertEquals(report.getCode(), "TEST_REPORT");
        assertEquals(report.getReportType(), ReportType.SIMPLE);
        assertNotNull(report.getGroup());

        assertNotNull(report.getTemplates());
        assertEquals(report.getTemplates().size(), 1);
        assertEquals(report.getTemplates().get(0).getCode(), "DEFAULT");

        assertNotNull(report.getBands());
        assertNotNull(report.getRootBandDefinition());
        assertEquals(report.getBands().size(), 2);

        assertNotNull(report.getInputParameters());
        assertEquals(report.getInputParameters().size(), 1);

        assertNotNull(report.getValuesFormats());
        assertEquals(report.getValuesFormats().size(), 1);

        assertNotNull(report.getReportScreens());
        assertEquals(report.getReportScreens().size(), 1);
    }

    @Override
    protected void tearDown() throws Exception {
        if (report != null)
            deleteRecord("REPORT_REPORT", report.getId());

        super.tearDown();
    }*/
}