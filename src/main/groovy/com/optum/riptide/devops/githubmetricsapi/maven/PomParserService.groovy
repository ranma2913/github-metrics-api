package com.optum.riptide.devops.githubmetricsapi.maven

import com.optum.riptide.devops.githubmetricsapi.Constants
import com.optum.riptide.devops.githubmetricsapi.content.ContentHelper
import org.kohsuke.github.GHContent
import org.kohsuke.github.GHRepository
import org.owasp.dependencycheck.xml.pom.Model
import org.owasp.dependencycheck.xml.pom.PomParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PomParserService {
  Logger log = LoggerFactory.getLogger(this.getClass())

  @Autowired
  PomParser pomParser
  @Autowired
  ContentHelper contentHelper

  String readProjectKey(GHRepository repo, String pomFilePath) {
    Optional<GHContent> pomFileOptional = this.getPomFileContent(repo, pomFilePath)
    Model pomModel
    String projectKey = Constants.EMPTY
    if (pomFileOptional.isPresent()) {
      pomModel = pomParser.parse(pomFileOptional.get().read())
      projectKey = pomModel.getGroupId().concat(":").concat(pomModel.getArtifactId()).replace('-parent', '')
    } else {
      log.error("Unable to read /pom.xml from repo {}", repo.getFullName())
    }
    return projectKey
  }

  String readProjectFriendlyName(GHRepository repo, String pomFilePath) {
    Optional<GHContent> pomFileOptional = this.getPomFileContent(repo, pomFilePath)
    Model pomModel
    String projectKey = Constants.EMPTY
    if (pomFileOptional.isPresent()) {
      pomModel = pomParser.parse(pomFileOptional.get().read())
      projectKey = pomModel.getArtifactId().replace('-parent', '')
    } else {
      log.error("Unable to read /pom.xml from repo {}", repo.getFullName())
    }
    return projectKey
  }

  Optional<GHContent> getPomFileContent(GHRepository repo, String pomFilePath) throws IOException {
    return contentHelper.getFileContent(repo, "/", pomFilePath)
  }
}
