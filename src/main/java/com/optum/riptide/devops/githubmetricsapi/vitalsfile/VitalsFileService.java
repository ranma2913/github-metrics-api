package com.optum.riptide.devops.githubmetricsapi.vitalsfile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.optum.riptide.devops.githubmetricsapi.compliance.ComplianceFileService;
import com.optum.riptide.devops.githubmetricsapi.maven.PomParserService;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHContentUpdateResponse;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.optum.riptide.devops.githubmetricsapi.Constants.POM_FILE;
import static com.optum.riptide.devops.githubmetricsapi.Constants.VITALS_FILE;

@Slf4j
@Service
public class VitalsFileService {
  final GitHub github;
  final PomParserService pomParserService;
  final ComplianceFileService complianceFileService;

  @Autowired
  public VitalsFileService(
      GitHub github,
      PomParserService pomParserService,
      ComplianceFileService complianceFileService) {
    this.github = github;
    this.pomParserService = pomParserService;
    this.complianceFileService = complianceFileService;
  }

  public Flux<GHRepository> createMissingVitalsFilesInOrg(String org) throws IOException {
    List<GHRepository> updatedRepositories;
    List<GHRepository> repositories = github.getOrganization(org).listRepositories(100).toList();

    updatedRepositories =
        repositories.stream()
            .map(
                repo -> {
                  try {
                    return createMissingVitalsFileInRepo(repo);
                  } catch (IOException e) {
                    throw new RuntimeException(e);
                  }
                })
            .toList();

    return Flux.fromIterable(updatedRepositories);
  }

  /** create a vitals file if it's missing. Return null if not updated. */
  public GHRepository createMissingVitalsFileInRepo(GHRepository repo) throws IOException {
    Optional<GHContent> vitalsFileOptional = this.getExistingVitalsFile(repo);
    if (vitalsFileOptional.isPresent()) {
      log.info("vitals.yaml exists in repo {}", repo.getFullName());
    } else {
      log.warn("vitals.yaml not found in repo {}", repo.getFullName());
      if (repo.isArchived()) {
        log.error(
            "Repository {} is archived. Please go to the GitHub Web UI to unarchive: {}",
            repo.getFullName(),
            repo.getHtmlUrl() + "/settings");
      } else {
        //        GHBranch branch = repo.getBranch(repo.getDefaultBranch());
        //        if (branch.isProtected()) {
        //          branch.disableProtection();
        //        }

        VitalsFile vitalsFile = new VitalsFile();
        vitalsFile
            .getMetadata()
            .setProjectFriendlyName(pomParserService.readProjectFriendlyName(repo, POM_FILE));
        vitalsFile.getMetadata().setProjectKey(pomParserService.readProjectKey(repo, POM_FILE));
        vitalsFile.getMetadata().setCaAgileId(complianceFileService.readCaAgileId(repo));

        // Create an ObjectMapper mapper for YAML
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String vitalsFileString = mapper.writeValueAsString(vitalsFile);
        GHContentUpdateResponse response =
            repo.createContent()
                .path(VITALS_FILE)
                .content(vitalsFileString)
                .message("Create " + VITALS_FILE)
                .commit();

        log.info(
            "{} file sucessfully added with commit {}", VITALS_FILE, response.getCommit().getSha());
      }
    }
    return repo;
  }

  public Optional<GHContent> getExistingVitalsFile(GHRepository repo) throws IOException {
    Optional<GHContent> content;
    try {
      List<GHContent> contentList =
          repo.getDirectoryContent("/").stream()
              .filter(ghContent -> ghContent.getName().equals(VITALS_FILE))
              .toList();
      content = contentList.stream().findFirst();
    } catch (GHFileNotFoundException e) {
      log.warn("Unable to find vitals.yaml in repository {}", repo.getFullName(), e);
      content = Optional.empty();
    }
    return content;
  }
}
