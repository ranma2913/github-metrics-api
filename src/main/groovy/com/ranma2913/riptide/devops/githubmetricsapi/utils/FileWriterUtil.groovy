package com.ranma2913.riptide.devops.githubmetricsapi.utils

import groovy.util.logging.Slf4j
import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.ss.SpreadsheetVersion
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.util.AreaReference
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFHyperlink
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFTable
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumns
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo

import java.nio.file.Files
import java.nio.file.Path

@Slf4j
class FileWriterUtil {
  static def writeCsvFile(Path outputFilePath, List<List<String>> csvData) {
    def csvDataRows = csvData*.join(',')
    Files.write(outputFilePath, csvDataRows)
  }

  /**
   * @see https://thinktibits.blogspot.com/2014/09/Excel-Insert-Format-Table-Apache-POI-Example.html
   */
  static def writeSimpleXlsxFile(Path outputFilePath, List<String> headerRowData, List<List<String>> rowData, String sheetName) {
    /* Start with Creating a workbook and worksheet object */
    XSSFWorkbook wb = new XSSFWorkbook()
    CreationHelper createHelper = wb.getCreationHelper()
    /* Setup Hyperlink Style */
    XSSFCellStyle hlinkstyle = wb.createCellStyle()
    XSSFFont hlinkfont = wb.createFont()
    hlinkfont.setUnderline(XSSFFont.U_SINGLE)
    hlinkfont.setColor(IndexedColors.BLUE.index)
    hlinkstyle.setFont(hlinkfont)

    // create the worksheet
    XSSFSheet sheet = wb.createSheet(sheetName)

    // create the reader row
    XSSFRow headerRow = sheet.createRow(0)
    XSSFCell headerNameCell = headerRow.createCell(0, CellType.STRING)
    headerNameCell.setCellValue(headerRowData[0])
    XSSFCell headerUrlCell = headerRow.createCell(1, CellType.STRING)
    headerUrlCell.setCellValue(headerRowData[1])
    XSSFCell headerDefaultBranchCell = headerRow.createCell(2, CellType.STRING)
    headerDefaultBranchCell.setCellValue(headerRowData[2])

    /* Add remaining Table Data */
    for (List<String> csvRow in rowData) {
      XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1)
      XSSFCell nameCell = row.createCell(0, CellType.STRING)
      nameCell.setCellValue(csvRow[0])

      XSSFCell urlCell = row.createCell(1)
      URL url = new URL("${csvRow[1]}")
      urlCell.setCellValue(url.toString())
      XSSFHyperlink link = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL)
      link.setAddress(url.toString())
      urlCell.setHyperlink((XSSFHyperlink) link)
      urlCell.setCellStyle(hlinkstyle)

      XSSFCell defaultBranchCell = row.createCell(2, CellType.STRING)
      defaultBranchCell.setCellValue(csvRow[2])
    }

