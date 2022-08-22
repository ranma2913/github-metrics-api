package com.optum.riptide.devops.githubmetricsapi.enterprise

import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GitHubConfig {
  @Value('${github.enterprise.endpoint}')
  String githubEnterpriseEndpoint
  @Value('${credentials_GIT_TOKEN}')
  String githubToken
  @Value('${credentials_MS_ID}')
  String githubId

  @Bean
  GitHub githubEnterprise() {
    //todo add OkHTTP Cached connector: https://github.com/hub4j/github-api/blob/main/src/main/java/org/kohsuke/github/GitHub.java#L103
    GitHub github = new GitHubBuilder()
        .withEndpoint(githubEnterpriseEndpoint)
        .withOAuthToken(githubToken, githubId)
        .build()
    return github
  }

  @Bean
  @Autowired
  GHOrganization githubEnterpriseOrg(GitHub githubEnterprise) {
    GHOrganization githubEnterpriseOrg = githubEnterprise.getOrganization('riptide-team')
    return githubEnterpriseOrg
  }

  @Bean
  @Autowired
  List<GHRepository> githubEnterpriseRepositories(GHOrganization githubEnterpriseOrg) {
    // pageSize – size for each page of items returned by GitHub. Maximum page size is 100.
    List<GHRepository> repositories = githubEnterpriseOrg.listRepositories(100).toList()
    return repositories
  }


//  GHRepository repo = github.createRepository(
//      "new-repository","this is my new repository",
//      "https://www.kohsuke.org/",true/*public*/);
//  repo.addCollaborators(github.getUser("abayer"),github.getUser("rtyler"));
//  repo.delete();
}
