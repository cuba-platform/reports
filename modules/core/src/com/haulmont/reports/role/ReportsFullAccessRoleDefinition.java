package com.haulmont.reports.role;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.security.app.role.AnnotatedRoleDefinition;
import com.haulmont.cuba.security.app.role.annotation.EntityAccess;
import com.haulmont.cuba.security.app.role.annotation.EntityAttributeAccess;
import com.haulmont.cuba.security.app.role.annotation.Role;
import com.haulmont.cuba.security.app.role.annotation.ScreenAccess;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.cuba.security.role.EntityAttributePermissionsContainer;
import com.haulmont.cuba.security.role.EntityPermissionsContainer;
import com.haulmont.cuba.security.role.ScreenPermissionsContainer;
import com.haulmont.reports.entity.*;
import com.haulmont.reports.entity.charts.ChartSeries;
import com.haulmont.reports.entity.charts.PieChartDescription;
import com.haulmont.reports.entity.charts.SerialChartDescription;
import com.haulmont.reports.entity.pivottable.PivotTableAggregation;
import com.haulmont.reports.entity.pivottable.PivotTableDescription;
import com.haulmont.reports.entity.pivottable.PivotTableProperty;
import com.haulmont.reports.entity.table.TemplateTableBand;
import com.haulmont.reports.entity.table.TemplateTableColumn;
import com.haulmont.reports.entity.table.TemplateTableDescription;
import com.haulmont.reports.entity.wizard.EntityTreeNode;
import com.haulmont.reports.entity.wizard.RegionProperty;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.ReportRegion;

/**
 * System role that grants full access for reports: editing, execution.
 */
@Role(name = ReportsFullAccessRoleDefinition.NAME)
public class ReportsFullAccessRoleDefinition extends AnnotatedRoleDefinition {
    public final static String NAME = "system-reports-full-access";

    @ScreenAccess(screenIds = {"report$inputParameters", "report$Report.run", "report$showReportTable",
            "report$showPivotTable", "report$showChart", "commonLookup",
            "report$ReportGroup.browse", "reports", "report$Report.browse",
            "report$Report.edit", "report$ChartEdit", "report$inputParametersFrame",
            "report$PivotTableAggregation.edit", "report$PivotTableEdit",
            "report$PivotTableProperty.edit", "report$BandDefinition.edit", "report$Report.importDialog",
            "report$Report.regionEditor", "report$Report.wizard", "report$ReportEntityTree.lookup",
            "report$ReportExecution.browse", "report$ReportExecution.dialog", "report$ReportGroup.edit",
            "report$ReportInputParameter.edit", "report$ReportTemplate.edit", "report$ReportValueFormat.edit",
            "report$TableEditFrame", "scriptEditorDialog"})
    @Override
    public ScreenPermissionsContainer screenPermissions() {
        return super.screenPermissions();
    }

    @EntityAccess(entityClass = com.haulmont.cuba.security.entity.Role.class, operations = {EntityOp.READ})
    @EntityAccess(entityClass = FileDescriptor.class, operations = {EntityOp.READ, EntityOp.CREATE, EntityOp.UPDATE, EntityOp.DELETE})
    @EntityAccess(entityClass = Report.class, operations = {EntityOp.READ, EntityOp.CREATE, EntityOp.UPDATE, EntityOp.DELETE})
    @EntityAccess(entityClass = ReportGroup.class, operations = {EntityOp.READ, EntityOp.CREATE, EntityOp.UPDATE, EntityOp.DELETE})
    @EntityAccess(entityClass = ReportTemplate.class, operations = {EntityOp.READ, EntityOp.CREATE, EntityOp.UPDATE, EntityOp.DELETE})
    @EntityAccess(entityClass = ReportExecution.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @EntityAccess(entityClass = ChartSeries.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @EntityAccess(entityClass = PieChartDescription.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @EntityAccess(entityClass = SerialChartDescription.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @EntityAccess(entityClass = PivotTableAggregation.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @EntityAccess(entityClass = PivotTableDescription.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @EntityAccess(entityClass = PivotTableProperty.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @EntityAccess(entityClass = TemplateTableBand.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @EntityAccess(entityClass = TemplateTableColumn.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @EntityAccess(entityClass = TemplateTableDescription.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @EntityAccess(entityClass = EntityTreeNode.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @EntityAccess(entityClass = RegionProperty.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @EntityAccess(entityClass = ReportData.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @EntityAccess(entityClass = ReportRegion.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @EntityAccess(entityClass = BandDefinition.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @EntityAccess(entityClass = DataSet.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @EntityAccess(entityClass = ReportInputParameter.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @EntityAccess(entityClass = ReportScreen.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @EntityAccess(entityClass = ReportValueFormat.class, operations = {EntityOp.CREATE, EntityOp.UPDATE, EntityOp.READ, EntityOp.DELETE})
    @Override
    public EntityPermissionsContainer entityPermissions() {
        return super.entityPermissions();
    }

    @EntityAttributeAccess(entityClass = com.haulmont.cuba.security.entity.Role.class, view = "*")
    @EntityAttributeAccess(entityClass = FileDescriptor.class, modify = "*")
    @EntityAttributeAccess(entityClass = Report.class, modify = "*")
    @EntityAttributeAccess(entityClass = ReportGroup.class, modify = "*")
    @EntityAttributeAccess(entityClass = ReportTemplate.class, modify = "*")
    @EntityAttributeAccess(entityClass = ReportExecution.class, modify = "*")
    @EntityAttributeAccess(entityClass = ChartSeries.class, modify = "*")
    @EntityAttributeAccess(entityClass = PieChartDescription.class, modify = "*")
    @EntityAttributeAccess(entityClass = SerialChartDescription.class, modify = "*")
    @EntityAttributeAccess(entityClass = PivotTableAggregation.class, modify = "*")
    @EntityAttributeAccess(entityClass = PivotTableDescription.class, modify = "*")
    @EntityAttributeAccess(entityClass = PivotTableProperty.class, modify = "*")
    @EntityAttributeAccess(entityClass = TemplateTableBand.class, modify = "*")
    @EntityAttributeAccess(entityClass = TemplateTableColumn.class, modify = "*")
    @EntityAttributeAccess(entityClass = TemplateTableDescription.class, modify = "*")
    @EntityAttributeAccess(entityClass = EntityTreeNode.class, modify = "*")
    @EntityAttributeAccess(entityClass = RegionProperty.class, modify = "*")
    @EntityAttributeAccess(entityClass = ReportData.class, modify = "*")
    @EntityAttributeAccess(entityClass = ReportRegion.class, modify = "*")
    @EntityAttributeAccess(entityClass = BandDefinition.class, modify = "*")
    @EntityAttributeAccess(entityClass = DataSet.class, modify = "*")
    @EntityAttributeAccess(entityClass = ReportInputParameter.class, modify = "*")
    @EntityAttributeAccess(entityClass = ReportScreen.class, modify = "*")
    @EntityAttributeAccess(entityClass = ReportValueFormat.class, modify = "*")
    @Override
    public EntityAttributePermissionsContainer entityAttributePermissions() {
        return super.entityAttributePermissions();
    }

    @Override
    public String getLocName() {
        return "Reports Full Access";
    }
}
