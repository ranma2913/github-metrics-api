package com.optum.riptide.devops.githubmetricsapi.vitalsfile

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

  @Unroll("UpdateVitalsFilesInOrg = #orgName")
  def "UpdateVitalsFilesInOrg"() {
    given:
    List<GHRepository> repositories = githubEnterprise.getOrganization(orgName).listRepositories(100).toList();
    List<GHRepository> updatedRepositories = new LinkedList<GHRepository>()

    repositories.stream()
        .map(
            repo -> {
              try {
                Optional<GHContent> vitalsFileContent = vitalsFileService.getExistingVitalsFile(repo)
                VitalsFile origVitalsFile
                VitalsFile vitalsFile
                if (vitalsFileContent.isPresent()) {
                  origVitalsFile = new VitalsFile(yamlSlurper.parse(vitalsFileContent.get().read()) as Map)
                  vitalsFile = origVitalsFile.clone()

                  if ('poc' == vitalsFile.metadata.caAgileId) {
                    // Check history of vitals file for caAgileId
                    List<GHCommit> vitalsCommits = repo.queryCommits().pageSize(50).path('vitals.yaml').list().toList()
                    for (GHCommit commit in vitalsCommits) {
                      def commitSha = commit.SHA1
                      def historicVitals = yamlSlurper.parse(repo.getFileContent('vitals.yaml', commitSha).read())
                      if (historicVitals.metadata.caAgileId != 'poc') {
                        vitalsFile.metadata.caAgileId = historicVitals.metadata.caAgileId
                        break // exit from commit loop
                      }
                    }
                  }
                } else {
                  vitalsFile = new VitalsFile()
                }

                /**
                 * Hard Code Values in Vitals.yaml
                 */
                vitalsFile.apiVersion = 'v1'
                vitalsFile.metadata.setAskId(askId)
                vitalsFile.metadata.projectKey = pomParserService.readProjectKey(repo, '/pom.xml')
                vitalsFile.metadata.projectFriendlyName = pomParserService.readProjectFriendlyName(repo, '/pom.xml')

                /**
                 * if not equals to original version then do an update. Otherwise proceed.
                 */
                if (!vitalsFile.equals(origVitalsFile)) {
                  Optional<GHContent> updateResponse = vitalsFileService.updateExistingVitalsFile(repo, vitalsFile)
                  cerberusScanService.cerberusScan(repo)
                  updatedRepositories.add(updateResponse.get().getOwner())
                }
              }
              catch (IOException e) {
                log.error("Unable to Update Vitals File in repo = {}", repo.getHtmlUrl(), e)
                throw new RuntimeException(e)
              }
            }
        ).toList()

    expect: 'All repositories should be updated.'
    updatedRepositories.size() == repositories.size()

    where:
    orgName        | askId               | caAgileId
//    'riptide-deprecated-apps' | ['~']               | 'do not use rally or use other system'
//    'riptide-devops'          | ['UHGWM110-017197'] | ''
//    'riptide-poc'             | ['~']               | 'do not use rally or use other system'
    'riptide-team' | ['UHGWM110-017197'] | ''
//    'riptide-team-microsite'  | ['UHGWM110-017197'] | ''
//    'iset'                    | ['']                | ''
//    'UHC-Motion'              | ['UHGWM110-017808'] | ''
//    'DigitalMemberships'      | ['UHGWM110-027031'] | ''
//    'ACET-Automation'         | ['UHGWM110-014781'] | ''
//    'ACET-Middleware'         | ['UHGWM110-014781'] | ''
//    'acet-salesforce'         | ['UHGWM110-014781'] | ''
//    'PACT-Middleware'         | ['UHGWM110-028410'] | ''
//    'pact-salesforce'         | ['UHGWM110-028410'] | ''
  }
}
