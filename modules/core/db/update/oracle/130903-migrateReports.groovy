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

import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.Transaction
import com.haulmont.cuba.core.app.FileStorageAPI
import com.haulmont.cuba.core.entity.FileDescriptor
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.FileStorageException
import com.haulmont.reports.app.service.ReportService
import com.haulmont.reports.entity.BandDefinition
import com.haulmont.reports.entity.Report
import com.haulmont.reports.entity.ReportTemplate
import org.apache.commons.lang3.StringUtils
import org.mybatis.spring.SqlSessionFactoryBean
import org.mybatis.spring.SqlSessionTemplate
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource

import javax.sql.DataSource

String mapping = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
        "<!DOCTYPE mapper\n" +
        "        PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"\n" +
        "        \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
        "<mapper namespace=\"com.haulmont.report\">\n" +
        "\n" +
        "    <select id=\"selectReport\" resultMap=\"reportMap\">\n" +
        "        select\n" +
        "        r.id as report_id,\n" +
        "        r.name as report_name,\n" +
        "        r.locale_names as report_locales,\n" +
        "        r.code as report_code,\n" +
        "        r.group_id as report_group_id,\n" +
        "        r.default_template_id as report_default_template_id,\n" +
        "        case\n" +
        "        when r.report_type = 10 then 'SIMPLE'\n" +
        "        when r.report_type = 20 then 'PRINT_FORM'\n" +
        "        when r.report_type = 30 then 'LIST_PRINT_FORM'\n" +
        "        end as report_type,\n" +
        "        t.id as template_id,\n" +
        "        t.code as template_code,\n" +
        "        t.is_custom as template_is_custom,\n" +
        "        t.custom_class as template_custom_class,\n" +
        "        t.output_name_pattern as template_output_name_pattern,\n" +
        "        case\n" +
        "            when t.output_type = 0 then 'XLS'\n" +
        "            when t.output_type = 10 then 'DOC'\n" +
        "            when t.output_type = 20 then 'PDF'\n" +
        "            when t.output_type = 30 then 'HTML'\n" +
        "        end as template_output_type,\n" +
        "        t.name as template_name,\n" +
        "        b.id as band_id,\n" +
        "        b.name as band_name,\n" +
        "        case\n" +
        "        when b.orientation = 0 then 'HORIZONTAL'\n" +
        "        when b.orientation = 1 then 'VERTICAL'\n" +
        "        end as band_orient,\n" +
        "        b.position_ as band_pos,\n" +
        "        b.parent_definition_id as band_parent_id,\n" +
        "        d.id as ds_id,\n" +
        "        d.name as ds_name,\n" +
        "        d.text as ds_text,\n" +
        "        case\n" +
        "            when d.data_set_type = 10 then 'SQL'\n" +
        "            when d.data_set_type = 20 then 'JPQL'\n" +
        "            when d.data_set_type = 30 then 'GROOVY'\n" +
        "            when d.data_set_type = 40 then 'SINGLE'\n" +
        "            when d.data_set_type = 50 then 'MULTI'\n" +
        "        end as ds_type,\n" +
        "        d.entity_param_name as ds_ent,\n" +
        "        d.list_entities_param_name as ds_lst,\n" +
        "        p.id as p_id,\n" +
        "        case\n" +
        "            when p.PARAMETER_TYPE = 10 then 'DATE'\n" +
        "            when p.PARAMETER_TYPE = 20 then 'TEXT'\n" +
        "            when p.PARAMETER_TYPE = 30 then 'ENTITY'\n" +
        "            when p.PARAMETER_TYPE = 40 then 'BOOLEAN'\n" +
        "            when p.PARAMETER_TYPE = 50 then 'NUMERIC'\n" +
        "            when p.PARAMETER_TYPE = 60 then 'ENTITY_LIST'\n" +
        "            when p.PARAMETER_TYPE = 70 then 'ENUMERATION'\n" +
        "            when p.PARAMETER_TYPE = 80 then 'DATETIME'\n" +
        "            when p.PARAMETER_TYPE = 90 then 'TIME'\n" +
        "        end as p_type,\n" +
        "        p.NAME as p_name,\n" +
        "        p.LOCALE_NAMES as p_loc_name,\n" +
        "        p.ALIAS as p_alias,\n" +
        "        p.SCREEN as p_screen,\n" +
        "        p.REQUIRED as p_required,\n" +
        "        p.POSITION_ as p_pos,\n" +
        "        p.META_CLASS as p_meta_class,\n" +
        "        p.ENUM_CLASS as p_enum_class,\n" +
        "        f.id as format_id,\n" +
        "        f.name as format_name,\n" +
        "        f.format as format_value,\n" +
        "        s.id as screen_id,\n" +
        "        s.screen_id as screen,\n" +
        "        ro.role_id as role_id\n" +
        "        from report_report r\n" +
        "        left join report_template t on t.report_id = r.id\n" +
        "        left join report_band_definition b on b.report_id = r.id\n" +
        "        left join report_data_set d on d.band_definition = b.id\n" +
        "        left join report_input_parameter p on p.report_id = r.id\n" +
        "        left join report_value_format f on f.report_id = r.id\n" +
        "        left join report_report_screen s on s.report_id = r.id\n" +
        "        left join report_reports_roles ro on ro.report_id = r.id\n" +
        "    </select>\n" +
        "\n" +
        "    <resultMap id=\"reportMap\" type=\"com.haulmont.reports.entity.Report\">\n" +
        "        <id property=\"id\" column=\"report_id\"/>\n" +
        "        <result property=\"id\" column=\"report_id\"/>\n" +
        "        <result property=\"name\" column=\"report_name\"/>\n" +
        "        <result property=\"localeNames\" column=\"locale_names\"/>\n" +
        "        <result property=\"code\" column=\"report_code\"/>\n" +
        "        <result property=\"reportType\" column=\"report_type\"/>\n" +
        "\n" +
        "        <association property=\"group\" column=\"group_id\" javaType=\"com.haulmont.reports.entity.ReportGroup\">\n" +
        "            <id property=\"id\" column=\"report_group_id\"/>\n" +
        "        </association>\n" +
        "\n" +
        "        <association property=\"defaultTemplate\" column=\"default_template_id\" javaType=\"com.haulmont.reports.entity.ReportTemplate\">\n" +
        "            <id property=\"id\" column=\"report_default_template_id\"/>\n" +
        "        </association>\n" +
        "\n" +
        "        <collection property=\"templates\" ofType=\"com.haulmont.reports.entity.ReportTemplate\">\n" +
        "            <id property=\"id\" column=\"template_id\"/>\n" +
        "            <result property=\"id\" column=\"template_id\"/>\n" +
        "            <result property=\"code\" column=\"template_code\"/>\n" +
        "            <result property=\"custom\" column=\"template_is_custom\"/>\n" +
        "            <result property=\"customDefinition\" column=\"template_custom_class\"/>\n" +
        "            <result property=\"outputNamePattern\" column=\"template_output_name_pattern\"/>\n" +
        "            <result property=\"name\" column=\"template_name\"/>\n" +
        "            <result property=\"reportOutputType\" column=\"template_output_type\"/>\n" +
        "        </collection>\n" +
        "\n" +
        "        <collection property=\"inputParameters\" ofType=\"com.haulmont.reports.entity.ReportInputParameter\">\n" +
        "            <id property=\"id\" column=\"p_id\"/>\n" +
        "            <result property=\"id\" column=\"p_id\"/>\n" +
        "            <result property=\"name\" column=\"p_name\"/>\n" +
        "            <result property=\"type\" column=\"p_type\"/>\n" +
        "            <result property=\"localeNames\" column=\"p_loc_name\"/>\n" +
        "            <result property=\"alias\" column=\"p_alias\"/>\n" +
        "            <result property=\"screen\" column=\"p_screen\"/>\n" +
        "            <result property=\"required\" column=\"p_required\"/>\n" +
        "            <result property=\"position\" column=\"p_pos\"/>\n" +
        "            <result property=\"entityMetaClass\" column=\"p_meta_class\"/>\n" +
        "            <result property=\"enumerationClass\" column=\"p_enum_class\"/>\n" +
        "        </collection>\n" +
        "\n" +
        "        <collection property=\"valuesFormats\" ofType=\"com.haulmont.reports.entity.ReportValueFormat\">\n" +
        "            <id property=\"id\" column=\"format_id\"/>\n" +
        "            <result property=\"id\" column=\"format_id\"/>\n" +
        "            <result property=\"valueName\" column=\"format_name\"/>\n" +
        "            <result property=\"formatString\" column=\"format_value\"/>\n" +
        "        </collection>\n" +
        "\n" +
        "        <collection property=\"reportScreens\" ofType=\"com.haulmont.reports.entity.ReportScreen\">\n" +
        "            <id property=\"id\" column=\"screen_id\"/>\n" +
        "            <result property=\"id\" column=\"screen_id\"/>\n" +
        "            <result property=\"screenId\" column=\"screen\"/>\n" +
        "        </collection>\n" +
        "\n" +
        "        <collection property=\"roles\" ofType=\"com.haulmont.cuba.security.entity.Role\">\n" +
        "            <id property=\"id\" column=\"role_id\"/>\n" +
        "            <result property=\"id\" column=\"role_id\"/>\n" +
        "        </collection>\n" +
        "\n" +
        "        <collection property=\"bands\" ofType=\"com.haulmont.reports.entity.BandDefinition\">\n" +
        "            <id property=\"id\" column=\"band_id\"/>\n" +
        "            <result property=\"id\" column=\"band_id\"/>\n" +
        "            <result property=\"name\" column=\"band_name\"/>\n" +
        "            <result property=\"orientation\" column=\"band_orient\"/>\n" +
        "            <result property=\"position\" column=\"band_pos\"/>\n" +
        "\n" +
        "            <association property=\"parentBandDefinition\" column=\"band_parent_id\"\n" +
        "                         javaType=\"com.haulmont.reports.entity.BandDefinition\">\n" +
        "                <id property=\"id\" column=\"band_parent_id\"/>\n" +
        "                <result property=\"id\" column=\"band_parent_id\"/>\n" +
        "            </association>\n" +
        "\n" +
        "            <collection property=\"dataSets\" ofType=\"com.haulmont.reports.entity.DataSet\">\n" +
        "                <id property=\"id\" column=\"ds_id\"/>\n" +
        "                <result property=\"id\" column=\"ds_id\"/>\n" +
        "                <result property=\"name\" column=\"ds_name\"/>\n" +
        "                <result property=\"text\" column=\"ds_text\"/>\n" +
        "                <result property=\"type\" column=\"ds_type\"/>\n" +
        "                <result property=\"entityParamName\" column=\"ds_ent\"/>\n" +
        "                <result property=\"listEntitiesParamName\" column=\"ds_lst\"/>\n" +
        "            </collection>\n" +
        "        </collection>\n" +
        "    </resultMap>\n" +
        "\n" +
        "</mapper>";


