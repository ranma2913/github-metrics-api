package com.optum.riptide.devops.githubmetricsapi.cerberus

import com.optum.riptide.http.restclients.resttemplate.RestTemplateFactory
import groovy.util.logging.Slf4j
import org.kohsuke.github.GHRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

import java.time.Duration

@Slf4j
@Service
class CerberusScanService {
  @Autowired
  WebClient client
  @Autowired
  RestTemplateFactory restTemplateFactory
  @Value('${uhg.cerberus.api}')
  String uhgCerberusApi

  /**
   * Trigger cerberus-scan
   */
  def asyncCerberusScan(GHRepository repo) {
    // repo.getFullName() isn't always reliable so manually templating name:
    def repoFullName = "${repo.getOwner()}/${repo.getName()}"
    def scanUrl = "$uhgCerberusApi/$repoFullName"
    WebClient.UriSpec<WebClient.RequestBodySpec> uriSpec = client.get()
    WebClient.RequestBodySpec bodySpec = uriSpec.uri(scanUrl)
    bodySpec.exchangeToMono(response -> {
      if (response.statusCode() == HttpStatus.OK) {
        String responseString = response.bodyToMono(String.class).block(Duration.ofSeconds(30))
        log.info("Cerberus-Scan Response from {} = \n{}", scanUrl, responseString)
      } else if (response.statusCode().is4xxClientError()) {
        String responseString = Mono.just("Error response = ${response.statusCode()}").block(Duration.ofSeconds(30))
        log.info("Cerberus-Scan Response from {} = \n{}", scanUrl, responseString)
      } else {
        log.error("Cerberus-Scan ERROR. Unable to get response from {}", scanUrl)
      }
    })
  }

  def cerberusScan(GHRepository repo) {
    // repo.getFullName() isn't always reliable so manually templating name:
    def repoFullName = "${repo.getOwner()}/${repo.getName()}"
    def scanUrl = "$uhgCerberusApi/$repoFullName"
    def responseString = restTemplateFactory.getRestTemplate(scanUrl).getForObject(new URI(scanUrl), String.class)
    log.info("Cerberus-Scan Response from {} = \n{}", scanUrl, responseString)
    return responseString
  }
}
