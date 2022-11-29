package com.optum.riptide.devops.githubmetricsapi.vitalsfile

import com.optum.riptide.devops.githubmetricsapi.schema.SchemaValidator
import com.optum.riptide.devops.githubmetricsapi.utils.CellProps
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

import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

@SpringBootTest
@Slf4j
@ActiveProfiles(['compiletime-tests'])
class ValidVitalsYamlReport_Job extends Specification {
  @Autowired
  GitHub githubEnterprise
  @Autowired
  VitalsFileService vitalsFileService
  @Autowired
  SchemaValidator schemaValidator

  @Unroll("Validate vitals.yaml for org = #orgName")
  def "Validate vitals.yaml for org"() {
    given: 'define the header row'
    List<CellProps> headerRow = [
        new CellProps('Repository', 'String'),
        new CellProps('Vitals File URL', 'String'),
        new CellProps('Vitals File Details', 'String')
    ]
    List<List<CellProps>> dataRows = new LinkedList()

    when: 'read the data'
    dataRows.addAll(readCsvDataForOrg(orgName))

    then: 'export the file'
    Path outputFilePath = Paths.get("target/${LocalDateTime.now().toString().replace(':', '')}_$outputFileName")
    FileWriterUtil.writeXlsxFile(outputFilePath, headerRow, dataRows, sheetName)

    expect: 'check output row count is higher than 0'
    dataRows.size() > 0

    where: 'examples to execute'
    orgName        | sheetName                       | outputFileName
//    'iset'  | 'vitals_file_schema_validation' | 'iset_vitals_file_schema_validation.xlsx'
    'riptide-team' | 'vitals_file_schema_validation' | 'riptide-team_vitals_file_schema_validation.xlsx'
//    'riptide-devops'          | 'vitals_file_schema_validation' | 'riptide-devops_vitals_file_schema_validation.xlsx'
//    'riptide-poc'             | 'vitals_file_schema_validation' | 'riptide-poc_vitals_file_schema_validation.xlsx'
//    'riptide-team-microsite'  | 'vitals_file_schema_validation' | 'riptide-team-microsite_vitals_file_schema_validation.xlsx'
//    'riptide-deprecated-apps' | 'vitals_file_schema_validation' | 'riptide-deprecated-apps_vitals_file_schema_validation.xlsx'
//    'UHC-Motion'              | 'vitals_file_schema_validation' | 'UHC-Motion_vitals_file_schema_validation.xlsx'
//    'DigitalMemberships'      | 'vitals_file_schema_validation' | 'DigitalMemberships_vitals_file_schema_validation.xlsx'
//    'acet-salesforce'         | 'vitals_file_schema_validation' | 'acet-salesforce_vitals_file_schema_validation.xlsx'
//    'ACET-Middleware'         | 'vitals_file_schema_validation' | 'ACET-Middleware_vitals_file_schema_validation.xlsx'
//    'pact-salesforce'         | 'vitals_file_schema_validation' | 'pact-salesforce_vitals_file_schema_validation.xlsx'
//    'PACT-Middleware'         | 'vitals_file_schema_validation' | 'PACT-Middleware_vitals_file_schema_validation.xlsx'
//    'ACET-Automation'         | 'vitals_file_schema_validation' | 'ACET-Automation_vitals_file_schema_validation.xlsx'
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
    List<CellProps> headerRow = [
        new CellProps('Repository', 'String'),
        new CellProps('Vitals File URL', 'String'),
        new CellProps('Vitals File Details', 'String')
    ]
    List<List<CellProps>> dataRows = new LinkedList()

    when: 'read the data from all orgs'
    orgNames.each() { orgName ->
      log.info("STARTED: Reading data for org: https://github.optum.com/{}", orgName)
      def localData = readCsvDataForOrg(orgName)
      dataRows.addAll(localData)
      log.info("COMPLETE")
    }

    then: 'export the file'
    Path outputFilePath = Paths.get("target/${LocalDateTime.now().toString().replace(':', '')}_$outputFileName")
    FileWriterUtil.writeXlsxFile(outputFilePath, headerRow, dataRows, sheetName)
  }

  List<List<CellProps>> readCsvDataForOrg(String orgName) {
    List<List<CellProps>> csvData = []
    GHOrganization org = githubEnterprise.getOrganization(orgName)
    List<GHRepository> repositories = org.listRepositories(100).toList()
    repositories.parallelStream()
        .map(repo -> {
          def vitalsFileHtmlUrl = repo.getHtmlUrl()
          def vitalsFileDetails = 'Not Found'

          if (repo.isArchived()) {
            vitalsFileDetails = 'Repo is Archived'
          } else {
            def vitalsFileContent = vitalsFileService.getExistingVitalsFile(repo)
            if (vitalsFileContent.isPresent()) {
              vitalsFileHtmlUrl = vitalsFileContent.get().getHtmlUrl()
              def validationMessage = vitalsFileService.validateVitalsFile(vitalsFileContent.get())
              if (validationMessage.size() == 0) {
                vitalsFileDetails = 'Valid'
              } else {
                def validationMessagesStrings = schemaValidator.validationMessagesStrings(validationMessage)
                vitalsFileDetails = "$validationMessagesStrings"
              }
            }
          }
          List<CellProps> dataRow = [
              new CellProps("${(repo.getFullName()?.trim() ?: "${repo.getOwner()}/${repo.getName()}")}", 'String'),
              new CellProps(vitalsFileHtmlUrl as String, 'URL'),
              new CellProps(vitalsFileDetails as String, 'String')
          ]
          csvData.add(dataRow)
        }).toList()
    return csvData
  }
}

