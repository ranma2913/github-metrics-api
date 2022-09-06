package com.optum.riptide.devops.githubmetricsapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = {
      "com.optum.riptide.http.restclients",
      "com.optum.riptide.devops.githubmetricsapi"
    })
public class GithubMetricsApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(GithubMetricsApiApplication.class, args);
  }

}
