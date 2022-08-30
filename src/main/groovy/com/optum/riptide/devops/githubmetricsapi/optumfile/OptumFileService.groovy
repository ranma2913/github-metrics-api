package com.optum.riptide.devops.githubmetricsapi.optumfile

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
class OptumFileService {
  YamlSlurper yamlSlurper
  ContentHelper contentHelper

  @Autowired
  OptumFileService(ContentHelper contentHelper) {
    this.contentHelper = contentHelper
    yamlSlurper = new YamlSlurper()
  }

  Optional<Map> readOptumFile(GHRepository repo) {
    Optional<GHContent> optumFileOptional = contentHelper.getFileContent(repo, "/", "Optumfile.yml")
    if (optumFileOptional.isPresent()) {
      return Optional.ofNullable(yamlSlurper.parse(optumFileOptional.get().read()) as Map)
    } else {
      log.error("Unable to read /Optumfile.yml from repo {}", repo.getHtmlUrl())
      return Optional.empty()
    }
  }

  String readCaAgileId(GHRepository repo) {
    String caAgileId = Constants.EMPTY
    Optional<Map> optumFileMapOptional = this.readOptumFile(repo)
    if (optumFileMapOptional.isPresent()) {
      caAgileId = optumFileMapOptional.get().metadata.caAgileId
    } else {
      log.error("Unable to read caAgileId from /Optumfile.yml")
    }
    return caAgileId
  }
}
