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
    given:
    def csvHeadRow = ['Repository', 'Repo URL', 'Vitals File Details'] // Header Row
    def csvData = []
    GHOrganization org = githubEnterprise.getOrganization(orgName)
    List<GHRepository> repositories = org.listRepositories(100).toList()

    Path outputFilePath = Paths.get("target/${LocalDateTime.now().toString().replace(':', '')}_$outputFileName")
    Files.createDirectories(outputFilePath.getParent())
    outputFilePath = Files.createFile(outputFilePath)

    List<GHRepository> filteredRepos =
        repositories.parallelStream()
            .map(
                repo -> {
                  def vitalsFileDetails
                  def vitalsFileContent = vitalsFileService.getExistingVitalsFile(repo)
                  if (vitalsFileContent.isPresent()) {
                    def validationMessage = vitalsFileService.validateVitalsFile(vitalsFileContent.get())
                    if (validationMessage.size() == 0) {
                      vitalsFileDetails = 'Valid'
                    } else {
                      def validationMessagesStrings = schemaValidator.validationMessagesStrings(validationMessage)
                      vitalsFileDetails = "$validationMessagesStrings"
                    }
                  } else {
                    vitalsFileDetails = 'Not Found'
                  }
                  def csvRow = [repo.getFullName(), repo.getHtmlUrl(), vitalsFileDetails]
                  csvData.add(csvRow)
                })
            .toList()
    if (outputFilePath.toString().contains("xls")) {
      FileWriterUtil.writeSimpleXlsxFile(outputFilePath, csvHeadRow, csvData, sheetName)
    } else {
      FileWriterUtil.writeCsvFile(outputFilePath, [csvHeadRow] + csvData)
    }

    expect:
    filteredRepos.size() == repositories.size()

    where:
    orgName                   | sheetName                 | outputFileName
    'riptide-deprecated-apps' | 'riptide-deprecated-apps' | 'riptide-deprecated-apps_vitals_file_schema_validation.xlsx'
    'riptide-devops'          | 'riptide-devops'          | 'riptide-devops_vitals_file_schema_validation.xlsx'
    'riptide-poc'             | 'riptide-poc'             | 'riptide-poc_vitals_file_schema_validation.xlsx'
    'riptide-team'            | 'riptide-team'            | 'riptide-team_vitals_file_schema_validation.xlsx'
  }
}
