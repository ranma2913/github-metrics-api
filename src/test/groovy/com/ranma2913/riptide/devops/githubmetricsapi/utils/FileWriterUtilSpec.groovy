package com.ranma2913.riptide.devops.githubmetricsapi.utils


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

  def "createRow"() {
    given:
    XSSFSheet sheet = wb.createSheet('sheet name')
    def rowData = [
        new CellProps('value 1', CellType.STRING),
        new CellProps('value 2', 'String'),
        new CellProps('http://value.3.url', 'URL'),
    ]
    when:
    XSSFRow row = FileWriterUtil.createRow(sheet, rowData, linkStyle)

    then:
    row
    noExceptionThrown()
  }
}
