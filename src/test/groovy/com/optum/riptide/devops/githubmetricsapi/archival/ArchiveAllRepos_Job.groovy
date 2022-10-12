package com.optum.riptide.devops.githubmetricsapi.archival


import groovy.util.logging.Slf4j
import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@Slf4j
@ActiveProfiles(['compiletime-tests'])
class ArchiveAllRepos_Job extends Specification {
  @Autowired
  GitHub githubEnterprise

  @Unroll('Archive Repos in Org = #orgName')
  def 'Archive Repos in Org'() {
    given: 'Setup'
    AtomicInteger processedCount = new AtomicInteger()

    when: 'Read the data'
    GHOrganization org = githubEnterprise.getOrganization(orgName)
    List<GHRepository> repositories = org.listRepositories(100).toList()

    then: 'Stream Processing'
    def result = repositories.stream()
        .map(repo -> {
          log.info("Processing repository {} of {}. URL = {}", processedCount.incrementAndGet(), repositories.size(), repo.htmlUrl)
          if (doArchive) {
            if (repo.isArchived()) {
              log.warn("Repository is already archived. URL = {}", repo.htmlUrl)
            } else {
              repo.archive()
            }
          }
        }).toList()

    expect: 'Validate Result'
    result

    where: 'Examples to execute'
    orgName                   | doArchive
    'riptide-deprecated-apps' | true
  }
}
