package com.optum.riptide.devops.githubmetricsapi.enterprise

import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class GitHub_TransferRepositoriesSpec extends Specification {
  @Autowired
  GitHub githubEnterprise

  def "Transfer Repositories to riptide-deprecated-apps"() {
    given:
    GHOrganization org = githubEnterprise.getOrganization("riptide-team")
    List<GHRepository> updatedRepositories = []
    List<GHRepository> repositories = org.listRepositories(100).toList()

    updatedRepositories =
        repositories.stream()
            .map(
                repo -> {
                  //todo transfer repos
                })
            .toList()

    expect:
    GHRepository repo = org.getRepository(repoName)


    where:
    count | repoName
    1     | 'name'

  }

}