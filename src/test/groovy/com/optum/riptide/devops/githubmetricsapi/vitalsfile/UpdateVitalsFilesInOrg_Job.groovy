package com.optum.riptide.devops.githubmetricsapi.vitalsfile

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.google.common.util.concurrent.RateLimiter
import com.optum.riptide.devops.githubmetricsapi.cerberus.CerberusScanService
import com.optum.riptide.devops.githubmetricsapi.maven.PomParserService
import groovy.util.logging.Slf4j
import groovy.yaml.YamlSlurper
import org.kohsuke.github.GHCommit
import org.kohsuke.github.GHContent
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

@Slf4j
@SpringBootTest
@ActiveProfiles(['compiletime-tests'])
class UpdateVitalsFilesInOrg_Job extends Specification {
  @Autowired
  GitHub githubEnterprise
  @Autowired
  VitalsFileService vitalsFileService
  @Autowired
  PomParserService pomParserService
  @Autowired
  CerberusScanService cerberusScanService

  YamlSlurper yamlSlurper = new YamlSlurper()
  RateLimiter rateLimiter = RateLimiter.create(1 / 5.0)

  @Unroll("UpdateVitalsFilesInOrg = #orgName")
  def "UpdateVitalsFilesInOrg"() {
    given:
    List<GHRepository> repositories = githubEnterprise.getOrganization(orgName).listRepositories(100).toList()
    List<GHRepository> updatedRepositories = new LinkedList<GHRepository>()
    AtomicInteger updateCount = new AtomicInteger()
    AtomicInteger noUpdateCount = new AtomicInteger()

    repositories.stream()
        .map(
            repo -> {
              if (!repo.isArchived()) {
                try {
                  Optional<GHContent> vitalsFileContentOptional = vitalsFileService.getExistingVitalsFile(repo)
                  VitalsFile origVitalsFile
                  VitalsFile vitalsFile
                  if (vitalsFileContentOptional.isPresent()) {
                    GHContent vitalsFileContent = vitalsFileContentOptional.get()
                    origVitalsFile = new VitalsFile(yamlSlurper.parse(vitalsFileContent.read()) as Map)
                    vitalsFile = origVitalsFile.clone()

                    if (overrideCaAgileId) {
                      vitalsFile.metadata.caAgileId = caAgileId
                    } else if ('poc' == vitalsFile.metadata.caAgileId) {
                      // Check history of vitals file for caAgileId
                      List<GHCommit> vitalsCommits = repo.queryCommits().pageSize(50).path('vitals.yaml').list().toList()
                      for (GHCommit commit in vitalsCommits) {
                        def commitSha = commit.SHA1
                        try {
                          def historicVitals = yamlSlurper.parse(repo.getFileContent('vitals.yaml', commitSha).read())
                          if (historicVitals.metadata.caAgileId != 'poc' &&
                              historicVitals.metadata.caAgileId.replaceAll('[ud]', '').isNumber()) {
                            vitalsFile.metadata.caAgileId = historicVitals.metadata.caAgileId
                            break // exit from commit loop
                          }
                        } catch (MismatchedInputException e) {
                          log.error('Unable to read {}', commit.getHtmlUrl(), e)
                        }
                      }
                    }
                    if (vitalsFile.metadata.caAgileId.replaceAll('[ud]', '').isNumber()) {
                      // replace 'ud' from vitalsFile.metadata.caAgileId and check if what remains is a number.
                      def rallyWorkspaceId = vitalsFile.metadata.caAgileId.replaceAll('[ud]', '')
                      vitalsFile.metadata.caAgileId = rallyWorkspaceId.isNumber() ? "${rallyWorkspaceId}ud" : vitalsFile.metadata.caAgileId
                    }
                  } else {
                    vitalsFile = new VitalsFile()
                  }

                  /**
                   * Hard Code Values in Vitals.yaml
                   */
                  vitalsFile.apiVersion = 'v1'
                  vitalsFile.metadata.setAskId(askId)

                  if (lookupProjectKey) {
                    vitalsFile.metadata.projectKey = pomParserService.readProjectKey(repo, '/pom.xml')
                  }
                  if (lookupProjectFriendlyName) {
                    vitalsFile.metadata.projectFriendlyName = pomParserService.readProjectFriendlyName(repo, '/pom.xml')
                  }
                  /**
                   * if not equals to original version then do an update. Otherwise proceed.
                   */
                  if (vitalsFile != origVitalsFile) {
                    Optional<GHContent> updateResponse = vitalsFileService.updateExistingVitalsFile(repo, vitalsFile)
                    updatedRepositories.add(updateResponse.get().getOwner())
                    updateCount.getAndIncrement()
                    if (rateLimiter.tryAcquire())
                      log.debug('updateCount = {}, noUpdateCount = {}', updateCount.get(), noUpdateCount.get())

                  } else {
                    noUpdateCount.getAndIncrement()
                    if (rateLimiter.tryAcquire())
                      log.debug('updateCount = {}, noUpdateCount = {}', updateCount.get(), noUpdateCount.get())

                  }
                  CompletableFuture.supplyAsync(() -> {
                    cerberusScanService.cerberusScan(repo)
                  })

                }
                catch (IOException e) {
                  log.error("Unable to Update Vitals File in Repo = {}", repo.getHtmlUrl(), e)
                }
              } else {
                noUpdateCount.getAndIncrement()
                log.warn("Unable to Update Vitals File in an Archived Repo = {}", repo.getHtmlUrl())
              }
            }
        ).toList()

    expect: 'All repositories should be updated.'
    updatedRepositories.size() <= repositories.size()

    where:
    orgName                   | askId               | overrideCaAgileId | caAgileId                              | lookupProjectKey | lookupProjectFriendlyName
    'riptide-deprecated-apps' | [null]              | false             | 'do not use rally or use other system' | true             | true
    'riptide-devops'          | ['UHGWM110-017197'] | false             | ''                                     | false            | false
    'riptide-poc'             | [null]              | false             | 'do not use rally or use other system' | true             | true
    'riptide-team'            | ['UHGWM110-017197'] | false             | ''                                     | true             | true
    'riptide-team-microsite'  | ['UHGWM110-017197'] | false             | ''                                     | true             | true
    'iset'                    | ['UHGWM110-000465'] | false             | ''                                     | false            | false
    'UHC-Motion'              | ['UHGWM110-017808'] | false             | ''                                     | false            | false
    'DigitalMemberships'      | ['UHGWM110-027031'] | false             | ''                                     | false            | false
    'ACET-Automation'         | ['UHGWM110-014781'] | false             | ''                                     | false            | false
    'ACET-Middleware'         | ['UHGWM110-014781'] | false             | ''                                     | false            | false
    'acet-salesforce'         | ['UHGWM110-014781'] | false             | ''                                     | false            | false
    'PACT-Middleware'         | ['UHGWM110-028410'] | false             | ''                                     | false            | false
    'pact-salesforce'         | ['UHGWM110-028410'] | false             | ''                                     | false            | false
  }
}
