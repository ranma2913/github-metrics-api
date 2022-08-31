package com.optum.riptide.devops.githubmetricsapi.vitalsfile


import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
@ActiveProfiles(['compiletime-tests'])
class CreateMissingVitalsFilesInOrg extends Specification {
  @Autowired
  GitHub githubEnterprise
  @Autowired
  VitalsFileService vitalsFileService

  @Unroll("createMissingVitalsFilesInOrg = #orgName")
  def "createMissingVitalsFilesInOrg"() {
    expect:
    vitalsFileService.createMissingVitalsFilesInOrg(orgName, enablePoc)

    where:
    no | orgName       | enablePoc
//    1  | 'riptide-deprecated-apps' | true
//    2  | 'riptide-devops'          | false
    3  | 'riptide-poc' | true
//    4  | 'riptide-team'            | false
  }
}
