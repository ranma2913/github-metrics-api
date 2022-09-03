package com.optum.riptide.devops.githubmetricsapi.utils

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class XSSFWorkbookHelper {
  XSSFWorkbook makeXSSFWorkbook() {
    XSSFWorkbook wb = new XSSFWorkbook()
    return wb
  }

  XSSFSheet makeXSSFSheet(XSSFWorkbook wb, String sheetName) {
    XSSFSheet sheet = wb.createSheet(sheetName)
    return sheet
  }

  XSSFCell getCell(XSSFRow row, String cellType, int columnIndex) {
    XSSFCell cell
    switch (cellType) {
      case 'URL':

        break
      default:
        cell = row.createCell(columnIndex, CellType.STRING)
        break

        return cell
    }
  }
}
