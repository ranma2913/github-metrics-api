package com.optum.riptide.devops.githubmetricsapi.vitalsfile

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.networknt.schema.CustomErrorMessageType
import com.networknt.schema.ValidationMessage
import com.optum.riptide.devops.githubmetricsapi.branch.protection.BranchProtectionService
import com.optum.riptide.devops.githubmetricsapi.compliance.ComplianceFileService
import com.optum.riptide.devops.githubmetricsapi.maven.PomParserService
import com.optum.riptide.devops.githubmetricsapi.optumfile.OptumFileService
import com.optum.riptide.devops.githubmetricsapi.schema.SchemaValidator
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.StringUtils
import org.kohsuke.github.GHBranch
import org.kohsuke.github.GHBranchProtection
import org.kohsuke.github.GHContent
import org.kohsuke.github.GHContentUpdateResponse
import org.kohsuke.github.GHFileNotFoundException
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.util.StreamUtils
import reactor.core.publisher.Flux

import java.text.MessageFormat

import static com.optum.riptide.devops.githubmetricsapi.Constants.POM_FILE
import static com.optum.riptide.devops.githubmetricsapi.Constants.VITALS_FILE
import static java.nio.charset.StandardCharsets.UTF_8

@Slf4j
@Service
class VitalsFileService {
  String localVitalsFileSchema
  String remoteVitalsFileSchema
  final GitHub github
  final PomParserService pomParserService
  final ComplianceFileService complianceFileService
  final OptumFileService optumFileService
  final BranchProtectionService branchProtectionService
  final SchemaValidator schemaValidator


  @Autowired
  VitalsFileService(
      GitHub github,
      PomParserService pomParserService,
      ComplianceFileService complianceFileService,
      OptumFileService optumFileService,
      BranchProtectionService branchProtectionService,
      SchemaValidator schemaValidator,
      @Value('${uhg.vitals-file.schema}') String vitalsFileSchemaUrl,
      @Value('${classpath:vitals_yaml_schema.json}') ClassPathResource localSchemaJson) {
    this.github = github
    this.pomParserService = pomParserService
    this.complianceFileService = complianceFileService
    this.optumFileService = optumFileService
    this.branchProtectionService = branchProtectionService
    this.schemaValidator = schemaValidator

    try (BufferedInputStream inputStream = new BufferedInputStream(new URL(vitalsFileSchemaUrl).openStream())) {
      this.remoteVitalsFileSchema = StreamUtils.copyToString(inputStream, UTF_8)
    } catch (IOException e) {
      log.error('Unable to download vitals file schema from {}', vitalsFileSchemaUrl, e)
    }
    this.localVitalsFileSchema = StreamUtils.copyToString(localSchemaJson.getInputStream(), UTF_8)
  }

  Flux<GHRepository> createMissingVitalsFilesInOrg(String org) throws IOException {
    List<GHRepository> updatedRepositories
    List<GHRepository> repositories = github.getOrganization(org).listRepositories(100).toList()

    updatedRepositories =
        repositories.stream()
            .map(
                repo -> {
                  try {
                    return createMissingVitalsFileInRepo(repo, false, true)
                  } catch (IOException e) {
                    throw new RuntimeException(e)
                  }
                })
            .toList()

    return Flux.fromIterable(updatedRepositories)
  }

  Flux<GHRepository> createMissingVitalsFilesInOrg(String org, boolean enablePoc)
      throws IOException {
    return this.createMissingVitalsFilesInOrg(org, enablePoc, false)
  }

  Flux<GHRepository> createMissingVitalsFilesInOrg(
      String org, boolean enablePoc, boolean overrideBranchProtection) throws IOException {

    List<GHRepository> updatedRepositories
    List<GHRepository> repositories = github.getOrganization(org).listRepositories(100).toList()

    updatedRepositories =
        repositories.stream()
            .map(
                repo -> {
                  try {
                    return createMissingVitalsFileInRepo(repo, enablePoc, overrideBranchProtection)
                  } catch (IOException e) {
                    throw new RuntimeException(e)
                  }
                })
            .toList()

    return Flux.fromIterable(updatedRepositories)
  }

