/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.formatters.xls;

import com.haulmont.reports.entity.Band;
import com.haulmont.reports.entity.ReportValueFormat;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import java.awt.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.haulmont.reports.formatters.AbstractFormatter.insertBandDataToString;
import static com.haulmont.reports.formatters.AbstractFormatter.unwrapParameterName;

/**
 * @author degtyarjov
 * @version $Id$
 */
public final class HSSFCellHelper {

    private HSSFCellHelper() {
    }

    /**
     * Copies template cell to result cell and fills it with band data
     *
     * @param rootBand     root band
     * @param band         band
     * @param templateCellValue preprocessed template cell value
     * @param resultCell   result cell
     * @param patriarch    toplevel container for shapes in a sheet
     */
    public static void updateValueCell(Band rootBand, Band band, String templateCellValue, HSSFCell resultCell,
                                       HSSFPatriarch patriarch) {
        Map<String, Object> bandData = band.getData();
        String parameterName = templateCellValue;

        parameterName = unwrapParameterName(parameterName);

        if (StringUtils.isEmpty(parameterName)) return;

        if (!bandData.containsKey(parameterName)) {
            resultCell.setCellValue(templateCellValue);
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
     * @param band             Data source
     * @return Cell value
     */
    public static String inlineBandDataToCellString(HSSFCell templateCell, String templateCellValue, Band band) {
        String resultStr = "";
        if (templateCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
            if (templateCellValue != null) resultStr = templateCellValue;
        } else {
            if (templateCell.toString() != null) resultStr = templateCell.toString();
        }

        if (!"".equals(resultStr)) return insertBandDataToString(band, resultStr);

        return "";
    }

    /**
     * @param cell - cell
     * @return true if cell contains only one template to inline value
     */
    public static boolean isOneValueCell(HSSFCell cell, String value) {
        boolean result = true;
        if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
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