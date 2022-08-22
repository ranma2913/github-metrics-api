package com.optum.riptide.devops.githubmetricsapi.enterprise;

import org.kohsuke.github.GHRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * WebFlux controller -
 * https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux -
 * https://www.baeldung.com/spring-webflux
 */
@RestController
public class GitHubEnterpriseController {
  @GetMapping(path = "/api/github-enterprise/api/v3/orgs/{org}/repos")
  public Flux<GHRepository> getRepositories(@PathVariable String org) {

    //    return (Flux<GHRepository>) (new GHRepository()));
    return null;
  }

}
