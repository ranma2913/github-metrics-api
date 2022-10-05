package com.optum.riptide.devops.githubmetricsapi.vitalsfile

import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
@ActiveProfiles(['compiletime-tests'])
class CreateForceUpdateVitalsFilesInRepo_Job extends Specification {
  @Autowired
  GitHub githubEnterprise
  @Autowired
  VitalsFileService vitalsFileService

  @Unroll("Create Missing Vitals File In Repo = #repoFullName")
  def "Create Missing Vitals File In Repo"() {
    given:
    GHRepository repo = githubEnterprise.getRepository(repoFullName)

    expect:
    repo
    vitalsFileService.forceUpdateVitalsFile(repo, askIds, overrideCaAgileId, caAgileId, lookupProjectKey, lookupProjectFriendlyName).isPresent()

    where:
    repoFullName | askIds | overrideCaAgileId | caAgileId | lookupProjectKey | lookupProjectFriendlyName
//    'riptide-deprecated-apps/decision-tree-ui'           | [null] | true              | 'do not use rally or use other system' | true             | true
//    'riptide-deprecated-apps/gherkin-editor' | [null] | true              | 'UHGWM110-028906' | true             | true
//    'riptide-deprecated-apps/auto-phone-transfer-widget' | [null] | true              | 'do not use rally or use other system' | true             | true
  }
}
