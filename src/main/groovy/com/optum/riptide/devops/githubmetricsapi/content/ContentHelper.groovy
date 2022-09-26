package com.optum.riptide.devops.githubmetricsapi.content

import groovy.util.logging.Slf4j
import org.kohsuke.github.GHContent
import org.kohsuke.github.GHRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Slf4j
@Service
class ContentHelper {

  @Cacheable(value = "ghContent", key = "#repo.fullName+#directory+#fileName", sync = true)
  Optional<GHContent> getFileContent(GHRepository repo, String directory, String fileName) throws IOException {
    List<GHContent> contentList =
        repo.getDirectoryContent(directory).parallelStream()
            .filter(ghContent -> ghContent.getName() == fileName)
            .toList()
    return contentList.stream().findFirst()
  }
}
