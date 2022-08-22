package com.optum.riptide.devops.githubmetricsapi.enterprisecloud


import org.kohsuke.github.GHRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

/**
 * WebFlux controller
 *  - https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux
 *  - https://www.baeldung.com/spring-webflux
 */
@RestController
class GitHubEnterpriseCloudController {

  @GetMapping(path = "/api/github-enterprise-cloud/api/v3/orgs/{org}/repos")
  private Flux<GHRepository> getRepositories(@RequestParam String orgName) {

    return new GHRepository();
  }
}
