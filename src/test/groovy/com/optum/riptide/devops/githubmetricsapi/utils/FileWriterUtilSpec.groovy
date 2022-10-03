package com.optum.riptide.devops.githubmetricsapi.utils

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import spock.lang.Shared
import spock.lang.Specification

class FileWriterUtilSpec extends Specification {
  @Shared
  XSSFWorkbook wb
  @Shared
  XSSFCellStyle linkStyle

  def setup() {
    // run before every feature method. https://spockframework.org/spock/docs/2.0/spock_primer.html#_fixture_methods
    wb = new XSSFWorkbook()
    linkStyle = wb.createCellStyle()
    XSSFFont linkFont = wb.createFont()
    linkFont.setUnderline(XSSFFont.U_SINGLE)
    linkFont.setColor(IndexedColors.BLUE.index)
    linkStyle.setFont(linkFont)
  }

  def "LookupCellType"() {
    expect:
    CellType.NUMERIC == FileWriterUtil.lookupCellType('numeric')
    CellType.STRING == FileWriterUtil.lookupCellType('String')
    CellType.FORMULA == FileWriterUtil.lookupCellType('formULA')
    CellType.BOOLEAN == FileWriterUtil.lookupCellType('boolean')
    CellType.ERROR == FileWriterUtil.lookupCellType('error')
    CellType.BLANK == FileWriterUtil.lookupCellType('URL')
  }

  def "createRow"() {
    given:
    XSSFSheet sheet = wb.createSheet('sheet name')

    when:
    XSSFRow row = FileWriterUtil.createRow(sheet, ['value 1': 'string', 'value 2': 'STRING', 'http://value.3.url': 'URL'], linkStyle)

    then:
    row
  }
}
