package com.ranma2913.riptide.devops.githubmetricsapi;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
public class RestTemplateConfiguration {
  @Autowired
  @Bean(name = {"restTemplate", "sslRestTemplate"})
  public RestTemplate sslRestTemplate(
      @Qualifier("selfSignedSslCloseableHttpClient") CloseableHttpClient httpClient) {
    RestTemplate restTemplate;
    HttpComponentsClientHttpRequestFactory requestFactory =
        new HttpComponentsClientHttpRequestFactory();
    requestFactory.setHttpClient(httpClient);
    restTemplate = new RestTemplate(requestFactory);
    return restTemplate;
  }
}
