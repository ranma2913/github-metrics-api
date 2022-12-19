package com.ranma2913.riptide.devops.githubmetricsapi.maven

import org.owasp.dependencycheck.xml.pom.PomParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PomParserConfig {
  Logger log = LoggerFactory.getLogger(this.getClass())

  @Bean
  PomParser pomParser() {
    return new PomParser()
  }
}
