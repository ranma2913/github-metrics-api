package com.optum.riptide.devops.githubmetricsapi.codeQL

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.networknt.schema.CustomErrorMessageType
import com.networknt.schema.ValidationMessage
import com.optum.riptide.devops.githubmetricsapi.branch.protection.BranchProtectionService
import com.optum.riptide.devops.githubmetricsapi.cerberus.CerberusScanService
import com.optum.riptide.devops.githubmetricsapi.compliance.ComplianceFileService
import com.optum.riptide.devops.githubmetricsapi.content.ContentHelper
import com.optum.riptide.devops.githubmetricsapi.maven.PomParserService
import com.optum.riptide.devops.githubmetricsapi.optumfile.OptumFileService
import com.optum.riptide.devops.githubmetricsapi.schema.SchemaValidator
import groovy.util.logging.Slf4j
import groovy.yaml.YamlSlurper
import org.apache.commons.lang3.StringUtils
import org.kohsuke.github.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.CacheEvict
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.util.StreamUtils
import reactor.core.publisher.Flux

import java.text.MessageFormat
import java.util.concurrent.CompletableFuture

import static com.optum.riptide.devops.githubmetricsapi.Constants.POM_FILE
import static com.optum.riptide.devops.githubmetricsapi.Constants.VITALS_FILE
import static java.nio.charset.StandardCharsets.UTF_8

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
