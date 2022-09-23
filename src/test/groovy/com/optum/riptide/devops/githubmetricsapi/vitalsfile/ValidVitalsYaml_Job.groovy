package com.optum.riptide.devops.githubmetricsapi.vitalsfile

import com.optum.riptide.devops.githubmetricsapi.schema.SchemaValidator
import com.optum.riptide.devops.githubmetricsapi.utils.FileWriterUtil
import groovy.util.logging.Slf4j
import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

@SpringBootTest
@Slf4j
@ActiveProfiles(['compiletime-tests'])
class ValidVitalsYaml_Job extends Specification {
  @Autowired
  GitHub githubEnterprise
  @Autowired
  VitalsFileService vitalsFileService
  @Autowired
  SchemaValidator schemaValidator

  @Unroll("Validate vitals.yaml for org = #orgName")
  def "Validate vitals.yaml for org"() {
    given: 'define the header row'
    def csvHeadRow = ['Repository', 'Vitals File URL', 'Vitals File Details'] // Header Row
    def csvData = []

    when: 'read the data'
    csvData = csvData.addAll(readCsvDataForOrg(orgName))

    then: 'export the file'
    writeOutputFile(csvHeadRow, csvData, outputFileName, sheetName)

    expect: 'check output row count is higher than 0'
    csvData.size() > 0

    where: 'examples to execute'
    orgName                   | sheetName                 | outputFileName
    'riptide-deprecated-apps' | 'riptide-deprecated-apps' | 'riptide-deprecated-apps_vitals_file_schema_validation.xlsx'
    'riptide-devops'          | 'riptide-devops'          | 'riptide-devops_vitals_file_schema_validation.xlsx'
    'riptide-poc'             | 'riptide-poc'             | 'riptide-poc_vitals_file_schema_validation.xlsx'
    'riptide-team'            | 'riptide-team'            | 'riptide-team_vitals_file_schema_validation.xlsx'
  }

  def "Validate vitals.yaml in multiple orgs and output one sheet"() {
    given: 'define the header row'
    def orgNames = [
        'iset',
        'riptide-team', 'riptide-devops', 'riptide-poc', 'riptide-team-microsite', 'riptide-deprecated-apps',
        'UHC-Motion', 'DigitalMemberships',
        'acet-salesforce', 'ACET-Middleware', 'pact-salesforce', 'PACT-Middleware', 'ACET-Automation',
    ]
    def sheetName = 'vitals_file_schema_validation'
    def outputFileName = 'vitals_file_schema_validation.xlsx'
    def csvHeadRow = ['Repository', 'Vitals File URL', 'Vitals File Details'] // Header Row
    def csvData = []

    when: 'read the data from all orgs'
    orgNames.each() { orgName ->
      log.info("STARTED: Reading data for org: https://github.optum.com/{}", orgName)
      def localData = readCsvDataForOrg(orgName)
      csvData.addAll(localData)
      log.info("COMPLETE")
    }

    then: 'export the file'
    writeOutputFile(csvHeadRow, csvData, outputFileName, sheetName)
  }

  def writeOutputFile(def csvHeadRow, def csvData, def outputFileName, def sheetName) {
    Path outputFilePath = Paths.get("target/${LocalDateTime.now().toString().replace(':', '')}_$outputFileName")
    Files.createDirectories(outputFilePath.getParent())
    outputFilePath = Files.createFile(outputFilePath)
    if (outputFilePath.toString().contains("xls")) {
      FileWriterUtil.writeSimpleXlsxFile(outputFilePath, csvHeadRow, csvData, sheetName)
    } else {
      FileWriterUtil.writeCsvFile(outputFilePath, [csvHeadRow] + csvData)
    }
  }

  def readCsvDataForOrg(String orgName) {
    def csvData = []
    GHOrganization org = githubEnterprise.getOrganization(orgName)
    List<GHRepository> repositories = org.listRepositories(100).toList()
    repositories.parallelStream()
        .map(
            repo -> {
              def vitalsFileDetails
              def vitalsFileContent = vitalsFileService.getExistingVitalsFile(repo)
              def vitalsFileHtmlUrl
              if (vitalsFileContent.isPresent()) {
                vitalsFileHtmlUrl = vitalsFileContent.get().getHtmlUrl()
                def validationMessage = vitalsFileService.validateVitalsFile(vitalsFileContent.get())
                if (validationMessage.size() == 0) {
                  vitalsFileDetails = 'Valid'
                } else {
                  def validationMessagesStrings = schemaValidator.validationMessagesStrings(validationMessage)
                  vitalsFileDetails = "$validationMessagesStrings"
                }
              } else {
                vitalsFileHtmlUrl = repo.getHtmlUrl()
                vitalsFileDetails = 'Not Found'
              }
              def csvRow = [repo.getFullName(), vitalsFileHtmlUrl, vitalsFileDetails]
              csvData.add(csvRow)
            }).toList()
    return csvData
  }
}