  /** create a vitals file if it's missing. Return null if not updated. */
  GHRepository createMissingVitalsFileInRepo(
      GHRepository repo, boolean enablePoc, boolean overrideBranchProtection) throws IOException {
    Optional<GHContent> vitalsFileOptional = this.getExistingVitalsFile(repo)
    GHRepository updatedRepo = null
    if (vitalsFileOptional.present) {
      log.debug('vitals.yaml exists in repo {}', repo.htmlUrl)
      updatedRepo = repo
    } else {
      log.warn('vitals.yaml not found in repo {}', repo.htmlUrl)
      if (repo.archived) {
        log.error(
            'Repository {} is archived. Please go to the GitHub Web UI to unarchive: {}',
            repo.fullName,
            repo.htmlUrl + '/settings')
      } else {
        GHBranchProtection origProtection = null
        if (overrideBranchProtection) {
          GHBranch branch = repo.getBranch(repo.defaultBranch)
          if (branch.protected) {
            origProtection = branch.protection
            branch.disableProtection()
          }
        }
        VitalsFile vitalsFile = new VitalsFile()
        vitalsFile
            .metadata.projectFriendlyName = pomParserService.readProjectFriendlyName(repo, POM_FILE)
        vitalsFile.metadata.projectKey = pomParserService.readProjectKey(repo, POM_FILE)

        // 1. check compliance.yaml
        var caAgileId = complianceFileService.readCaAgileId(repo)
        // 2. check Optumfile.yml
        caAgileId =
            StringUtils.isNotBlank(caAgileId) ? caAgileId : optumFileService.readCaAgileId(repo)
        // 3. use 'poc' if not found
        if (enablePoc) {
          caAgileId = StringUtils.isNotBlank(caAgileId) ? caAgileId : 'poc'
        }

        // 4. Upload new vitals file if fully populated.
        if (StringUtils.isNotBlank(caAgileId)) {
          vitalsFile.metadata.caAgileId = caAgileId
          // Create an ObjectMapper mapper for YAML
          ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
          String vitalsFileString = mapper.writeValueAsString(vitalsFile)
          GHContentUpdateResponse response =
              repo.createContent()
                  .path(VITALS_FILE)
                  .content(vitalsFileString)
                  .message('Create ' + VITALS_FILE)
                  .commit()

          log.info(
              '{} file successfully added with commit {}',
              VITALS_FILE,
              response.commit.sha)
          updatedRepo = repo
        } else {
          log.error(
              'Unable to read caAgileId from compliance.yaml or Optumfile.yml in repo {}',
              repo.htmlUrl)
        }

        // Put back protection if any.
        if (null != origProtection) {
          branchProtectionService.protectBranch(
              repo.getBranch(repo.defaultBranch), origProtection)
        }
      }
    }
    return updatedRepo
  }

  Optional<GHContent> updateExistingVitalsFile(GHRepository repo, VitalsFile newVitalsFile) throws IOException {
    Optional<GHContent> vitalsFileOptional = this.getExistingVitalsFile(repo)
    if (vitalsFileOptional.present) {
      GHContentUpdateResponse updateResponse = vitalsFileOptional.get()
          .update(newVitalsFile.toString(), 'Updating vitals.yaml content.')
      log.info('Updated vitals.yaml in commit = {}', updateResponse.commit.htmlUrl)
    }
    return vitalsFileOptional
  }

  Optional<GHContent> getExistingVitalsFile(GHRepository repo) throws IOException {
    Optional<GHContent> content
    try {
      List<GHContent> contentList =
          repo.getDirectoryContent('/').stream()
              .filter(ghContent -> ghContent.name == VITALS_FILE)
              .toList()
      content = contentList.stream().findFirst()
    } catch (GHFileNotFoundException e) {
      log.warn('Unable to find vitals.yaml in repository {}', repo.fullName, e)
      content = Optional.empty()
    }
    return content
  }

  Set<ValidationMessage> validateVitalsFile(GHContent vitalsFileContent) throws IOException {
    String existingFile = vitalsFileContent.content
    Set<ValidationMessage> validationMessages = new LinkedHashSet<>()
    try {
      validationMessages = schemaValidator.validateYamlText(localVitalsFileSchema, existingFile, null)
    } catch (Exception e) {
      log.error('Unable to validate {}', vitalsFileContent.getHtmlUrl(), e)
      def customValidationMessage = ValidationMessage.of('Parse error',
          new CustomErrorMessageType('FATAL',
              new MessageFormat("Unable to parse ${vitalsFileContent.getHtmlUrl()}, cause = ${e.getLocalizedMessage()}")),
          "${e.getClass()}", vitalsFileContent.getHtmlUrl(), e.getLocalizedMessage())
      log.debug('Custom ValidationMessage = {}', customValidationMessage)
      validationMessages.add(customValidationMessage)
    }
    return validationMessages
  }
}
