package com.optum.riptide.devops.githubmetricsapi.compliance

import com.optum.riptide.devops.githubmetricsapi.Constants
import com.optum.riptide.devops.githubmetricsapi.content.ContentHelper
import groovy.util.logging.Slf4j
import groovy.yaml.YamlSlurper
import org.kohsuke.github.GHContent
import org.kohsuke.github.GHRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Slf4j
@Service
class ComplianceFileService {
  YamlSlurper yamlSlurper
  ContentHelper contentHelper

  @Autowired
  ComplianceFileService(ContentHelper contentHelper) {
    this.contentHelper = contentHelper
    this.yamlSlurper = new YamlSlurper()
  }

  String readCaAgileId(GHRepository repo) {
    Optional<GHContent> complianceFileOptional = contentHelper.getFileContent(repo, "/", "compliance.yaml")
    String caAgileId = Constants.EMPTY
    if (complianceFileOptional.isPresent()) {
      caAgileId = ((HashMap) (yamlSlurper.parse(complianceFileOptional.get().read())))
          .getOrDefault('rallyWorkspaceId', 'poc')
    } else {
      log.error("Unable to read /compliance.yaml from repo {}", repo.getFullName())
    }
    return caAgileId
  }
}
