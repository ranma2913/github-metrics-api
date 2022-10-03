package com.optum.riptide.devops.githubmetricsapi.maven

import com.optum.riptide.devops.githubmetricsapi.Constants
import com.optum.riptide.devops.githubmetricsapi.content.ContentHelper
import groovy.util.logging.Slf4j
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.NodeChild
import org.kohsuke.github.GHContent
import org.kohsuke.github.GHRepository
import org.owasp.dependencycheck.xml.pom.Model
import org.owasp.dependencycheck.xml.pom.PomParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Slf4j
@Service
class PomParserService {
  @Autowired
  PomParser pomParser
  @Autowired
  ContentHelper contentHelper

  XmlSlurper xmlSlurper = new XmlSlurper()

  String readProjectKey(GHRepository repo, String pomFilePath) {
    Optional<GHContent> pomContentOptional = this.getPomFileContent(repo, pomFilePath)
    String projectKey = Constants.EMPTY
    if (pomContentOptional.isPresent()) {
      Model pomModel = pomParser.parse(pomContentOptional.get().read())

      if (pomModel.getArtifactId().contains('-parent')) {
        pomContentOptional = this.getServicePomContent(repo, pomContentOptional)
        if (pomContentOptional.isPresent()) {
          def servicePomContent = pomContentOptional.get()
          projectKey = this.readProjectKey(repo, servicePomContent.getPath())
        }
      } else {
        def groupId = pomModel.groupId?.trim() ?: pomModel.parentGroupId.trim()
        def artifactId = pomModel.artifactId?.trim() ?: pomModel.parentArtifactId.trim().replace('-parent', '')
        projectKey = "$groupId:$artifactId"
      }
    } else {
      log.error("Unable to read {} from repo {}", pomFilePath, repo.getFullName())
    }
    return projectKey
  }

  String readProjectFriendlyName(GHRepository repo, String pomFilePath) {
    Optional<GHContent> pomContentOptional = this.getPomFileContent(repo, pomFilePath)
    String projectFriendlyName = Constants.EMPTY
    if (pomContentOptional.isPresent()) {
      Model pomModel = pomParser.parse(pomContentOptional.get().read())

      if (pomModel.getArtifactId().contains('-parent')) {
        pomContentOptional = this.getServicePomContent(pomContentOptional.get())
        if (pomContentOptional.isPresent()) {
          def servicePomContent = pomContentOptional.get()
          projectFriendlyName = this.readProjectFriendlyName(repo, servicePomContent.getPath())
        }
      } else {
        def artifactId = pomModel.artifactId?.trim() ?: pomModel.parentArtifactId.trim().replace('-parent', '')
        projectFriendlyName = "$artifactId"
      }
    } else {
      log.error("Unable to read {} from repo {}", pomFilePath, repo.getFullName())
    }
    return projectFriendlyName
  }

  Optional<GHContent> getServicePomContent(GHContent pomContent) {
    Optional<GHContent> servicePomContent = Optional.empty()
    def project = xmlSlurper.parse(pomContent.read())
    // lookup service pom.xml
    for (module in project.modules.module) {
      if (!module.text().contains('-atdd')) {
        def servicePomPath = "/${module.text()}/pom.xml"
        def contentOptional = this.getPomFileContent(pomContent.getOwner(), servicePomPath)
        if (contentOptional.isPresent()) {
          servicePomContent = contentOptional
        }
        break // for (module in project.modules.module)
      }
    }
    return servicePomContent
  }

  Optional<GHContent> getPomFileContent(GHRepository repo, String pomFilePath) {
    def pathList = pomFilePath.tokenize('/')
    pathList = pathList.take(pathList.size() - 1)
    def directory = pathList.join('/')
    return contentHelper.getFileContent(repo, "/$directory", 'pom.xml')
  }

  /**
   * Load a list of dependencies from root pom + service pom if found.
   * @param pomContent
   * @return
   */
  Optional<List<NodeChild>> readDependenciesRecursive(GHContent pomContent) {
    List<NodeChild> dependencies = new LinkedList<NodeChild>()
    Model pomModel = pomParser.parse(pomContent.read())

    // read root pom dependencies.
    def rootDependencies = this.readDependencies(pomContent)
    if (rootDependencies.isPresent()) {
      dependencies.addAll(rootDependencies.get())
    }

    // if parent pom, read service dependencies.
    if (pomModel.getArtifactId().contains('-parent')) {
      def pomContentOptional = this.getServicePomContent(pomContent)
      if (pomContentOptional.isPresent()) {
        Optional<List<NodeChild>> servicePomDependenciesOptional = this.readDependencies(pomContentOptional.get())
        if (servicePomDependenciesOptional.isPresent()) {
          dependencies.addAll(servicePomDependenciesOptional.get())
        }
      }
    }

    // if any dependencies were found return them, else empty Optional.
    if (!dependencies.isEmpty()) {
      return Optional.of(dependencies)
    } else {
      return Optional.empty()
    }
  }


  Optional<List<NodeChild>> readDependencies(GHContent pomContent) {
    def project = xmlSlurper.parse(pomContent.read())
    if (project.dependencies instanceof groovy.xml.slurpersupport.NodeChildren) {
      List<NodeChild> dependencies = new LinkedList<>()
      for (dependency in project.dependencies.dependency) {
        dependencies.add(dependency)
      }
      return Optional.of(dependencies)
    } else {
      return Optional.empty()
    }
  }
}
