package com.optum.riptide.devops.githubmetricsapi.enterprise

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class GitHubEnterpriseRepositoryServiceSpec extends Specification {
  @Autowired
  GitHubEnterpriseRepositoryService githubEnterpriseRepositoryService

  def "GitHubEnterpriseRepositoryService initializes"() {
    expect:
    githubEnterpriseRepositoryService
  }
}
