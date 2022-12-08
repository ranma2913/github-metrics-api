package com.optum.riptide.devops.githubmetricsapi.utils

import groovy.util.logging.Slf4j
import org.apache.poi.ss.usermodel.CellType

@Slf4j
class CellProps {
  def value
  def displayValue
  String cellType

  CellProps(def value, def displayValue, CellType cellType) {
    this.value = value
    this.displayValue = displayValue
    this.cellType = cellType.toString()
  }

  CellProps(def value, def displayValue, String cellType) {
    this.value = value
    this.displayValue = displayValue
    this.cellType = cellType
  }

  CellProps(def value, CellType cellType) {
    this.value = value
    this.displayValue = value
    this.cellType = cellType.toString()
  }

  CellProps(def value, String cellType) {
    this.value = value
    this.displayValue = value
    this.cellType = cellType
  }

  String getCellTypeString() {
    return this.cellType
  }

  CellType getCellType() {
    return lookupCellType(this.cellType)
  }

/**
 * Converts String to CellType enum. Defaulting to CellType.BLANK
 * @param cellTypeString
 * @return CellType
 */
  static CellType lookupCellType(String cellTypeString) {
    CellType cellType
    try {
      cellType = cellTypeString.toUpperCase() as CellType
    } catch (Exception e) {
      log.debug("Unable to convert String '$cellTypeString' to enum CellType. Defaulting to CellType.BLANK")
      cellType = CellType.BLANK
    }
    return cellType
  }
}