postUpdate.add({
    SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
    sqlSessionFactoryBean.setDataSource((DataSource) AppBeans.get("dataSource"));
    sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("cuba-mybatis.xml"));
    def resArray = new Resource[1];

    resArray[0] = new ByteArrayResource(mapping.bytes);
    sqlSessionFactoryBean.setMapperLocations(resArray);

    SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactoryBean.getObject());

    Persistence persistence = AppBeans.get(Persistence.NAME)
    Transaction tx = persistence.createTransaction();
    try {
        EntityManager entityManager = persistence.getEntityManager();
        entityManager.createNativeQuery("update report_report r " +
                "set default_template_id = t.id " +
                "from report_template t " +
                "where r.default_template_id is null and t.report_id = r.id and t.is_default = true")
                .executeUpdate();

        List<Report> reports = (List<Report>) sqlSessionTemplate.selectList("com.haulmont.report.selectReport");
        for (Report report : reports) {
            FileStorageAPI fileStorage = AppBeans.get(FileStorageAPI.NAME);

            if (report.getTemplates() != null) {
                for (ReportTemplate reportTemplate : report.getTemplates()) {
                    Object id = entityManager.createNativeQuery("select template_file_id from report_template where id = ?1")
                            .setParameter(1, reportTemplate.getId())
                            .getFirstResult();

                    if (id != null) {
                        try {
                            FileDescriptor fileDescr = entityManager.find(FileDescriptor.class, UUID.fromString(id.toString()));
                            if (fileDescr != null) {
                                byte[] bytes = fileStorage.loadFile(fileDescr);
                                reportTemplate.setContent(bytes);
                                reportTemplate.setName(fileDescr.getName());
                                reportTemplate.setOutputNamePattern(StringUtils.substringBeforeLast(reportTemplate.getName(), ".") + "." + reportTemplate.getReportOutputType().outputType.getId())
                            }
                        } catch (FileStorageException e) {
                            System.out.println("Failed to load file " + id);
                        }
                    }

                    if (reportTemplate.equals(report.defaultTemplate) || ReportTemplate.DEFAULT_TEMPLATE_CODE.equals(reportTemplate.getCode()) || 'report$default'.equalsIgnoreCase(reportTemplate.getCode())) {
                        report.setDefaultTemplate(reportTemplate);
                    }

                    ReportTemplate updated = entityManager.merge(reportTemplate);
                }
            }

            Map<UUID, BandDefinition> map = [:]
            for (BandDefinition band : report.bands) {
                map.put(band.id, band)
            }

            for (BandDefinition band : report.bands) {
                if (band.parentBandDefinition) {
                    BandDefinition parentBand = map[band.parentBandDefinition.id]
                    if (parentBand != null) {
                        band.setParentBandDefinition(parentBand)
                        if (!parentBand.childrenBandDefinitions) {
                            parentBand.childrenBandDefinitions = []
                        }

                        parentBand.childrenBandDefinitions.add(band)
                    }
                }
            }

            for (BandDefinition band : report.bands) {
                if (band.childrenBandDefinitions) {
                    band.childrenBandDefinitions = new ArrayList<>(band.childrenBandDefinitions)
                    band.childrenBandDefinitions.sort {it.position}
                }
            }

            if (report.inputParameters) {
                report.inputParameters = new ArrayList<>(report.inputParameters)
                report.inputParameters.sort({it.position})
            }

            ReportService reportService = AppBeans.get(ReportService.NAME);
            String xml = reportService.convertToString(report);
            report.setXml(xml);

            Report updatedReport = entityManager.merge(report);
        }

        tx.commit();
    } finally {
        tx.end();
    }
})