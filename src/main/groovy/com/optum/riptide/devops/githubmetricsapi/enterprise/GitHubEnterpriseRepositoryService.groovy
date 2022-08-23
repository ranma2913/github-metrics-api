package com.optum.riptide.devops.githubmetricsapi.enterprise

import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GitHubEnterpriseRepositoryService {
  Logger log = LoggerFactory.getLogger(this.getClass())

  List<GHRepository> listRepositories(GHOrganization githubEnterpriseOrg) throws IOException {
    // pageSize – size for each page of items returned by GitHub. Maximum page size is 100.
    List<GHRepository> repositories = githubEnterpriseOrg.listRepositories(100).toList()
    return repositories
  }
}
