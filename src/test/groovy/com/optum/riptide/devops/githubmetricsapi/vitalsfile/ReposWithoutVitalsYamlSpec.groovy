package com.optum.riptide.devops.githubmetricsapi.vitalsfile

import groovy.util.logging.Slf4j
import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

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

  def "create csv of repos missing vitals.yaml"() {
    given:
    def csvData = [
        ['Repository', 'Repo URL', 'Default Branch'] // Header Row
    ]
    GHOrganization org = githubEnterprise.getOrganization(orgName)
    List<GHRepository> repositories = org.listRepositories(100).toList()

    Path outputFilePath = Paths.get("target/${LocalDateTime.now().toString()}_$outputFileName")
    Files.createDirectories(outputFilePath.getParent())
    outputFilePath = Files.createFile(outputFilePath)

    List<GHRepository> filteredRepos =
        repositories.stream()
            .filter(repo -> {
              !vitalsFileService.getExistingVitalsFile(repo).isPresent()
            })
            .map(
                repo -> {
                  def csvRow = [repo.getFullName(), repo.getHtmlUrl(), repo.getDefaultBranch()]
                  csvData.add(csvRow)
                })
            .toList()
    def csvDataRows = csvData*.join(',')
    Files.write(outputFilePath, csvDataRows)

    expect:
    filteredRepos.size() < repositories.size()

    where:
    orgName        | outputFileName
    'riptide-team' | 'riptide-team_missing_vitals.csv'
  }
}