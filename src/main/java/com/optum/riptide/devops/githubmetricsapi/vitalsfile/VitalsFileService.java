package com.optum.riptide.devops.githubmetricsapi.vitalsfile;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class VitalsFileService {
  static final String VITALS_FILE = "vitals.yaml";
  final GitHub github;

  @Autowired
  public VitalsFileService(GitHub github) {
    this.github = github;
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
    Optional<GHContent> vitalsFile = this.getExistingVitalsFile(repo);
    if (vitalsFile.isPresent()) {
      log.info("vitals.yaml exists in repo {}", repo.getFullName());
    } else {
      log.warn("vitals.yaml not found in repo {}", repo.getFullName());
      // todo write vitals file
    }
    return repo;
  }

  public Optional<GHContent> getExistingVitalsFile(GHRepository repo) throws IOException {
    List<GHContent> contentList =
        repo.getDirectoryContent("/").stream()
            .filter(ghContent -> ghContent.getName().equals(VITALS_FILE))
            .toList();
    return contentList.stream().findFirst();
  }
}
