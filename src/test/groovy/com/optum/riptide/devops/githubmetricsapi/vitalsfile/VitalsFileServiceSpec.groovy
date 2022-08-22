package com.optum.riptide.devops.githubmetricsapi.vitalsfile


import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest
@ActiveProfiles(['compiletime-tests'])
class VitalsFileServiceSpec extends Specification {
  @Autowired
  GitHub githubEnterprise
  @Autowired
  VitalsFileService vitalsFileService

  def "CreateMissingVitalsFilesInOrg"() {
    expect:
    vitalsFileService.createMissingVitalsFilesInOrg('riptide-deprecated-apps')
  }

  def "CreateMissingVitalsFileInRepo"() {

  }

  def "GetExistingVitalsFile"() {
  }
}
