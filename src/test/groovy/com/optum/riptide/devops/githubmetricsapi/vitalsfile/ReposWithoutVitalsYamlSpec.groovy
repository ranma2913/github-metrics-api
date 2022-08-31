package com.optum.riptide.devops.githubmetricsapi.vitalsfile

import com.optum.riptide.devops.githubmetricsapi.FileWriterUtil
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
class ReposWithoutVitalsYamlSpec extends Specification {
  @Autowired
  GitHub githubEnterprise
  @Autowired
  VitalsFileService vitalsFileService

  @Unroll("Create csv of repos missing vitals.yaml for org = #orgName")
  def "Create csv of repos missing vitals.yaml"() {
    given:
    def csvHeadRow = ['Repository', 'Repo URL', 'Default Branch'] // Header Row
    def csvData = []
    GHOrganization org = githubEnterprise.getOrganization(orgName)
    List<GHRepository> repositories = org.listRepositories(100).toList()

    Path outputFilePath = Paths.get("target/${LocalDateTime.now().toString().replace(':', '')}_$outputFileName")
    Files.createDirectories(outputFilePath.getParent())
    outputFilePath = Files.createFile(outputFilePath)

    List<GHRepository> filteredRepos =
        repositories.parallelStream()
            .filter(repo -> {
              !vitalsFileService.getExistingVitalsFile(repo).isPresent()
            })
            .map(
                repo -> {
                  def csvRow = [repo.getFullName(), repo.getHtmlUrl(), repo.getDefaultBranch()]
                  csvData.add(csvRow)
                })
            .toList()
    if (outputFilePath.toString().contains("xls")) {
      FileWriterUtil.writeSimpleXlsxFile(outputFilePath, csvHeadRow, csvData, 'Needs vitals.yaml')
    } else {
      FileWriterUtil.writeCsvFile(outputFilePath, [csvHeadRow] + csvData)
    }

    expect:
    filteredRepos.size() < repositories.size()

    where:
    orgName                   | outputFileName
    'riptide-deprecated-apps' | 'riptide-deprecated-apps_missing_vitals.xlsx'
//    'riptide-devops'          | 'riptide-devops_missing_vitals.xlsx'
//    'riptide-poc'             | 'riptide-poc_missing_vitals.xlsx'
//    'riptide-team'            | 'riptide-team_missing_vitals.xlsx'
  }
}
