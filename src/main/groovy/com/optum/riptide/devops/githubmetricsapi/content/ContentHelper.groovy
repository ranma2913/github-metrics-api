package com.optum.riptide.devops.githubmetricsapi.content

import org.kohsuke.github.GHContent
import org.kohsuke.github.GHRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ContentHelper {
  Logger log = LoggerFactory.getLogger(this.getClass())

  Optional<GHContent> getFileContent(GHRepository repo, String directory, String fileName) throws IOException {
    List<GHContent> contentList =
        repo.getDirectoryContent(directory).stream()
            .filter(ghContent -> ghContent.getName().equals(fileName))
            .toList()
    return contentList.stream().findFirst()
  }
}
