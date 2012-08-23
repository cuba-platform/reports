/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.cuba.report.formatters.xls;

import com.haulmont.cuba.report.Band;
import com.haulmont.cuba.report.ReportValueFormat;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import java.awt.Dimension;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.haulmont.cuba.report.formatters.AbstractFormatter.insertBandDataToString;
import static com.haulmont.cuba.report.formatters.AbstractFormatter.unwrapParameterName;

/**
 * @author degtyarjov
 * @version $Id$
 */
public final class HSSFCellHelper {

    private static final String CELL_DYNAMIC_STYLE_SELECTOR = "##style=";

    private HSSFCellHelper() {
    }

    /**
     * Copies template cell to result cell and fills it with band data
     *
     * @param rootBand     Root band
     * @param band         Band
     * @param templateCell Template cell
     * @param resultCell   Result cell
     * @param patriarch    Toplevel container for shapes in a sheet
     */
    public static void updateValueCell(Band rootBand, Band band, HSSFCell templateCell, HSSFCell resultCell,
                                       HSSFPatriarch patriarch) {
        Map<String, Object> bandData = band.getData();
        String parameterName = templateCell.toString();

        parameterName = unwrapParameterName(parameterName);

        if (StringUtils.isEmpty(parameterName)) return;

        if (!bandData.containsKey(parameterName)) {
            resultCell.setCellValue(templateCell.getRichStringCellValue());
            return;
        }

        Object parameterValue = bandData.get(parameterName);
        HashMap<String, ReportValueFormat> valuesFormats = rootBand.getValuesFormats();

        if (parameterValue == null)
            resultCell.setCellType(HSSFCell.CELL_TYPE_BLANK);
        else if (parameterValue instanceof Number)
            resultCell.setCellValue(((Number) parameterValue).doubleValue());
        else if (parameterValue instanceof Boolean)
            resultCell.setCellValue((Boolean) parameterValue);
        else if (parameterValue instanceof Date)
            resultCell.setCellValue((Date) parameterValue);
        else if (valuesFormats.containsKey(parameterName)) {
            String formatString = valuesFormats.get(parameterName).getFormatString();
            ImageExtractor imageExtractor = new ImageExtractor(formatString, parameterValue);
            if (ImageExtractor.isImage(formatString)) {
                paintImageToCell(resultCell, patriarch, imageExtractor);
            }
        } else
            resultCell.setCellValue(new HSSFRichTextString(parameterValue.toString()));
    }

    private static void paintImageToCell(HSSFCell resultCell, HSSFPatriarch patriarch, ImageExtractor imageExtractor) {
        ImageExtractor.Image image = imageExtractor.extract();
        if (image != null) {
            int targetHeight = image.getHeight();
            resultCell.getRow().setHeightInPoints(targetHeight);
            HSSFSheet sheet = resultCell.getSheet();
            HSSFWorkbook workbook = sheet.getWorkbook();

            int pictureIdx = workbook.addPicture(image.getContent(), Workbook.PICTURE_TYPE_JPEG);

            CreationHelper helper = workbook.getCreationHelper();
            ClientAnchor anchor = helper.createClientAnchor();
            anchor.setCol1(resultCell.getColumnIndex());
            anchor.setRow1(resultCell.getRowIndex());
            anchor.setCol2(resultCell.getColumnIndex());
            anchor.setRow2(resultCell.getRowIndex());
            HSSFPicture picture = patriarch.createPicture(anchor, pictureIdx);
            Dimension imageDimension = picture.getImageDimension();
            double actualHeight = imageDimension.getHeight();
            picture.resize((double) targetHeight / actualHeight);
            picture.resize();
        }
    }

    /**
     * Inlines band data to cell.
     * No formatting supported now.
     * <p>
     * Applies named style to cell if it contains '##style=' mark
     * </p>
     * @param templateCell     Cell to inline data
     * @param resultCell       Result cell
     * @param workbook         Workbook
     * @param templateWorkbook Template workbook
     * @param band             Data source
     * @param fontCache        Font cache
     * @param styleCache       Styles   @return string with inlined band data
     * @return Cell value
     */
    public static String inlineBandDataToCellString(HSSFCell templateCell, HSSFCell resultCell,
                                                    HSSFWorkbook templateWorkbook, HSSFWorkbook workbook,
                                                    Band band,
                                                    XlsFontCache fontCache,
                                                    XlsStyleCache styleCache) {
        String resultStr = "";
        if (templateCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
            HSSFRichTextString richString = templateCell.getRichStringCellValue();
            if (richString != null) resultStr = richString.getString();
        } else {
            if (templateCell.toString() != null) resultStr = templateCell.toString();
        }

        Map<String, Object> bandData = band.getData();

        // apply dynamic style
        int stylePosition = StringUtils.indexOf(resultStr, CELL_DYNAMIC_STYLE_SELECTOR);
        if (stylePosition >= 0) {
            String styleSelector = StringUtils.trimToNull(
                    StringUtils.substring(resultStr, stylePosition + CELL_DYNAMIC_STYLE_SELECTOR.length()));
            resultStr = StringUtils.substring(resultStr, 0, stylePosition - 1);
            if (styleSelector != null && bandData.containsKey(styleSelector) && bandData.get(styleSelector) != null) {
                HSSFCellStyle cellStyle = styleCache.getStyleByName((String) bandData.get(styleSelector));

                if (cellStyle != null) {
                    applyNamedStyle(resultCell, cellStyle, templateWorkbook, workbook, fontCache, styleCache);
                }
            }
        }

        if (!"".equals(resultStr)) return insertBandDataToString(band, resultStr);

        return "";
    }

    private static void applyNamedStyle(HSSFCell resultCell, HSSFCellStyle cellStyle,
                                        HSSFWorkbook templateWorkbook, HSSFWorkbook workbook,
                                        XlsFontCache fontCache, XlsStyleCache styleCache) {
        HSSFCellStyle newStyle = workbook.createCellStyle();
        // color
        newStyle.setFillBackgroundColor(cellStyle.getFillBackgroundColor());
        newStyle.setFillForegroundColor(cellStyle.getFillForegroundColor());
        newStyle.setFillPattern(cellStyle.getFillPattern());
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

    /**
     * Detects if cell contains only one template to inline value
     *
     * @param cell - cell
     * @return -
     */
    public static boolean isOneValueCell(HSSFCell cell) {
        boolean result = true;
        if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
            String value = cell.getRichStringCellValue().getString();

            if (value.lastIndexOf("${") != 0)
                result = false;
            else
                result = value.indexOf("}") == value.length() - 1;
        }
        return result;
    }

    public static HSSFCell getCellFromReference(CellReference cref, HSSFSheet templateSheet) {
        return getCellFromReference(templateSheet, cref.getCol(), cref.getRow());
    }

    public static HSSFCell getCellFromReference(HSSFSheet templateSheet, int colIndex, int rowIndex) {
        HSSFRow row = templateSheet.getRow(rowIndex);
        row = row == null ? templateSheet.createRow(rowIndex) : row;
        HSSFCell cell = row.getCell(colIndex);
        cell = cell == null ? row.createCell(colIndex) : cell;
        return cell;
    }
}
