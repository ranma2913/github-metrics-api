package com.optum.riptide.devops.githubmetricsapi.vitals

import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class VitalsFileService {
  @Autowired
  GitHub github

}
