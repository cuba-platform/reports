/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.cuba.report.formatters.xls.options;

import com.haulmont.cuba.report.formatters.xls.StyleOption;
import com.haulmont.cuba.report.formatters.xls.XlsFontCache;
import com.haulmont.cuba.report.formatters.xls.XlsStyleCache;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Apply custom style to target cell
 *
 * @author artamonov
 * @version $Id$
 */
public class CustomCellStyleOption implements StyleOption {

    private HSSFCell resultCell;

    private HSSFCellStyle cellStyle;

    private HSSFWorkbook templateWorkbook;

    private HSSFWorkbook workbook;

    private XlsFontCache fontCache;

    private XlsStyleCache styleCache;

    public CustomCellStyleOption(HSSFCell resultCell, HSSFCellStyle cellStyle,
                                 HSSFWorkbook templateWorkbook, HSSFWorkbook workbook,
                                 XlsFontCache fontCache, XlsStyleCache styleCache) {
        this.resultCell = resultCell;
        this.cellStyle = cellStyle;
        this.templateWorkbook = templateWorkbook;
        this.workbook = workbook;
        this.fontCache = fontCache;
        this.styleCache = styleCache;
    }

    @Override
    public void apply() {
        HSSFCellStyle newStyle = workbook.createCellStyle();
        // color
        newStyle.setFillBackgroundColor(cellStyle.getFillBackgroundColor());
        newStyle.setFillForegroundColor(cellStyle.getFillForegroundColor());
        newStyle.setFillPattern(cellStyle.getFillPattern());

        applyBorders(newStyle);

        // alignment
        newStyle.setAlignment(cellStyle.getAlignment());
        newStyle.setVerticalAlignment(cellStyle.getVerticalAlignment());
        // misc
        newStyle.setDataFormat(cellStyle.getDataFormat());
        newStyle.setHidden(cellStyle.getHidden());
        newStyle.setLocked(cellStyle.getLocked());
        newStyle.setIndention(cellStyle.getIndention());
        newStyle.setRotation(cellStyle.getRotation());
        newStyle.setWrapText(cellStyle.getWrapText());
        // font
        HSSFFont cellFont = cellStyle.getFont(templateWorkbook);
        HSSFFont newFont = workbook.createFont();

        newFont.setFontName(cellFont.getFontName());
        newFont.setItalic(cellFont.getItalic());
        newFont.setStrikeout(cellFont.getStrikeout());
        newFont.setTypeOffset(cellFont.getTypeOffset());
        newFont.setBoldweight(cellFont.getBoldweight());
        newFont.setCharSet(cellFont.getCharSet());
        newFont.setColor(cellFont.getColor());
        newFont.setUnderline(cellFont.getUnderline());
        newFont.setFontHeight(cellFont.getFontHeight());
        newFont.setFontHeightInPoints(cellFont.getFontHeightInPoints());

        newStyle.setFont(fontCache.processFont(newFont));

        resultCell.setCellStyle(styleCache.processCellStyle(newStyle));
    }

    private void applyBorders(HSSFCellStyle newStyle) {
        fixNeighbourCellBorders();

        // borders
        newStyle.setBorderLeft(cellStyle.getBorderLeft());
        newStyle.setBorderRight(cellStyle.getBorderRight());
        newStyle.setBorderTop(cellStyle.getBorderTop());
        newStyle.setBorderBottom(cellStyle.getBorderBottom());

        // border colors
        newStyle.setLeftBorderColor(cellStyle.getLeftBorderColor());
        newStyle.setRightBorderColor(cellStyle.getRightBorderColor());
        newStyle.setBottomBorderColor(cellStyle.getBottomBorderColor());
        newStyle.setTopBorderColor(cellStyle.getTopBorderColor());
    }

    private void fixNeighbourCellBorders() {
        HSSFSheet sheet = resultCell.getRow().getSheet();
        // disable neighboring cells border
        int columnIndex = resultCell.getColumnIndex();
        int rowIndex = resultCell.getRowIndex();
        // fix left border
        fixLeftBorder(sheet, columnIndex);

        // fix right border
        fixRightBorder(sheet, columnIndex);

        // fix up border
        fixUpBorder(sheet, columnIndex, rowIndex);

        // fix down border
        fixDownBorder(sheet, columnIndex, rowIndex);
    }

