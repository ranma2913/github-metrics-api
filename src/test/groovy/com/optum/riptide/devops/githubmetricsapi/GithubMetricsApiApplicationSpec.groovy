package com.optum.riptide.devops.githubmetricsapi


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class GithubMetricsApiApplicationSpec extends Specification {

  @Autowired(required = false)
  GithubMetricsApiApplication application

  def "Context is initialized"() {
    expect: "application is not null"
    application
  }

}
