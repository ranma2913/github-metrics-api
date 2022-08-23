package com.optum.riptide.devops.githubmetricsapi.enterprise

import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
class GitHubEnterpriseRepositoryServiceSpec extends Specification {
  @Autowired
  GitHub githubEnterprise
  @Autowired
  GitHubEnterpriseRepositoryService githubEnterpriseRepositoryService

  def "GitHubEnterpriseRepositoryService initializes"() {
    expect:
    githubEnterprise
    githubEnterpriseRepositoryService
  }

  @Unroll("GitHubEnterpriseRepositoryService.listRepositories(#orgName)")
  def "GitHubEnterpriseRepositoryService.listRepositories"() {
    expect:
    expectedListSize <= githubEnterpriseRepositoryService.listRepositories(githubEnterprise.getOrganization(orgName)).size()

    where:
    expectedListSize | orgName
    100              | 'riptide-team'
    20               | 'riptide-devops'
    50               | 'riptide-deprecated-apps'
  }

}
