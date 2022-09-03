package com.optum.riptide.devops.githubmetricsapi.enterprise

import groovy.util.logging.Slf4j
import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

@Slf4j
@SpringBootTest
class GitHub_DeleteEmptyRepos_Job extends Specification {
  @Autowired
  GitHub githubEnterprise

  @Unroll("Delete Empty Repositories in Org = #orgName")
  def "Delete Empty Repositories in Org"() {
    given:
    GHOrganization org = githubEnterprise.getOrganization(orgName)
    List<GHRepository> deletedRepositories = []
    List<GHRepository> repositories = org.listRepositories(100).toList()

    repositories.parallelStream()
        .map(
            repo -> {
              try {
                repo.getDirectoryContent('/')
              } catch (e) {
                if (e.getLocalizedMessage().containsIgnoreCase("This repository is empty")) {
                  log.warn("Repo is empty. Adding to delete list: {} ...", repo.getHtmlUrl())
                  TimeUnit.SECONDS.sleep(15)
                  // repo.delete() // throws exception: "Repository cannot be deleted until it is done being created on disk."
                  deletedRepositories.add(repo)
                } else {
                  log.error("Unable to get directory content for repo {}", repo)
                }
              }
            })
        .toList()

    def deletedRepositoriesNames = deletedRepositories.stream().map(repo -> { return repo.getHtmlUrl() }).toList()
    log.warn("deletedRepositories count = {}, deletedRepositoriesNames = {}", deletedRepositories.size(), deletedRepositoriesNames)

    expect:
    deletedRepositories

    where:
    count | orgName
    1     | 'riptide-team'

  }

}