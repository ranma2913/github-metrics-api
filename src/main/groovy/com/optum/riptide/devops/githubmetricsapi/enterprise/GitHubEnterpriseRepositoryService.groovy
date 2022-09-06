package com.optum.riptide.devops.githubmetricsapi.enterprise


import groovy.util.logging.Slf4j
import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Slf4j
@Service
class GitHubEnterpriseRepositoryService {
  @Value('${credentials_GIT_TOKEN}')
  String githubToken
  @Autowired
  GitHub githubEnterprise
  @Autowired
  RestTemplate sslRestTemplate

//  @PostConstruct
//  void init() {
//    List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
////Add the Jackson Message converter
//    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
//
//// Note: here we are making this converter to process any kind of response,
//// not only application/*json, which is the default behaviour
//    converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL))
//    messageConverters.add(converter);
//    sslRestTemplate.setMessageConverters(messageConverters);
//  }

  /**
   *
   * @param githubEnterpriseOrg
   * @return
   * @throws IOException
   */
  List<GHRepository> listRepositories(GHOrganization githubEnterpriseOrg) throws IOException {
    // pageSize – size for each page of items returned by GitHub. Maximum page size is 100.
    List<GHRepository> repositories = githubEnterpriseOrg.listRepositories(100).toList()
    return repositories
  }

  /**
   *
   * @param repo
   * @param newOwnerName
   * @return
   */
  GHRepository transferRepository(GHRepository repo, String newOwnerName) {
    GHRepository transferredRepo = null
    GHRepository.Visibility origVisibility = repo.getVisibility()
    repo.setVisibility(GHRepository.Visibility.PUBLIC)
    if (repo.isFork()) {
      repo.forkTo(githubEnterprise.getOrganization(newOwnerName))
      repo.delete()
    } else if (repo.getOwnerName() != newOwnerName) {
      HttpHeaders headers = new HttpHeaders()
      headers.set('Accept', 'application/vnd.github+json')
      headers.set('Content-Type', 'application/json')
      headers.set('Authorization', "Bearer $githubToken")
      HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(["new_owner": newOwnerName], headers)

      try {
        ResponseEntity<Map<String, Object>> responseEntity = sslRestTemplate.postForEntity(
            "${githubEnterprise.getApiUrl()}/repos/{owner}/{repo}/transfer",
            requestEntity,
            LinkedHashMap.class,
            [owner: repo.getOwnerName(), repo: repo.getName()]
        )
        log.debug("responseEntity.getStatusCode = {}", responseEntity.getStatusCode())
        transferredRepo = githubEnterprise.getRepository("${newOwnerName}/${repo.getName()}")
      } catch (HttpClientErrorException e) {
        log.error("Error {}", e.getMessage())
        if (HttpStatus.UNPROCESSABLE_ENTITY == e.getStatusCode()) {
          try {
            transferredRepo = githubEnterprise.getRepository("${newOwnerName}/${repo.getName()}")
          } catch (e2) {
            log.error("Error. Unable to get repository ${newOwnerName}/${repo.getName()}", e2)
          }
        }
      }
    } else {
      // repo.getOwnerName() & newOwnerName are the same.
      transferredRepo = repo
    }
    transferredRepo?.setVisibility(origVisibility)
    return transferredRepo
  }
}
