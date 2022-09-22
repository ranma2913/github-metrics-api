package com.optum.riptide.devops.githubmetricsapi.vitalsfile

import groovy.util.logging.Slf4j
import groovy.yaml.YamlSlurper
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

  YamlSlurper yamlSlurper = new YamlSlurper()

  @Unroll("createMissingVitalsFilesInOrg = #orgName")
  def "createMissingVitalsFilesInOrg"() {
    given:
    List<GHRepository> repositories = githubEnterprise.getOrganization(orgName).listRepositories(100).toList();
    List<GHRepository> updatedRepositories =
        repositories.parallelStream()
            .map(
                repo -> {
                  try {
                    Optional<GHContent> repoContent = vitalsFileService.getExistingVitalsFile(repo)
                    def vitalsFile
                    if (repoContent.isPresent()) {
                      vitalsFile = yamlSlurper.parse(repoContent.get().read()) as VitalsFile
                    } else {
                      vitalsFile = new VitalsFile()
                    }

                    vitalsFile.metadata.askId = '~'
                    vitalsFile.metadata.caAgileId = caAgileId
                    return vitalsFileService.updateExistingVitalsFile(repo, vitalsFile)
                  } catch (IOException e) {
                    log.error("Unable to Update Vitals File in repo = {}", repo.getHtmlUrl(), e)
                    throw new RuntimeException(e);
                  }
                })
            .toList()

    expect: 'All repositories should be updated.'
    updatedRepositories.size() == repositories.size()

    where:
    no | orgName                   | askId | caAgileId
    1  | 'riptide-deprecated-apps' | '~'   | 'do not use rally or use other system'
//    2  | 'riptide-devops'          | 'UHGWM110-017197' | 'poc'
//    3  | 'riptide-poc'             | '~'               | 'poc'
//    4  | 'riptide-team'            | 'UHGWM110-017197' | 'poc'
  }
}
