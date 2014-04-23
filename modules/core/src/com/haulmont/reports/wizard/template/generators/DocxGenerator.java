/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.wizard.template.generators;

import com.haulmont.reports.entity.wizard.RegionProperty;
import com.haulmont.reports.entity.wizard.ReportRegion;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tr;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fedorchenko
 * @version $Id$
 */
public class DocxGenerator extends AbstractOfficeGenerator {

    protected OpcPackage generatePackage() throws Docx4JException {
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
        MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();
        org.docx4j.wml.ObjectFactory factory = org.docx4j.jaxb.Context.getWmlObjectFactory();
        for (ReportRegion reportRegion : reportData.getReportRegions()) {
            if (reportRegion.isTabulatedRegion()) {
                mainDocumentPart.addParagraphOfText("");
                int writableWidthTwips = wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips();
                int cols = reportRegion.getRegionProperties().size();
                int cellWidthTwips = new Double(Math.floor((writableWidthTwips / cols))).intValue();
                Tbl table = TblFactory.createTable(2, reportRegion.getRegionProperties().size(), cellWidthTwips);
                boolean isFirstHeaderCellFounded = false; //for adding band name in table header row
                List<String> tableHeaderRowData = new ArrayList<>(reportRegion.getRegionProperties().size());
                for (RegionProperty rp : reportRegion.getRegionProperties()) {

                    if (!isFirstHeaderCellFounded) {
                        //TODO use yarg AbstractFormatter constants if U are good in regexps
                        tableHeaderRowData.add("##band=" + reportRegion.getNameForBand() + " " + rp.getHierarchicalLocalizedNameExceptRoot());

                        isFirstHeaderCellFounded = true;
                    } else {
                        tableHeaderRowData.add(rp.getHierarchicalLocalizedNameExceptRoot());
                    }
                }
                fillWordTableRow(tableHeaderRowData, factory, (Tr) table.getContent().get(0));

                List<String> tablePlaceholdersRowData = new ArrayList<>(reportRegion.getRegionProperties().size());
                for (RegionProperty rp : reportRegion.getRegionProperties()) {
                    tablePlaceholdersRowData.add(reportTemplatePlaceholder.getPlaceholderValue(rp.getHierarchicalNameExceptRoot(), reportRegion));
                }
                fillWordTableRow(tablePlaceholdersRowData, factory, (Tr) table.getContent().get(1));

                mainDocumentPart.addObject(table);
            } else {
                for (RegionProperty rp : reportRegion.getRegionProperties()) {
                    mainDocumentPart.addParagraphOfText(rp.getHierarchicalLocalizedNameExceptRoot() + ": " + reportTemplatePlaceholder.getPlaceholderValueWithBandName(rp.getHierarchicalNameExceptRoot(), reportRegion));
                }
            }
        }
        return wordMLPackage;
    }


}
