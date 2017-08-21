/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui.report.run;

import com.haulmont.bali.datastruct.Pair;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.entity.KeyValueEntity;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.data.GroupDatasource;
import com.haulmont.cuba.gui.data.impl.DsContextImpl;
import com.haulmont.cuba.gui.data.impl.ValueGroupDatasourceImpl;
import com.haulmont.cuba.gui.theme.ThemeConstants;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.tables.dto.CubaTableDTO;
import com.haulmont.reports.gui.ReportGuiManager;
import com.haulmont.yarg.reporting.ReportOutputDocument;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShowReportTable extends AbstractWindow {
    public static final String REPORT_PARAMETER = "report";
    public static final String TEMPLATE_CODE_PARAMETER = "templateCode";
    public static final String PARAMS_PARAMETER = "reportParams";
    public static final String TABLE_DATA_PARAMETER = "tableData";

    @Inject
    protected GroupBoxLayout reportParamsBox;
    @Inject
    protected ReportGuiManager reportGuiManager;
    @Inject
    protected ThemeConstants themeConstants;
    @Inject
    protected ComponentsFactory componentsFactory;
    @Inject
    protected Metadata metadata;

    @Inject
    protected LookupField reportLookup;
    @Inject
    protected Button printReportBtn;
    @Inject
    protected BoxLayout parametersFrameHolder;
    @Inject
    protected HBoxLayout reportSelectorBox;
    @Inject
    protected ScrollBoxLayout tablesHolder;


    @WindowParam(name = REPORT_PARAMETER, required = true)
    protected Report report;
    @WindowParam(name = TEMPLATE_CODE_PARAMETER, required = true)
    protected String templateCode;
    @WindowParam(name = PARAMS_PARAMETER, required = true)
    protected Map<String, Object> reportParameters;

    protected InputParametersFrame inputParametersFrame;
    protected DsContextImpl dsContext;

    @Override
    public void init(final Map<String, Object> params) {
        super.init(params);
        dsContext = new DsContextImpl(getDsContext().getDataSupplier());

        getDialogOptions()
                .setWidth(themeConstants.getInt("cuba.gui.report.ShowReportTable.width"))
                .setHeight(themeConstants.getInt("cuba.gui.report.ShowReportTable.height"))
                .setResizable(true);

        if (report != null) {
            reportSelectorBox.setVisible(false);
            CubaTableDTO dto = (CubaTableDTO) SerializationSupport.deserialize((byte[]) params.get(TABLE_DATA_PARAMETER));
            drawTables(dto);
            openReportParameters(reportParameters);
        }
        reportLookup.addValueChangeListener(e -> {
            report = (Report) e.getValue();
            openReportParameters(null);
        });
    }

    private void openReportParameters(Map<String, Object> reportParameters) {
        parametersFrameHolder.removeAll();

        if (report != null) {
            Map<String, Object> params = ParamsMap.of(
                    InputParametersFrame.REPORT_PARAMETER, report,
                    InputParametersFrame.PARAMETERS_PARAMETER, reportParameters
            );

            inputParametersFrame = (InputParametersFrame) openFrame(parametersFrameHolder,
                    "report$inputParametersFrame", params);
            reportParamsBox.setVisible(true);
        } else {
            reportParamsBox.setVisible(false);
        }
    }

    public void printReport() {
        if (inputParametersFrame != null && inputParametersFrame.getReport() != null) {
            if (validateAll()) {
                Map<String, Object> parameters = inputParametersFrame.collectParameters();
                Report report = inputParametersFrame.getReport();
                ReportOutputDocument reportResult = reportGuiManager.getReportResult(report, parameters, templateCode);
                CubaTableDTO dto = (CubaTableDTO) SerializationSupport.deserialize(reportResult.getContent());
                drawTables(dto);
            }
        }
    }

    protected void drawTables(CubaTableDTO dto) {
        Map<String, List<KeyValueEntity>> data = dto.getData();
        Map<String, Set<Pair<String, Class>>> headerMap = dto.getHeaders();
        tablesHolder.removeAll();

        if (data == null || data.isEmpty())
            return;

        data.forEach((dataSetName, keyValueEntities) -> {
            if (keyValueEntities != null && !keyValueEntities.isEmpty()) {
                GroupDatasource dataSource = createDataSource(dataSetName, keyValueEntities, headerMap);
                GroupBoxLayout groupBoxLayout = createTable(dataSetName, dataSource);
                tablesHolder.add(groupBoxLayout);
            }
        });
    }

    protected GroupDatasource createDataSource(String dataSetName, List<KeyValueEntity> keyValueEntities, Map<String, Set<Pair<String, Class>>> headerMap) {
        DsBuilder dsBuilder = DsBuilder.create(getDsContext())
                .setId(dataSetName + "Ds")
                .setDataSupplier(getDsContext().getDataSupplier());
        ValueGroupDatasourceImpl ds = dsBuilder.buildValuesGroupDatasource();
        ds.setRefreshMode(CollectionDatasource.RefreshMode.NEVER);

        Set<Pair<String, Class>> headers = headerMap.get(dataSetName);
        headers.forEach(pair -> ds.addProperty(pair.getFirst(), pair.getSecond()));

        dsContext.register(ds);
        keyValueEntities.forEach(ds::includeItem);
        return ds;
    }

    protected GroupBoxLayout createTable(String dataSetName, GroupDatasource dataSource) {
        Table table = componentsFactory.createComponent(GroupTable.class);
        table.setId(dataSetName + "Table");
        table.setDatasource(dataSource);
        table.setWidth("100%");

        GroupBoxLayout groupBox = componentsFactory.createComponent(GroupBoxLayout.class);
        groupBox.setCaption(dataSetName);
        groupBox.add(table);
        return groupBox;
    }
}
