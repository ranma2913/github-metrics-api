package com.optum.riptide.devops.githubmetricsapi.enterprise

import org.kohsuke.github.GHRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

/**
 * WebFlux controller
 *  - https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux
 *  - https://www.baeldung.com/spring-webflux
 */
@RestController
class GitHubEnterpriseController {

  @GetMapping(path = "/api/github-enterprise/api/v3/orgs/{org}/repos")
  private Flux<GHRepository> getRepositories(@PathVariable String org) {

    return new GHRepository();
  }
}
