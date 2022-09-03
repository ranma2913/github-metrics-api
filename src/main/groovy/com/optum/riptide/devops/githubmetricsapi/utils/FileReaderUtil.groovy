package com.optum.riptide.devops.githubmetricsapi.utils


import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import java.nio.file.Files
import java.nio.file.Path

class FileReaderUtil {

  /**
   * @see https://www.java67.com/2014/09/how-to-read-write-xlsx-file-in-java-apache-poi-example.html
   * @see https://thinktibits.blogspot.com/2014/09/Excel-Insert-Format-Table-Apache-POI-Example.html
   */
  static XSSFSheet getSheetFromXlsxFile(Path inputFilePath, String sheetName) {

    // Finds the workbook instance for XLSX file
    XSSFWorkbook wb = new XSSFWorkbook(Files.newInputStream(inputFilePath));

    // Return first sheet from the XLSX workbook
    XSSFSheet sheet = wb.getSheet(sheetName);

    return sheet
  }
}
