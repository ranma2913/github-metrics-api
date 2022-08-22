package com.optum.riptide.devops.githubmetricsapi.enterprise

import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class GitHubConfigSpec extends Specification {
  @Autowired
  @Qualifier('githubEnterprise')
  GitHub githubEnterprise

  def "GithubEnterprise"() {
    expect:
    githubEnterprise
  }
}
