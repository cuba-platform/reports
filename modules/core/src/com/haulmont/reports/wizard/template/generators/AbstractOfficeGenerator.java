/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.wizard.template.generators;

import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.exception.TemplateGenerationException;
import com.haulmont.reports.wizard.template.Generator;
import com.haulmont.reports.wizard.template.ReportTemplatePlaceholder;
import org.apache.poi.ss.util.CellReference;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io3.Save;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.wml.*;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author fedorchenko
 * @version $Id$
 */
public abstract class AbstractOfficeGenerator implements Generator {
    protected ReportTemplatePlaceholder reportTemplatePlaceholder = new ReportTemplatePlaceholder();

    protected ReportData reportData;

    public byte[] generate(ReportData reportData) throws TemplateGenerationException {
        this.reportData = reportData;
        byte[] template;
        OpcPackage basePackage;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            basePackage = generatePackage();
            Save saver = new Save(basePackage);
            saver.save(byteArrayOutputStream);
            template = byteArrayOutputStream.toByteArray();
        } catch (Docx4JException | JAXBException | IOException e) {
            throw new TemplateGenerationException(e);
        }
        return template;
    }

    public Row createRow(org.xlsx4j.sml.ObjectFactory factory, String stringContent, int colNum, long rowNum) {
        Row row = factory.createRow();
        row.setR(rowNum);
        Cell cell = createCell(factory, stringContent, colNum, rowNum);
        cell.setS(1L);
        row.getC().add(cell);
        return row;
    }

    public Cell createCell(org.xlsx4j.sml.ObjectFactory factory, String stringContent, int colNum, long rowNum) {
        Cell cell = factory.createCell();
        cell.setT(STCellType.STR);
        cell.setV(stringContent);
        cell.setR(CellReference.convertNumToColString(colNum - 1) + "" + rowNum);
        return cell;
    }

    public void fillWordTableRow(List<String> stringData, org.docx4j.wml.ObjectFactory factory, Tr tableRow) {
        int columnNumber = 0;
        for (String s : stringData) {
            Tc column = (Tc) tableRow.getContent().get(columnNumber++);
            P columnParagraph = (P) column.getContent().get(0);
            Text text = factory.createText();
            text.setValue(s);
            R run = factory.createR();
            run.getContent().add(text);
            columnParagraph.getContent().add(run);
        }
    }

    protected abstract OpcPackage generatePackage() throws TemplateGenerationException, Docx4JException, JAXBException;
}
