/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.wizard.template.generators;

import com.haulmont.reports.entity.wizard.RegionProperty;
import com.haulmont.reports.entity.wizard.ReportRegion;
import org.apache.poi.ss.util.CellReference;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.xlsx4j.sml.*;

import javax.xml.bind.JAXBException;

/**
 * @author fedorchenko
 * @version $Id$
 */
public class XlsxGenerator extends AbstractOfficeGenerator {
    public static final String CELL_MASK = "$%s$%s";
    protected static final String SHEET = "Sheet1"; //PartName can`t contain non-utf symbols cause it used URI encoding and after it looks like %D0%9B%D0%B8%D1%81%D1%821.xml for Лист1.xml

    protected OpcPackage generatePackage() throws Docx4JException, JAXBException {
        SpreadsheetMLPackage pkg = SpreadsheetMLPackage.createPackage();
        //String sheetInternalName = ((Messages) AppBeans.get(Messages.NAME)).getMessage(getClass(), SHEET);
        WorksheetPart sheet = pkg.createWorksheetPart(new PartName("/xl/worksheets/" + SHEET + ".xml"), SHEET, 1);
        SheetData sheetData = sheet.getJaxbElement().getSheetData();
        org.xlsx4j.sml.ObjectFactory factory = org.xlsx4j.jaxb.Context.getsmlObjectFactory();
        CTCalcPr ctCalcPr = factory.createCTCalcPr();
        ctCalcPr.setCalcMode(STCalcMode.AUTO);
        pkg.getWorkbookPart().getJaxbElement().setCalcPr(ctCalcPr);


        DefinedNames definedNames = factory.createDefinedNames();
        long rowNum = 1; //first row of sheet is '1'
        long startedRowForRegion;
        long endedRowForRegion;
        for (ReportRegion reportRegion : reportData.getReportRegions()) {
            if (reportRegion.isTabulatedRegion()) {
                rowNum++;//insert empty row before table
                int colNum = 1;                     //first column of sheet is '1'
                for (RegionProperty regionProperty : reportRegion.getRegionProperties()) {
                    sheetData.getRow().add(createRow(factory, regionProperty.getHierarchicalLocalizedNameExceptRoot(), colNum++, rowNum));
                }
                rowNum++;
                startedRowForRegion = rowNum;
                colNum = 1;
                for (RegionProperty regionProperty : reportRegion.getRegionProperties()) {
                    sheetData.getRow().add(createRow(factory, reportTemplatePlaceholder.getPlaceholderValue(regionProperty.getHierarchicalNameExceptRoot(), reportRegion), colNum++, rowNum));
                }
                endedRowForRegion = rowNum;
                rowNum++;
                rowNum++;//insert empty row after table
            } else {
                startedRowForRegion = rowNum;
                for (RegionProperty regionProperty : reportRegion.getRegionProperties()) {
                    Row row = factory.createRow();
                    row.setR(rowNum);
                    row.getC().add(createCell(factory, regionProperty.getHierarchicalLocalizedNameExceptRoot() + ":", 1, rowNum));
                    row.getC().add(createCell(factory, reportTemplatePlaceholder.getPlaceholderValue(regionProperty.getHierarchicalNameExceptRoot(), reportRegion), 2, rowNum));
                    sheetData.getRow().add(row);
                    rowNum++;
                }
                endedRowForRegion = rowNum - 1;
            }

            addDefinedNames(SHEET, factory, definedNames, startedRowForRegion, endedRowForRegion, reportRegion);
        }
        pkg.getWorkbookPart().getJaxbElement().setDefinedNames(definedNames);
        return pkg;
    }

    private void addDefinedNames(String sheetInternalName, ObjectFactory factory, DefinedNames definedNames, long startedRowForRegion, long endedRowForRegion, ReportRegion reportRegion) {
        String regionCellFrom = String.format(CELL_MASK, "A", String.valueOf(startedRowForRegion));
        String regionCellTo = String.format(CELL_MASK,
                (reportRegion.isTabulatedRegion() ? CellReference.convertNumToColString(reportRegion.getRegionProperties().size() - 1) : "B"),
                String.valueOf(endedRowForRegion));
        if (reportRegion.isTabulatedRegion()) {
            //create defined name for a header of table
            CTDefinedName ctDefinedName = factory.createCTDefinedName();
            ctDefinedName.setName(reportRegion.getNameForHeaderBand());
            String regionHeaderCellFrom = String.format(CELL_MASK,
                    "A",
                    String.valueOf(startedRowForRegion - 1));
            String regionHeaderCellTo = String.format(CELL_MASK,
                    CellReference.convertNumToColString(reportRegion.getRegionProperties().size() - 1),
                    String.valueOf(endedRowForRegion - 1));
            ctDefinedName.setValue(sheetInternalName + "!" + regionHeaderCellFrom + ":" + regionHeaderCellTo);
            definedNames.getDefinedName().add(ctDefinedName);
        }
        CTDefinedName ctDefinedName = factory.createCTDefinedName();
        ctDefinedName.setName(reportRegion.getNameForBand());
        ctDefinedName.setValue(sheetInternalName + "!" + regionCellFrom + ":" + regionCellTo);
        definedNames.getDefinedName().add(ctDefinedName);
    }


}