    private void fixLeftBorder(HSSFSheet sheet, int columnIndex) {
        if (columnIndex > 1) {
            fixLeftCell(sheet, resultCell.getRowIndex(), columnIndex - 1);
            // fix merged left border
            for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
                if (mergedRegion.isInRange(resultCell.getRowIndex(), resultCell.getColumnIndex())) {
                    int firstRow = mergedRegion.getFirstRow();
                    int lastRow = mergedRegion.getLastRow();

                    for (int leftIndex = firstRow; leftIndex <= lastRow; leftIndex++) {
                        fixLeftCell(sheet, leftIndex, columnIndex - 1);
                    }
                    break;
                }
            }
        }
    }

    private void fixLeftCell(HSSFSheet sheet, int rowIndex, int columnIndex) {
        HSSFCell leftCell = sheet.getRow(rowIndex).getCell(columnIndex);
        if (leftCell != null) {
            HSSFCellStyle newLeftStyle = workbook.createCellStyle();
            HSSFCellStyle leftCellStyle = leftCell.getCellStyle();
            newLeftStyle.cloneStyleRelationsFrom(leftCellStyle);
            newLeftStyle.setBorderRight(cellStyle.getBorderLeft());
            newLeftStyle.setRightBorderColor(cellStyle.getLeftBorderColor());

            leftCell.setCellStyle(newLeftStyle);
        }
    }

    private void fixRightBorder(HSSFSheet sheet, int columnIndex) {
        fixRightCell(sheet, resultCell.getRowIndex(), columnIndex + 1);
        // fix merged right border
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.isInRange(resultCell.getRowIndex(), resultCell.getColumnIndex())) {
                int firstRow = mergedRegion.getFirstRow();
                int lastRow = mergedRegion.getLastRow();

                for (int rightIndex = firstRow; rightIndex <= lastRow; rightIndex++) {
                    fixRightCell(sheet, rightIndex, columnIndex + 1);
                }
                break;
            }
        }
    }

    private void fixRightCell(HSSFSheet sheet, int rowIndex, int columnIndex) {
        HSSFCell rightCell = sheet.getRow(rowIndex).getCell(columnIndex);
        if (rightCell != null) {
            HSSFCellStyle newRightStyle = workbook.createCellStyle();
            HSSFCellStyle rightCellStyle = rightCell.getCellStyle();
            newRightStyle.cloneStyleRelationsFrom(rightCellStyle);
            newRightStyle.setBorderLeft(cellStyle.getBorderRight());
            newRightStyle.setLeftBorderColor(cellStyle.getRightBorderColor());

            rightCell.setCellStyle(newRightStyle);
        }
    }

    private void fixUpBorder(HSSFSheet sheet, int columnIndex, int rowIndex) {
        if (rowIndex > 0) {
            // fix simple up border
            fixUpCell(sheet, rowIndex, columnIndex);
            // fix merged up border
            for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
                if (mergedRegion.isInRange(resultCell.getRowIndex(), resultCell.getColumnIndex())) {
                    int firstColumn = mergedRegion.getFirstColumn();
                    int lastColumn = mergedRegion.getLastColumn();

                    for (int upIndex = firstColumn; upIndex <= lastColumn; upIndex++) {
                        fixUpCell(sheet, rowIndex, upIndex);
                    }
                    break;
                }
            }
        }
    }

    private void fixUpCell(HSSFSheet sheet, int rowIndex, int columnIndex) {
        HSSFCell upCell = sheet.getRow(rowIndex - 1).getCell(columnIndex);
        if (upCell != null) {
            HSSFCellStyle newUpStyle = workbook.createCellStyle();
            HSSFCellStyle upCellStyle = upCell.getCellStyle();
            newUpStyle.cloneStyleRelationsFrom(upCellStyle);
            newUpStyle.setBorderBottom(cellStyle.getBorderTop());
            newUpStyle.setBottomBorderColor(cellStyle.getTopBorderColor());

            upCell.setCellStyle(newUpStyle);
        }
    }

    private void fixDownBorder(HSSFSheet sheet, int columnIndex, int rowIndex) {
        // fix simple down border
        fixDownCell(sheet, rowIndex, columnIndex);
        // fix merged down border
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.isInRange(resultCell.getRowIndex(), resultCell.getColumnIndex())) {
                int firstColumn = mergedRegion.getFirstColumn();
                int lastColumn = mergedRegion.getLastColumn();

                for (int downIndex = firstColumn; downIndex <= lastColumn; downIndex++) {
                    fixDownCell(sheet, rowIndex, downIndex);
                }
                break;
            }
        }
    }

    private void fixDownCell(HSSFSheet sheet, int rowIndex, int columnIndex) {
        HSSFCell downCell = sheet.getRow(rowIndex + 1).getCell(columnIndex);
        if (downCell != null) {
            HSSFCellStyle newDownStyle = workbook.createCellStyle();
            HSSFCellStyle downCellStyle = downCell.getCellStyle();
            newDownStyle.cloneStyleRelationsFrom(downCellStyle);
            newDownStyle.setBorderTop(cellStyle.getBorderBottom());
            newDownStyle.setTopBorderColor(cellStyle.getBottomBorderColor());

            downCell.setCellStyle(newDownStyle);
        }
    }
}
