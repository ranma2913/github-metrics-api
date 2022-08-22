package com.optum.riptide.devops.githubmetricsapi.enterprise;

import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@Configuration
public class GitHubConfig {
  @Value("${github.enterprise.endpoint}")
  private String githubEnterpriseEndpoint;

  @Value("${github.enterprise.default-org}")
  private String githubEnterpriseDefaultOrg;

  @Value("${credentials_GIT_TOKEN}")
  private String githubToken;

  @Value("${credentials_MS_ID}")
  private String githubId;

  @Value("${credentials_MS_ID}")
  private String githubDefaultOrg;

  @Bean
  public GitHub githubEnterprise() throws IOException {
    // todo add OkHTTP Cached connector:
    // https://github.com/hub4j/github-api/blob/main/src/main/java/org/kohsuke/github/GitHub.java#L103
    GitHub github =
        new GitHubBuilder()
            .withEndpoint(githubEnterpriseEndpoint)
            .withOAuthToken(githubToken, githubId)
            .build();
    return github;
  }

  @Bean
  @Autowired
  public GHOrganization githubEnterpriseOrg(GitHub githubEnterprise) throws IOException {
    GHOrganization githubEnterpriseOrg =
        githubEnterprise.getOrganization(githubEnterpriseDefaultOrg);
    return githubEnterpriseOrg;
  }

  @Bean
  @Autowired
  public List<GHRepository> githubEnterpriseRepositories(GHOrganization githubEnterpriseOrg)
      throws IOException {
    // pageSize – size for each page of items returned by GitHub. Maximum page size is 100.
    List<GHRepository> repositories = githubEnterpriseOrg.listRepositories(100).toList();
    return repositories;
  }
}
