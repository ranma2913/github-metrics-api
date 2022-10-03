package com.optum.riptide.devops.githubmetricsapi.vault

import com.optum.riptide.devops.githubmetricsapi.maven.PomParserService
import com.optum.riptide.devops.githubmetricsapi.utils.CellProps
import com.optum.riptide.devops.githubmetricsapi.utils.FileWriterUtil
import groovy.util.logging.Slf4j
import groovy.xml.slurpersupport.NodeChild
import org.kohsuke.github.GHContent
import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@Slf4j
@ActiveProfiles(['compiletime-tests'])
class PasswordVaultingReport_Job extends Specification {
  @Autowired
  GitHub githubEnterprise
  @Autowired
  PomParserService pomParserService

  def "Password Vault Utilization Report"() {
    given: 'define the header row'
    // Header Row
    List<CellProps> headerRow = [
        new CellProps('Repository', 'String'),
        new CellProps('hasDatabase', 'String'),
        new CellProps('hasVault', 'String')
    ]
    // data rows
    List<List<CellProps>> dataRows = []
    AtomicInteger processedCount = new AtomicInteger()

    when: 'read the data'
    GHOrganization org = githubEnterprise.getOrganization(orgName)
    List<GHRepository> repositories = org.listRepositories(100).toList()

    repositories.stream()
        .map(repo -> {

          log.info("Processing repository {} of {}. URL = {}", processedCount.incrementAndGet(), repositories.size(), repo.htmlUrl)
          boolean hasDatabase = false
          boolean hasVault = false
          Optional<GHContent> pomContentOptional = pomParserService.getPomFileContent(repo, 'pom.xml')
          Optional<List<NodeChild>> rootDependencies
          if (pomContentOptional.isPresent()) {
            rootDependencies = pomParserService.readDependenciesRecursive(pomContentOptional.get())
            if (rootDependencies.isPresent()) {
              for (dependency in rootDependencies.get()) {
                if (dependency.artifactId.text().contains('mysql')) {
                  hasDatabase = true
                }
                if (dependency.artifactId.text().contains('spring-cloud-starter-vault-config')) {
                  hasVault = true
                }
              }
            }
          }

          // data rows
          List<CellProps> dataRow = [
              new CellProps("${repo.getHtmlUrl()}", "${(repo.getFullName()?.trim() ?: "${repo.getOwner()}/${repo.getName()}")}", 'URL'),
              new CellProps("$hasDatabase", 'BOOLEAN'),
              new CellProps("$hasVault", 'BOOLEAN')
          ]

          dataRows.add(dataRow)
        }).toList()

    then: 'export the file'
    Path outputFilePath = Paths.get("target/${LocalDateTime.now().toString().replace(':', '')}_$outputFileName")
    FileWriterUtil.writeXlsxFile(outputFilePath, headerRow, dataRows, sheetName)

    expect: 'check output row count is higher than 0'
    dataRows.size() == repositories.size()

    where: 'examples to execute'
    orgName       | sheetName                | outputFileName
//    'iset'  | 'mysql and vault report' | 'iset mysql and vault report.xlsx'
//    'riptide-team' | 'mysql and vault report' | 'riptide-team mysql and vault report.xlsx'
//    'riptide-devops'          | 'mysql and vault report' | 'riptide-devops mysql and vault report.xlsx'
    'riptide-poc' | 'mysql and vault report' | 'riptide-poc mysql and vault report.xlsx'
//    'riptide-team-microsite'  | 'mysql and vault report' | 'riptide-team-microsite mysql and vault report.xlsx'
//    'riptide-deprecated-apps' | 'mysql and vault report' | 'riptide-deprecated-apps mysql and vault report.xlsx'
//    'UHC-Motion'              | 'mysql and vault report' | 'UHC-Motion mysql and vault report.xlsx'
//    'DigitalMemberships'      | 'mysql and vault report' | 'DigitalMemberships mysql and vault report.xlsx'
//    'acet-salesforce'         | 'mysql and vault report' | 'acet-salesforce mysql and vault report.xlsx'
//    'ACET-Middleware'         | 'mysql and vault report' | 'ACET-Middleware mysql and vault report.xlsx'
//    'pact-salesforce'         | 'mysql and vault report' | 'pact-salesforce mysql and vault report.xlsx'
//    'PACT-Middleware'         | 'mysql and vault report' | 'PACT-Middleware mysql and vault report.xlsx'
//    'ACET-Automation'         | 'mysql and vault report' | 'ACET-Automation mysql and vault report.xlsx'
  }

  def hasVault() {}

}
