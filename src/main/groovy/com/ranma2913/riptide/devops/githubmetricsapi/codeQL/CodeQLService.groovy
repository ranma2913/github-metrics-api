package com.ranma2913.riptide.devops.githubmetricsapi.codeQL

import com.ranma2913.riptide.devops.githubmetricsapi.content.ContentHelper
import groovy.util.logging.Slf4j
import org.kohsuke.github.GHContent
import org.kohsuke.github.GHRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Slf4j
@Service
class CodeQLService {
  @Autowired
  ContentHelper contentHelper

  Boolean isCodeQLFileMerged(GHRepository repo) throws IOException {
      Optional<GHContent> complianceFileOptional = null;
      Boolean isCodeQLMerged
      try {
          complianceFileOptional = contentHelper.getFileContent(repo, '/.github/workflows/', "codeql-analysis.yml")
          if(complianceFileOptional.isPresent()){
              isCodeQLMerged = true;
          }
      }catch(FileNotFoundException e){
          log.error("code QL file not found exception")
          isCodeQLMerged = false
      }
    return isCodeQLMerged
  }
}
