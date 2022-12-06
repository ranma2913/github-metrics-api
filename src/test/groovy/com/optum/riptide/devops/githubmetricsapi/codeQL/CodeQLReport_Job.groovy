package com.optum.riptide.devops.githubmetricsapi.codeQL

import com.optum.riptide.devops.githubmetricsapi.codeQL.CodeQLService
import com.optum.riptide.devops.githubmetricsapi.utils.FileWriterUtilForCodeQL
import groovy.util.logging.Slf4j
import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

@SpringBootTest
@Slf4j
class CodeQLReport_Job extends Specification {
  @Autowired
  GitHub githubEnterprise
  @Autowired
  CodeQLService codeQLService

  @Unroll("Create csv of repos Code QL repo for org = #orgName")
  def "Create csv of repos Code QL status"() {
    given:
    def csvHeadRow = ['Repository', 'Repo URL', 'Default Branch', 'Code QL Merge status'] // Header Row
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
                  def csvRow = [repo.getFullName(), repo.getHtmlUrl(), repo.getDefaultBranch(), codeQLService.isCodeQLFileMerged(repo)]
                  csvData.add(csvRow)
                })
            .toList()
    if (outputFilePath.toString().contains("xls")) {
        FileWriterUtilForCodeQL.writeSimpleXlsxFile(outputFilePath, csvHeadRow, csvData, sheetName)
    } else {
        FileWriterUtilForCodeQL.writeCsvFile(outputFilePath, [csvHeadRow] + csvData)
    }

    expect:
    filteredRepos.size() == repositories.size()

    where:
    orgName        | sheetName           | outputFileName
//    'riptide-deprecated-apps' | 'Needs vitals.yaml' | 'riptide-deprecated-apps_missing_vitals.xlsx'
//    'riptide-devops'          | 'Needs vitals.yaml' | 'riptide-devops_missing_vitals.xlsx'
//    'riptide-poc'             | 'Needs vitals.yaml' | 'riptide-poc_missing_vitals.xlsx'
    'riptide-team' | 'Needs code QL file' | 'riptide-team_codeQL_report.xlsx'
  }
}
