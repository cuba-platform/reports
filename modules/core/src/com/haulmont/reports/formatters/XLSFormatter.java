/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.formatters;

import com.haulmont.reports.entity.Band;
import com.haulmont.reports.entity.Orientation;
import com.haulmont.reports.entity.ReportOutputType;
import com.haulmont.reports.exception.ReportingException;
import com.haulmont.reports.formatters.oo.XlsToPdfConverter;
import com.haulmont.reports.formatters.xls.*;
import com.haulmont.reports.formatters.xls.options.AutoWidthOption;
import com.haulmont.reports.formatters.xls.options.CopyColumnWidthOption;
import com.haulmont.reports.formatters.xls.options.CustomCellStyleOption;
import com.haulmont.reports.formatters.xls.options.CustomWidthOption;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.RefPtg;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static com.haulmont.reports.formatters.xls.HSSFCellHelper.*;
import static com.haulmont.reports.formatters.xls.HSSFPicturesHelper.getAllAnchors;
import static com.haulmont.reports.formatters.xls.HSSFRangeHelper.*;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class XLSFormatter extends AbstractFormatter {

    private Log log = LogFactory.getLog(getClass());

    private static final String DYNAMIC_HEIGHT_STYLE = "styleWithoutHeight";

    private static final String CELL_DYNAMIC_STYLE_SELECTOR = "##style=";
    private static final String COPY_COLUMN_WIDTH_SELECTOR = "##copyColumnWidth";
    private static final String AUTO_WIDTH_SELECTOR = "##autoWidth";
    private static final String CUSTOM_WIDTH_SELECTOR = "##width=";

    private HSSFWorkbook templateWorkbook;
    private HSSFSheet currentTemplateSheet = null;

    private XlsFontCache fontCache = new XlsFontCache();
    private XlsStyleCache styleCache = new XlsStyleCache();

    private int rownum;
    private int colnum;
    private int rowsAddedByVerticalBand = 0;
    private int rowsAddedByHorizontalBand = 0;

    private HSSFWorkbook resultWorkbook;
    private Map<String, List<SheetRange>> mergeRegionsForRangeNames = new HashMap<>();
    private Map<HSSFSheet, HSSFSheet> templateToResultSheetsMapping = new HashMap<>();
    private Map<String, Bounds> templateBounds = new HashMap<>();

    private Map<Area, List<Area>> areasDependency = new LinkedHashMap<>();
    private List<Integer> orderedPicturesId = new ArrayList<>();
    private Map<String, EscherAggregate> sheetToEscherAggregate = new HashMap<>();

    private AreaDependencyHelper areaDependencyHelper = new AreaDependencyHelper();

    private Band rootBand;
    private Map<HSSFSheet, HSSFPatriarch> drawingPatriarchsMap = new HashMap<>();

    private List<StyleOption> optionContainer = new ArrayList<>();

    public XLSFormatter() {
        registerReportExtension("xls");
        registerReportExtension("xlt");

        registerReportOutput(ReportOutputType.XLS);
        registerReportOutput(ReportOutputType.PDF);

        defaultOutputType = ReportOutputType.XLS;
    }

    @Override
    public void createDocument(Band rootBand, ReportOutputType outputType, OutputStream outputStream) {
        this.rootBand = rootBand;

        if (templateFile == null)
            throw new NullPointerException();

        try {
            initWorkbook();
        } catch (Exception e) {
            throw new ReportingException(e);
        }

        processDocument(rootBand);

        applyStyleOptions();

        outputDocument(outputType, outputStream);
    }

    private void applyStyleOptions() {
        for (StyleOption option : optionContainer) {
            option.apply();
        }
        optionContainer.clear();
    }

    private void initWorkbook() throws IOException {
        templateWorkbook = new HSSFWorkbook(getFileInputStream(templateFile));
        resultWorkbook = new HSSFWorkbook(getFileInputStream(templateFile));
        for (int sheetNumber = 0; sheetNumber < templateWorkbook.getNumberOfSheets(); sheetNumber++) {
            HSSFSheet templateSheet = templateWorkbook.getSheetAt(sheetNumber);
            HSSFSheet resultSheet = resultWorkbook.getSheetAt(sheetNumber);
            templateToResultSheetsMapping.put(templateSheet, resultSheet);
            initMergeRegions(templateSheet);

            HSSFPatriarch drawingPatriarch = resultSheet.getDrawingPatriarch();
            drawingPatriarchsMap.put(resultSheet, drawingPatriarch);
        }

        List<HSSFPictureData> allPictures = templateWorkbook.getAllPictures();
        for (HSSFPictureData allPicture : allPictures) {
            int i = resultWorkbook.addPicture(allPicture.getData(), Workbook.PICTURE_TYPE_JPEG);
            orderedPicturesId.add(i);
        }

        for (int sheetNumber = 0; sheetNumber < resultWorkbook.getNumberOfSheets(); sheetNumber++) {
            HSSFSheet resultSheet = resultWorkbook.getSheetAt(sheetNumber);
            for (int i = 0, size = resultSheet.getNumMergedRegions(); i < size; i++) {
                resultSheet.removeMergedRegion(0);//each time we remove region - they "move to left" so region 1 become region 0
            }
        }

        rownum = 0;
        colnum = 0;
    }

    private void outputDocument(ReportOutputType outputType, OutputStream outputStream) {
        if (ReportOutputType.XLS == outputType) {
            try {
                resultWorkbook.write(outputStream);
            } catch (Exception e) {
                throw new ReportingException(e);
            }
        } else if (ReportOutputType.PDF == outputType) {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                resultWorkbook.write(byteArrayOutputStream);
                XlsToPdfConverter xlsToPdfConverter = new XlsToPdfConverter();
                xlsToPdfConverter.convertXlsToPdf(byteArrayOutputStream.toByteArray(), outputStream);
            } catch (Exception e) {
                throw new ReportingException(e);
            }
        }
    }

    private void processDocument(Band rootBand) {
        for (Band childBand : rootBand.getChildrenList()) {
            writeBand(childBand);
        }

        for (Map.Entry<Area, List<Area>> entry : areasDependency.entrySet()) {
            Area original = entry.getKey();

            for (Area dependent : entry.getValue()) {
                updateBandFormula(original, dependent);
            }
        }

        for (int sheetNumber = 0; sheetNumber < templateWorkbook.getNumberOfSheets(); sheetNumber++) {
            HSSFSheet templateSheet = templateWorkbook.getSheetAt(sheetNumber);
            HSSFSheet resultSheet = resultWorkbook.getSheetAt(sheetNumber);

            copyPicturesFromTemplateToResult(templateSheet, resultSheet);
        }
    }

    private void writeBand(Band band) {
        String rangeName = band.getName();
        HSSFSheet templateSheet = getTemplateSheetForRangeName(templateWorkbook, rangeName);

        if (templateSheet != currentTemplateSheet) { //todo: reimplement. store rownum for each sheet.
            currentTemplateSheet = templateSheet;
            rownum = 0;
        }

        HSSFSheet resultSheet = templateToResultSheetsMapping.get(templateSheet);

        if (Orientation.HORIZONTAL.equals(band.getOrientation())) {
            colnum = 0;
            writeHorizontalBand(band, templateSheet, resultSheet);
        } else {
            writeVerticalBand(band, templateSheet, resultSheet);
        }
    }

    /**
     * Method writes horizontal band
     * Note: Only one band for row is supported. Now we think that many bands for row aren't usable.
     *
     * @param band          - band to write
     * @param templateSheet - template sheet
     * @param resultSheet   - result sheet
     */
    private void writeHorizontalBand(Band band, HSSFSheet templateSheet, HSSFSheet resultSheet) {
        String rangeName = band.getName();
        AreaReference templateRange = getAreaForRange(templateWorkbook, rangeName);

        if (templateRange == null) {
            log.warn("Could not fount area with name " + rangeName);
            return;
        }

        CellReference[] crefs = templateRange.getAllReferencedCells();

        CellReference topLeft, bottomRight;
        AreaReference resultRange;

        int rowsAddedByHorizontalBandBackup = rowsAddedByHorizontalBand;
        int rownumBackup = rownum;

        if (crefs != null) {
            addRangeBounds(band, crefs);

            List<HSSFRow> resultRows = new ArrayList<>();

            int currentRowNum = -1;
            int currentRowCount = -1;
            int currentColumnCount = 0;
            int offset = 0;

            topLeft = new CellReference(rownum, 0);
            // no child bands - merge regions now
            if (band.getChildrenList().isEmpty()) {
                copyMergeRegions(resultSheet, rangeName, rownum + rowsAddedByHorizontalBand,
                        getCellFromReference(crefs[0], templateSheet).getColumnIndex());
            }

            for (CellReference cellRef : crefs) {
                HSSFCell templateCell = getCellFromReference(cellRef, templateSheet);
                HSSFRow resultRow;
                if (templateCell.getRowIndex() != currentRowNum) { //create new row
                    resultRow = resultSheet.createRow(rownum + rowsAddedByHorizontalBand);
                    rowsAddedByHorizontalBand += 1;

                    if (templateCell.getCellStyle().getParentStyle() != null
                            && templateCell.getCellStyle().getParentStyle().getUserStyleName() != null
                            && templateCell.getCellStyle().getParentStyle().getUserStyleName().equals(DYNAMIC_HEIGHT_STYLE)
                            ) {
                        //resultRow.setHeight(templateCell.getRow().getHeight());
                    } else {
                        // todo do it lazy after render!
                        resultRow.setHeight(templateCell.getRow().getHeight());
                    }
                    resultRows.add(resultRow);

                    currentRowNum = templateCell.getRowIndex();
                    currentRowCount++;
                    currentColumnCount = 0;
                    offset = templateCell.getColumnIndex();
                } else {                                          // or write cell to current row
                    resultRow = resultRows.get(currentRowCount);
                    currentColumnCount++;
                }

                copyCellFromTemplate(templateCell, templateSheet, resultSheet, resultRow, offset + currentColumnCount, band);
            }

            bottomRight = new CellReference(rownum + rowsAddedByHorizontalBand - 1, offset + currentColumnCount);
            resultRange = new AreaReference(topLeft, bottomRight);

            areaDependencyHelper.addDependency(new Area(band.getName(), AreaAlign.HORIZONTAL, templateRange),
                    new Area(band.getName(), AreaAlign.HORIZONTAL, resultRange));
        }

        for (Band child : band.getChildrenList()) {
            writeBand(child);
        }

        // scheduled merge regions
        if (!band.getChildrenList().isEmpty() && crefs != null) {
            copyMergeRegions(resultSheet, rangeName, rownumBackup + rowsAddedByHorizontalBandBackup,
                    getCellFromReference(crefs[0], templateSheet).getColumnIndex());
        }

        rownum += rowsAddedByHorizontalBand;
        rowsAddedByHorizontalBand = 0;
        rownum += rowsAddedByVerticalBand;
        rowsAddedByVerticalBand = 0;
    }

    /**
     * Method writes vertical band
     * Note: no child support for vertical band ;)
     *
     * @param band          - band to write
     * @param templateSheet - template sheet
     * @param resultSheet   - result sheet
     */
    private void writeVerticalBand(Band band, HSSFSheet templateSheet, HSSFSheet resultSheet) {
        String rangeName = band.getName();
        CellReference[] crefs = getRangeContent(templateWorkbook, rangeName);

        Set<Integer> addedRowNumbers = new HashSet<Integer>();

        if (crefs != null) {
            addRangeBounds(band, crefs);

            Bounds thisBounds = templateBounds.get(band.getName());
            Bounds parentBounds = templateBounds.get(band.getParentBand().getName());
            int localRowNum = parentBounds != null ? rownum + thisBounds.row0 - parentBounds.row0 : rownum;

            colnum = colnum == 0 ? getCellFromReference(crefs[0], templateSheet).getColumnIndex() : colnum;
            copyMergeRegions(resultSheet, rangeName, localRowNum, colnum);

            int firstRow = crefs[0].getRow();
            int firstColumn = crefs[0].getCol();

            for (CellReference cref : crefs) {//create necessary rows
                int currentRow = cref.getRow();
                final int rowOffset = currentRow - firstRow;
                if (!rowExists(resultSheet, localRowNum + rowOffset)) {
                    resultSheet.createRow(localRowNum + rowOffset);
                }
                addedRowNumbers.add(cref.getRow());
            }

            CellReference topLeft = null;
            CellReference bottomRight = null;
            for (CellReference cref : crefs) {
                int currentRow = cref.getRow();
                int currentColumn = cref.getCol();
                final int rowOffset = currentRow - firstRow;
                final int columnOffset = currentColumn - firstColumn;

                HSSFCell templateCell = getCellFromReference(cref, templateSheet);
                resultSheet.setColumnWidth(colnum + columnOffset, templateSheet.getColumnWidth(templateCell.getColumnIndex()));
                HSSFCell resultCell = copyCellFromTemplate(templateCell, templateSheet, resultSheet, resultSheet.getRow(localRowNum + rowOffset), colnum + columnOffset, band);
                if (topLeft == null) {
                    topLeft = new CellReference(resultCell);
                }
                bottomRight = new CellReference(resultCell);
            }

            colnum += crefs[crefs.length - 1].getCol() - firstColumn + 1;

            AreaReference templateRange = getAreaForRange(templateWorkbook, rangeName);
            AreaReference resultRange = new AreaReference(topLeft, bottomRight);
            areaDependencyHelper.addDependency(new Area(band.getName(), AreaAlign.VERTICAL, templateRange),
                    new Area(band.getName(), AreaAlign.VERTICAL, resultRange));
        }

        //for first level vertical bands we should increase rownum by number of rows added by vertical band
        //nested vertical bands do not add rows, they use parent space
        if ("Root".equals(band.getParentBand().getName())) {
            List<Band> sameBands = band.getParentBand().getChildrenByName(band.getName());
            if (sameBands.size() > 0 && sameBands.get(sameBands.size() - 1) == band) {//check if this vertical band is last vertical band with same name
                rownum += addedRowNumbers.size();
                //      rowsAddedByVerticalBand = 0;
            }
        }
    }

    /**
     * <p>
     * Method creates mapping [rangeName -> List< CellRangeAddress >]. <br/>
     * List contains all merge regions for this named range
     * </p>
     * todo: if merged regions writes wrong - look on methods isMergeRegionInsideNamedRange & isNamedRangeInsideMergeRegion
     * todo: how to recognize if merge region must be copied with named range
     *
     * @param currentSheet Sheet which contains merge regions
     */
    private void initMergeRegions(HSSFSheet currentSheet) {
        int rangeNumber = templateWorkbook.getNumberOfNames();
        for (int i = 0; i < rangeNumber; i++) {
            HSSFName aNamedRange = templateWorkbook.getNameAt(i);

            String refersToFormula = aNamedRange.getRefersToFormula();
            if (!AreaReference.isContiguous(refersToFormula)) {
                continue;
            }

            AreaReference aref = new AreaReference(refersToFormula);

            Integer rangeFirstRow = aref.getFirstCell().getRow();
            Integer rangeFirstColumn = (int) aref.getFirstCell().getCol();
            Integer rangeLastRow = aref.getLastCell().getRow();
            Integer rangeLastColumn = (int) aref.getLastCell().getCol();

            for (int j = 0; j < currentSheet.getNumMergedRegions(); j++) {
                CellRangeAddress mergedRegion = currentSheet.getMergedRegion(j);
                if (mergedRegion != null) {
                    Integer regionFirstRow = mergedRegion.getFirstRow();
                    Integer regionFirstColumn = mergedRegion.getFirstColumn();
                    Integer regionLastRow = mergedRegion.getLastRow();
                    Integer regionLastColumn = mergedRegion.getLastColumn();

                    boolean mergedInsideNamed = isMergeRegionInsideNamedRange(
                            rangeFirstRow, rangeFirstColumn, rangeLastRow, rangeLastColumn,
                            regionFirstRow, regionFirstColumn, regionLastRow, regionLastColumn);

                    boolean namedInsideMerged = isNamedRangeInsideMergeRegion(
                            rangeFirstRow, rangeFirstColumn, rangeLastRow, rangeLastColumn,
                            regionFirstRow, regionFirstColumn, regionLastRow, regionLastColumn);

                    if (mergedInsideNamed || namedInsideMerged) {
                        String name = aNamedRange.getNameName();
                        SheetRange sheetRange = new SheetRange(mergedRegion, currentSheet.getSheetName());
                        if (mergeRegionsForRangeNames.get(name) == null) {
                            List<SheetRange> list = new ArrayList<>();
                            list.add(sheetRange);
                            mergeRegionsForRangeNames.put(name, list);
                        } else {
                            mergeRegionsForRangeNames.get(name).add(sheetRange);
                        }
                    }
                }
            }
        }
    }

    /**
     * Copies template cell to result row into result column. Fills this cell with data from band
     *
     * @param templateCell Template cell
     * @param resultRow    Result row
     * @param resultColumn Result column
     * @param band         Band
     */
    private HSSFCell copyCellFromTemplate(HSSFCell templateCell, HSSFSheet templateSheet, HSSFSheet resultSheet, HSSFRow resultRow, int resultColumn, Band band) {
        if (templateCell != null) {
            HSSFCell resultCell = resultRow.createCell(resultColumn);

            HSSFCellStyle templateStyle = templateCell.getCellStyle();
            HSSFCellStyle resultStyle = copyCellStyle(templateStyle);
            resultCell.setCellStyle(resultStyle);

            String templateCellValue = "";
            int cellType = templateCell.getCellType();

            if (cellType != HSSFCell.CELL_TYPE_FORMULA && cellType != HSSFCell.CELL_TYPE_NUMERIC) {
                HSSFRichTextString richStringCellValue = templateCell.getRichStringCellValue();
                templateCellValue = richStringCellValue != null ? richStringCellValue.getString() : "";

                Map<String, Object> bandData = band.getData();
                templateCellValue = extractStyles(templateCell, templateSheet, resultSheet,
                        resultCell, templateCellValue, bandData);
            }

            if (cellType == HSSFCell.CELL_TYPE_STRING && isOneValueCell(templateCell, templateCellValue))
                updateValueCell(rootBand, band, templateCellValue, resultCell,
                        drawingPatriarchsMap.get(resultCell.getSheet()));
            else if (cellType == HSSFCell.CELL_TYPE_FORMULA) {
                String cellValue = inlineBandDataToCellString(templateCell, templateCellValue, band);
                if (StringUtils.isNotEmpty(cellValue))
                    resultCell.setCellFormula(cellValue);
                else
                    resultCell.setCellType(HSSFCell.CELL_TYPE_BLANK);
            } else if (cellType == HSSFCell.CELL_TYPE_STRING) {
                String cellValue = inlineBandDataToCellString(templateCell, templateCellValue, band);
                if (StringUtils.isNotEmpty(cellValue))
                    resultCell.setCellValue(new HSSFRichTextString(cellValue));
                else
                    resultCell.setCellType(HSSFCell.CELL_TYPE_BLANK);
            } else {
                String cellValue = inlineBandDataToCellString(templateCell, templateCellValue, band);
                if (StringUtils.isNotEmpty(cellValue))
                    resultCell.setCellValue(cellValue);
                else
                    resultCell.setCellType(HSSFCell.CELL_TYPE_BLANK);
            }

            return resultCell;
        }

        return null;
    }


    /**
     * Inlines band data to cell.
     * No formatting supported now.
     * <p>
     * Applies named style to cell if it contains '##style=' mark
     * </p>
     *
     * @param templateCell Cell to inline data
     * @param band         Data source
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

    private String extractStyles(HSSFCell templateCell,
                                 HSSFSheet templateSheet, HSSFSheet resultSheet,
                                 HSSFCell resultCell, String templateCellValue, Map<String, Object> bandData) {
        // apply dynamic style
        int stylePosition = StringUtils.indexOf(templateCellValue, CELL_DYNAMIC_STYLE_SELECTOR);
        if (stylePosition >= 0) {
            String stringTail = StringUtils.substring(templateCellValue, stylePosition + CELL_DYNAMIC_STYLE_SELECTOR.length());
            int styleEndIndex = StringUtils.indexOf(stringTail, " ");
            if (styleEndIndex < 0)
                styleEndIndex = templateCellValue.length() - 1;

            String styleSelector = StringUtils.substring(templateCellValue, stylePosition,
                    styleEndIndex + CELL_DYNAMIC_STYLE_SELECTOR.length() + stylePosition);

            templateCellValue = StringUtils.replace(templateCellValue, styleSelector, "");

            styleSelector = StringUtils.substring(styleSelector, CELL_DYNAMIC_STYLE_SELECTOR.length());

            if (styleSelector != null && bandData.containsKey(styleSelector) && bandData.get(styleSelector) != null) {
                HSSFCellStyle cellStyle = styleCache.getStyleByName((String) bandData.get(styleSelector));

                if (cellStyle != null) {
                    optionContainer.add(new CustomCellStyleOption(resultCell, cellStyle,
                            templateWorkbook, resultWorkbook, fontCache, styleCache));
                }
            }
        }

        // apply fixed width to column from template cell
        if (StringUtils.contains(templateCellValue, COPY_COLUMN_WIDTH_SELECTOR)) {
            templateCellValue = StringUtils.replace(templateCellValue, COPY_COLUMN_WIDTH_SELECTOR, "");
            optionContainer.add(new CopyColumnWidthOption(resultSheet,
                    resultCell.getColumnIndex(), templateSheet.getColumnWidth(templateCell.getColumnIndex())));
        }

        if (StringUtils.contains(templateCellValue, AUTO_WIDTH_SELECTOR)) {
            templateCellValue = StringUtils.replace(templateCellValue, AUTO_WIDTH_SELECTOR, "");
            optionContainer.add(new AutoWidthOption(resultSheet, resultCell.getColumnIndex()));
        }


        int widthPosition = StringUtils.indexOf(templateCellValue, CUSTOM_WIDTH_SELECTOR);
        if (widthPosition >= 0) {
            String stringTail = StringUtils.substring(templateCellValue, widthPosition + CUSTOM_WIDTH_SELECTOR.length());
            int styleEndIndex = StringUtils.indexOf(stringTail, " ");
            if (styleEndIndex < 0)
                styleEndIndex = templateCellValue.length() - 1;

            String widthLengthSelector = StringUtils.substring(templateCellValue, widthPosition,
                    styleEndIndex + CUSTOM_WIDTH_SELECTOR.length() + widthPosition);

            templateCellValue = StringUtils.replace(templateCellValue, widthLengthSelector, "");

            widthLengthSelector = StringUtils.substring(widthLengthSelector, CUSTOM_WIDTH_SELECTOR.length());

            if (widthLengthSelector != null && bandData.containsKey(widthLengthSelector) && bandData.get(widthLengthSelector) != null) {
                Object width = bandData.get(widthLengthSelector);

                if (width != null && width instanceof Integer) {
                    optionContainer.add(new CustomWidthOption(resultSheet, resultCell.getColumnIndex(), (Integer) width));
                }
            }
        }

        templateCellValue = StringUtils.stripEnd(templateCellValue, null);
        return templateCellValue;
    }

    private HSSFCellStyle copyCellStyle(HSSFCellStyle templateStyle) {
        HSSFCellStyle style = styleCache.getCellStyleByTemplate(templateStyle);

        if (style == null) {
            HSSFCellStyle newStyle = resultWorkbook.createCellStyle();

            newStyle.cloneStyleRelationsFrom(templateStyle);
            HSSFFont templateFont = templateStyle.getFont(templateWorkbook);
            HSSFFont font = fontCache.getFontByTemplate(templateFont);
            if (font != null)
                newStyle.setFont(font);
            else {
                newStyle.cloneFontFrom(templateStyle);
                fontCache.addCachedFont(templateFont, newStyle.getFont(resultWorkbook));
            }
            styleCache.addCachedStyle(templateStyle, newStyle);
            style = newStyle;
        }

        return style;
    }

    /**
     * Create new merge regions in result sheet identically to range's merge regions from template.
     * Not support copy of frames and rules
     *
     * @param resultSheet            - result sheet
     * @param rangeName              - range name
     * @param firstTargetRangeRow    - first column of target range
     * @param firstTargetRangeColumn - first column of target range
     */
    private void copyMergeRegions(HSSFSheet resultSheet, String rangeName,
                                  int firstTargetRangeRow, int firstTargetRangeColumn) {
        int rangeNameIdx = templateWorkbook.getNameIndex(rangeName);
        if (rangeNameIdx == -1) return;

        HSSFName aNamedRange = templateWorkbook.getNameAt(rangeNameIdx);
        AreaReference aref = new AreaReference(aNamedRange.getRefersToFormula());
        int column = aref.getFirstCell().getCol();
        int row = aref.getFirstCell().getRow();

        List<SheetRange> regionsList = mergeRegionsForRangeNames.get(rangeName);
        if (regionsList != null)
            for (SheetRange sheetRange : regionsList) {
                if (resultSheet.getSheetName().equals(sheetRange.getSheetName())) {
                    CellRangeAddress cra = sheetRange.getCellRangeAddress();
                    if (cra != null) {
                        int regionHeight = cra.getLastRow() - cra.getFirstRow() + 1;
                        int regionWidth = cra.getLastColumn() - cra.getFirstColumn() + 1;

                        int regionVOffset = cra.getFirstRow() - row;
                        int regionHOffset = cra.getFirstColumn() - column;

                        CellRangeAddress newRegion = cra.copy();
                        newRegion.setFirstColumn(regionHOffset + firstTargetRangeColumn);
                        newRegion.setLastColumn(regionHOffset + regionWidth - 1 + firstTargetRangeColumn);

                        newRegion.setFirstRow(regionVOffset + firstTargetRangeRow);
                        newRegion.setLastRow(regionVOffset + regionHeight - 1 + firstTargetRangeRow);

                        boolean skipRegion = false;

                        for (int mergedIndex = 0; mergedIndex < resultSheet.getNumMergedRegions(); mergedIndex++) {
                            CellRangeAddress mergedRegion = resultSheet.getMergedRegion(mergedIndex);

                            if (!isIntersectedRegions(newRegion, mergedRegion))
                                continue;

                            skipRegion = true;
                        }

                        if (!skipRegion) {
                            resultSheet.addMergedRegion(newRegion);
                        }
                    }
                }
            }
    }

    private boolean isIntersectedRegions(CellRangeAddress x, CellRangeAddress y) {
        return (x.getFirstColumn() <= y.getLastColumn() &&
                x.getLastColumn() >= y.getFirstColumn() &&
                x.getLastRow() >= y.getFirstRow() &&
                x.getFirstRow() <= y.getLastRow())
                // or
                || (y.getFirstColumn() <= x.getLastColumn() &&
                y.getLastColumn() >= x.getFirstColumn() &&
                y.getLastRow() >= x.getFirstRow() &&
                y.getFirstRow() <= x.getLastRow());
    }

    /**
     * This method adds range bounds to cache. Key is bandName
     *
     * @param band  - band
     * @param crefs - range
     */
    private void addRangeBounds(Band band, CellReference[] crefs) {
        if (templateBounds.containsKey(band.getName()))
            return;
        Bounds bounds = new Bounds(crefs[0].getRow(), crefs[0].getCol(), crefs[crefs.length - 1].getRow(), crefs[crefs.length - 1].getCol());
        templateBounds.put(band.getName(), bounds);
    }

    private void updateBandFormula(Area original, Area dependent) {
        HSSFSheet templateSheet = getTemplateSheetForRangeName(templateWorkbook, original.getName());
        HSSFSheet resultSheet = templateToResultSheetsMapping.get(templateSheet);

        AreaReference area = dependent.toAreaReference();
        for (CellReference cell : area.getAllReferencedCells()) {
            HSSFCell resultCell = getCellFromReference(cell, resultSheet);

            if (resultCell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
                Ptg[] ptgs = HSSFFormulaParser.parse(resultCell.getCellFormula(), resultWorkbook);

                for (Ptg ptg : ptgs) {
                    if (ptg instanceof AreaPtg) {
                        areaDependencyHelper.updateAreaPtg(original, dependent, (AreaPtg) ptg);
                    } else if (ptg instanceof RefPtg) {
                        areaDependencyHelper.updateRefPtg(original, dependent, (RefPtg) ptg);
                    }
                }

                String calculatedFormula = HSSFFormulaParser.toFormulaString(templateWorkbook, ptgs);
                resultCell.setCellFormula(calculatedFormula);
            }
        }
    }

    /**
     * Returns EscherAggregate from sheet
     *
     * @param sheet - HSSFSheet
     * @return - EscherAggregate from sheet
     */
    private EscherAggregate getEscherAggregate(HSSFSheet sheet) {
        EscherAggregate agg = sheetToEscherAggregate.get(sheet.getSheetName());
        if (agg == null) {
            agg = sheet.getDrawingEscherAggregate();
            sheetToEscherAggregate.put(sheet.getSheetName(), agg);
        }
        return agg;
    }

    /**
     * Copies all pictures from template sheet to result sheet
     *
     * @param templateSheet - template sheet
     * @param resultSheet   - result sheet
     */
    private void copyPicturesFromTemplateToResult(HSSFSheet templateSheet, HSSFSheet resultSheet) {
        List<HSSFClientAnchor> list = getAllAnchors(getEscherAggregate(templateSheet));

        int i = 0;
        for (HSSFClientAnchor anchor : list) {
            Cell topLeft = areaDependencyHelper.getCellFromTemplate(new Cell(anchor.getCol1(), anchor.getRow1()));
            anchor.setCol1(topLeft.getCol());
            anchor.setRow1(topLeft.getRow());

            Cell bottomRight = areaDependencyHelper.getCellFromTemplate(new Cell(anchor.getCol2(), anchor.getRow2()));
            anchor.setCol2(bottomRight.getCol());
            anchor.setRow2(bottomRight.getRow());

            HSSFPatriarch sheetPatriarch = drawingPatriarchsMap.get(resultSheet);
            sheetPatriarch.createPicture(anchor, orderedPicturesId.get(i++));
        }
    }

    private static boolean rowExists(HSSFSheet sheet, int rowNumber) {
        return sheet.getRow(rowNumber) != null;
    }

    /**
     * In this class colected all methods which works with area's dependencies
     */
    private class AreaDependencyHelper {
        void updateCell(Cell cell) {
            Area areaReference = areaDependencyHelper.getAreaByCoordinate(cell.getCol(), cell.getRow());
            List<Area> dependent = areasDependency.get(areaReference);

            if (dependent != null && !dependent.isEmpty()) {
                Area destination = dependent.get(0);

                int col = cell.getCol() - areaReference.getTopLeft().getCol() + destination.getTopLeft().getCol();
                int row = cell.getRow() - areaReference.getTopLeft().getRow() + destination.getTopLeft().getRow();

                cell.setCol(col);
                cell.setRow(row);
            }
        }

        Cell getCellFromTemplate(Cell cell) {
            Cell newCell = new Cell(cell);
            updateCell(newCell);
            return newCell;
        }

        /**
         * Adds area dependency for formula calculations
         *
         * @param main      Main area
         * @param dependent Dependent area
         */
        void addDependency(Area main, Area dependent) {
            List<Area> set = areasDependency.get(main);

            if (set == null) {
                set = new ArrayList<Area>();
                areasDependency.put(main, set);
            }
            set.add(dependent);
        }

        void updateRefPtg(Area original, Area dependent, RefPtg refPtg) {
            Area areaWhichContainsPtg = getAreaByCoordinate(refPtg.getColumn(), refPtg.getRow());

            if (areaWhichContainsPtg == original) {//ptg referes inside the band - shift
                int horizontalOffset = dependent.getTopLeft().getCol() - original.getTopLeft().getCol();
                int verticalOffset = dependent.getTopLeft().getRow() - original.getTopLeft().getRow();

                refPtg.setRow(refPtg.getRow() + verticalOffset);
                refPtg.setColumn(refPtg.getColumn() + horizontalOffset);
            } else {//ptg referes outside the band - calculate
                List<Area> allDependentAreas = areasDependency.get(areaWhichContainsPtg);
                if (CollectionUtils.isEmpty(allDependentAreas)) return;

                Area dependentFromAreaWhichContainsPtg = allDependentAreas.get(0);

                int horizontalOffset = dependentFromAreaWhichContainsPtg.getTopLeft().getCol() - areaWhichContainsPtg.getTopLeft().getCol();
                int verticalOffset = dependentFromAreaWhichContainsPtg.getTopLeft().getRow() - areaWhichContainsPtg.getTopLeft().getRow();

                refPtg.setRow(refPtg.getRow() + verticalOffset);
                refPtg.setColumn(refPtg.getColumn() + horizontalOffset);
            }
        }

        void updateAreaPtg(Area original, Area dependent, AreaPtg areaPtg) {
            boolean ptgIsInsideBand = original.getTopLeft().getRow() <= areaPtg.getFirstRow() && original.getBottomRight().getRow() >= areaPtg.getLastRow() &&
                    original.getTopLeft().getCol() <= areaPtg.getFirstColumn() && original.getBottomRight().getCol() >= areaPtg.getLastColumn();

            //If areaPtg refers to cells inside the band - shift areaPtg bounds
            //If areaPtg refers to cells outside the band (refers to another band) - grow areaPtg bounds
            if (ptgIsInsideBand) {
                shiftPtgBounds(original, dependent, areaPtg);
            } else {
                growPtgBounds(original, areaPtg);
            }
        }

        void shiftPtgBounds(Area original, Area dependent, AreaPtg areaPtg) {
            int horizontalOffset = dependent.getTopLeft().getCol() - original.getTopLeft().getCol();
            int verticalOffset = dependent.getTopLeft().getRow() - original.getTopLeft().getRow();
            areaPtg.setFirstRow(areaPtg.getFirstRow() + verticalOffset);
            areaPtg.setLastRow(areaPtg.getLastRow() + verticalOffset);
            areaPtg.setFirstColumn(areaPtg.getFirstColumn() + horizontalOffset);
            areaPtg.setLastColumn(areaPtg.getLastColumn() + horizontalOffset);
        }

        void growPtgBounds(Area original, AreaPtg areaPtg) {
            Area ptgAreaReference = getAreaByCoordinate(areaPtg.getFirstColumn(), areaPtg.getFirstRow());

            List<Area> allDependentAreas = areasDependency.get(ptgAreaReference);

            if (CollectionUtils.isEmpty(allDependentAreas)) return;

            //find summary bounds of dependent areas
            int minRow = Integer.MAX_VALUE;
            int maxRow = -1;
            int minColumn = Integer.MAX_VALUE;
            int maxColumn = -1;

            for (Area currentArea : allDependentAreas) {
                int upperBound = currentArea.getTopLeft().getRow();
                int lowerBound = currentArea.getBottomRight().getRow();
                int leftBound = currentArea.getTopLeft().getCol();
                int rightBound = currentArea.getBottomRight().getCol();

                if (upperBound < minRow) minRow = upperBound;
                if (lowerBound > maxRow) maxRow = lowerBound;

                if (leftBound < minColumn) minColumn = leftBound;
                if (rightBound > maxColumn) maxColumn = rightBound;
            }

            //if area is horizontal - grow it vertically otherwise grow it horozontally (cause horizontal bands grow down and vertical grow left)
            if (AreaAlign.HORIZONTAL == original.getAlign()) {
                areaPtg.setFirstRow(minRow);
                areaPtg.setLastRow(maxRow);
            } else {
                areaPtg.setFirstColumn(minColumn);
                areaPtg.setLastColumn(maxColumn);
            }
        }

        Area getAreaByCoordinate(int col, int row) {
            for (Area areaReference : areasDependency.keySet()) {
                if (areaReference.getTopLeft().getCol() > col) continue;
                if (areaReference.getTopLeft().getRow() > row) continue;
                if (areaReference.getBottomRight().getCol() < col) continue;
                if (areaReference.getBottomRight().getRow() < row) continue;

                return areaReference;
            }

            return null;
        }
    }

    /**
     * Cell range at sheet
     */
    private class SheetRange {
        private CellRangeAddress cellRangeAddress;
        private String sheetName;

        private SheetRange(CellRangeAddress cellRangeAddress, String sheetName) {
            this.cellRangeAddress = cellRangeAddress;
            this.sheetName = sheetName;
        }

        public CellRangeAddress getCellRangeAddress() {
            return cellRangeAddress;
        }

        public String getSheetName() {
            return sheetName;
        }
    }

    /**
     * Bounds of region [(x,y) : (x1, y1)]
     */
    private static class Bounds {
        public final int row0;
        public final int column0;
        public final int row1;
        public final int column1;

        private Bounds(int row0, int column0, int row1, int column1) {
            this.row0 = row0;
            this.column0 = column0;
            this.row1 = row1;
            this.column1 = column1;
        }
    }
}