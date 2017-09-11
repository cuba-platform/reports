/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.wizard.template.generators;

import com.google.common.base.Joiner;
import com.haulmont.reports.entity.wizard.RegionProperty;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.ReportRegion;
import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.reports.wizard.template.Generator;
import com.haulmont.reports.wizard.template.ReportTemplatePlaceholder;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author birin
 * @version $Id$
 */
public class CsvGenerator implements Generator {
    protected ReportTemplatePlaceholder reportTemplatePlaceholder = new ReportTemplatePlaceholder();
    protected static final String SEPARATOR = ";";
    protected static final String WRAPPER = "\"";

    @Override
    public byte[] generate(ReportData reportData) throws TemplateGenerationException, TemplateException, IOException {
        String templateContent = generateTemplate(reportData);
        return templateContent.getBytes(StandardCharsets.UTF_8);
    }

    protected String generateTemplate(ReportData reportData) {
        List<String> headers = new ArrayList<>();
        List<String> aliases = new ArrayList<>();

        List<ReportRegion> reportRegions = reportData.getReportRegions();
        for (ReportRegion reportRegion : reportRegions) {
            List<String> propertyHeaders = new ArrayList<>();
            List<String> propertyAliases = new ArrayList<>();

            List<RegionProperty> regionProperties = reportRegion.getRegionProperties();
            for (RegionProperty regionProperty : regionProperties) {
                propertyHeaders.add(wrapField(regionProperty.getHierarchicalLocalizedNameExceptRoot()));

                String placeholderValue = reportTemplatePlaceholder.getPlaceholderValue(regionProperty.getHierarchicalNameExceptRoot(), reportRegion);
                propertyAliases.add(wrapField(placeholderValue));
            }

            headers.add(Joiner.on(SEPARATOR).join(propertyHeaders));
            aliases.add(Joiner.on(SEPARATOR).join(propertyAliases));
        }

        return Joiner.on(SEPARATOR).join(headers) + "\n" +  Joiner.on(SEPARATOR).join(aliases);
    }

    protected static String wrapField(String fieldValue) {
        return WRAPPER + fieldValue + WRAPPER;
    }
}
