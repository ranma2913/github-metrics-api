package com.optum.riptide.devops.githubmetricsapi.vitalsfile;

import org.kohsuke.github.GHRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
public class VitalsFileController {
  final VitalsFileService vitalsFileService;

  @Autowired
  public VitalsFileController(VitalsFileService vitalsFileService) {
    this.vitalsFileService = vitalsFileService;
  }

  @PostMapping(path = "/api/vitals/create-if-missing/org/{org}")
  public Flux<GHRepository> vitalsCreateIfMissing(@PathVariable String org) throws IOException {
    Flux<GHRepository> updatedRepositories = vitalsFileService.createMissingVitalsFilesInOrg(org);
    return updatedRepositories;
  }
}