/* Auto Size Column Width now that all data is added */
    for (int i = 0; i < headerRowData.size(); i++) {
      sheet.autoSizeColumn(i)
    }

    /* Write output as File */
    try (FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath.toString())) {
      wb.write(fileOutputStream)
    }
    return outputFilePath
  }

  static def writeXlsxFile(Path path, List<CellProps> headerRowData, List<List<CellProps>> rowDataList, String sheetName) {
    /* Start with Creating a workbook and worksheet object */
    XSSFWorkbook wb = new XSSFWorkbook()

    // Setup Hyperlink Style
    XSSFCellStyle linkStyle = wb.createCellStyle()
    XSSFFont linkFont = wb.createFont()
    linkFont.setUnderline(XSSFFont.U_SINGLE)
    linkFont.setColor(IndexedColors.BLUE.index)
    linkStyle.setFont(linkFont)

    // create the worksheet
    XSSFSheet sheet = wb.createSheet(sheetName)

    // create the reader row
    XSSFRow headerRow = createRow(sheet, headerRowData, linkStyle)
    /* Add remaining Table Data */
    for (List<CellProps> rowData in rowDataList) {
      createRow(sheet, rowData, linkStyle)
    }

    /* Auto Size Column Width now that all data is added */
    for (int i = 0; i < headerRowData.size(); i++) {
      sheet.autoSizeColumn(i)
    }

    /* Write output as File */
    try (FileOutputStream fileOutputStream = new FileOutputStream(path.toString())) {
      wb.write(fileOutputStream)
    }
    return path
  }

  static XSSFRow createRow(XSSFSheet sheet, List<CellProps> rowData, XSSFCellStyle linkStyle) {
    XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1)
    rowData.eachWithIndex { cellProps, i ->
      XSSFCell cell = row.createCell(i, cellProps.getCellType())
      if (cellProps.getCellTypeString() == 'URL') {
        cell.setCellValue(cellProps.displayValue)
        XSSFHyperlink link = (XSSFHyperlink) row.getSheet().getWorkbook()
            .getCreationHelper().createHyperlink(HyperlinkType.URL)
        link.setAddress(new URL("$cellProps.value").toString())
        cell.setHyperlink((XSSFHyperlink) link)
        cell.setCellStyle(linkStyle)
      } else {
        cell.setCellValue(cellProps.value)
      }
    }
    return row
  }


  /**
   * currently not working.
   */
  @Deprecated
  static def writeXlsxFile(Path outputFilePath, List<String> csvHeadRow, List<List<String>> csvData,
                           String sheetName, String tableStyleInfoName, String tableDisplayName, String tableName) {
    /* Start with Creating a workbook and worksheet object */
    XSSFWorkbook wb = new XSSFWorkbook()
    CreationHelper createHelper = wb.getCreationHelper()
    /* Setup Hyperlink Style */
    XSSFCellStyle hlinkstyle = wb.createCellStyle()
    XSSFFont hlinkfont = wb.createFont()
    hlinkfont.setUnderline(XSSFFont.U_SINGLE)
    hlinkfont.setColor(IndexedColors.BLUE.index)
    hlinkstyle.setFont(hlinkfont)

    XSSFSheet sheet = wb.createSheet(sheetName)


    /* Create an object of type XSSFTable */
    /* Define the data range including headers */
    XSSFTable table = sheet.createTable(new AreaReference(
        new CellReference(0, 0),
        new CellReference(csvData.size(), csvHeadRow.size() - 1),
        SpreadsheetVersion.EXCEL2007
    ))

    /* get CTTable object*/
    CTTable ctTable = table.getCTTable()

    /* Let us define the required Style for the table */
    CTTableStyleInfo styleInfo = ctTable.addNewTableStyleInfo()
    styleInfo.setName(tableStyleInfoName)
    styleInfo.setShowColumnStripes(false) //showColumnStripes=0
    styleInfo.setShowRowStripes(true) //showRowStripes=1

    ctTable.setDisplayName(tableDisplayName)      /* this is the display name of the table */
    ctTable.setName(tableName)    /* This maps to "displayName" attribute in <table>, OOXML */
    ctTable.setId(1l) //id attribute against table as long value

    CTTableColumns columns = ctTable.addNewTableColumns()
    columns.setCount(csvHeadRow.size().longValue()) //define number of columns

    /* Define Header Information for the Table */
    for (int i = 0; i < csvHeadRow.size(); i++) {
      CTTableColumn column = columns.addNewTableColumn()
      column.setName("${csvData[i]}")
      column.setId(i)
    }

    /* Add remaining Table Data */
    csvData.each() { dataItemList ->
      XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1)
      XSSFCell nameCell = row.createCell(0, CellType.STRING)
      nameCell.setCellValue(dataItemList[0])

      XSSFCell urlCell = row.createCell(1)
      urlCell.setCellValue((dataItemList[1] as URL).toString())
      XSSFHyperlink link = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL)
      link.setAddress((dataItemList[1] as URL).toString())
      urlCell.setHyperlink((XSSFHyperlink) link)
      urlCell.setCellStyle(hlinkstyle)

      XSSFCell defaultBranchCell = row.createCell(2, CellType.STRING)
      defaultBranchCell.setCellValue(dataItemList[2])
    }

    /* Auto Size Column Width now that all data is added */
    for (int i = 0; i < csvHeadRow.size(); i++) {
      sheet.autoSizeColumn(i)
    }

    /* Write output as File */
    try (FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath.toString())) {
      wb.write(fileOutputStream)
    }
  }


}
