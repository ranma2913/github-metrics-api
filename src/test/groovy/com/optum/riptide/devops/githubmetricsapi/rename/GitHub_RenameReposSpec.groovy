package com.optum.riptide.devops.githubmetricsapi.rename


import groovy.util.logging.Slf4j
import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification
import spock.lang.Unroll

@Slf4j
@SpringBootTest
class GitHub_RenameReposSpec extends Specification {
  @Autowired
  GitHub githubEnterprise

  @Unroll("rename repos in org = #orgName")
  def "rename repos in org"() {
    given:
    GHOrganization org = githubEnterprise.getOrganization(orgName)
    List<GHRepository> repositories = org.listRepositories(100).toList()

    List<GHRepository> updatedRepositories = repositories.stream()
        .filter(repo -> repo.getName().contains(target))
        .filter(repo -> !repo.isArchived())
        .map(repo -> {
          repo.renameTo(repo.getName().replace(target, replacement))
          return repo
        })
        .toList()
    List<GHRepository> archivedRepos = repositories.stream()
        .filter(repo -> repo.getName().contains(target))
        .filter(repo -> repo.isArchived())
        .map(repo -> { return repo })
        .toList()

    expect:
    updatedRepositories.size() >= 0
    archivedRepos.size() >= 0

    where:
    count | orgName                   | target        | replacement
    1     | 'riptide-deprecated-apps' | 'DEPRECATED-' | ''
  }
}