package com.ranma2913.riptide.devops.githubmetricsapi;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

@Slf4j
@Configuration
public class CloseableHttpClientConfiguration {

  @Bean
  @Qualifier("defaultCloseableHttpClient")
  public CloseableHttpClient defaultCloseableHttpClient() {
    return HttpClients.createDefault();
  }

  @Bean
  @Qualifier("selfSignedSslCloseableHttpClient")
  public CloseableHttpClient selfSignedSslCloseableHttpClient(
      @Autowired @Qualifier("selfSignedSslContext") SSLContext sslContext) {

    return HttpClients.custom()
        .setSSLContext(sslContext)
        .setDefaultRequestConfig(null)
        .setSSLHostnameVerifier(new NoopHostnameVerifier())
        .build();
  }
}
