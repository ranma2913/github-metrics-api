package com.ranma2913.riptide.devops.githubmetricsapi;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLContext;

@Configuration
public class ReactiveWebClientConfiguration {
  @Bean
  public SslContext reactiveSslContext(@Qualifier("selfSignedSslContext") SSLContext sslContext) {
    return new JdkSslContext(sslContext, true, ClientAuth.NONE);
  }

  @Bean
  HttpClient reactiveHttpClient(SslContext reactiveSslContext) {
    return HttpClient.create().secure(sslSpec -> sslSpec.sslContext(reactiveSslContext));
  }

  @Bean
  public WebClient.Builder reactiveWebClientBuilder(HttpClient reactorNettyHttpClient) {
    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(reactorNettyHttpClient));
  }

  @Bean(name = {"webClient", "reactiveWebClient"})
  public WebClient reactiveWebClient(WebClient.Builder optumStandardTrustsSslWebClientBuilder) {
    return optumStandardTrustsSslWebClientBuilder.build();
  }
}
