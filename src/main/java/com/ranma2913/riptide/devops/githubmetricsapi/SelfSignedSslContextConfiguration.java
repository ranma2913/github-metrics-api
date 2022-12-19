package com.ranma2913.riptide.devops.githubmetricsapi;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Slf4j
@Configuration
public class SelfSignedSslContextConfiguration {
  @Bean
  @Qualifier("selfSignedSslContext")
  public SSLContext selfSignedSslContext() {
    SSLContext sslContext = null;
    try {
      sslContext =
          new SSLContextBuilder().loadTrustMaterial(null, (certificate, authType) -> true).build();
    } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
      log.error("Error getting the SSLContext", e);
    }
    return sslContext;
  }

  @Bean
  @Qualifier("selfSignedX509SslContext")
  public SSLContext selfSignedX509SslContext()
      throws NoSuchAlgorithmException, KeyManagementException {
    final TrustManager[] trustAllCerts =
        new TrustManager[] {
          new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
              return null;
            }

            public void checkClientTrusted(
                final java.security.cert.X509Certificate[] arg0, final String arg1)
                throws CertificateException {
              // do nothing and blindly accept the certificate
            }

            public void checkServerTrusted(
                final java.security.cert.X509Certificate[] arg0, final String arg1)
                throws CertificateException {
              // do nothing and blindly accept the server
            }
          }
        };

    final SSLContext sslcontext = SSLContext.getInstance("SSL");
    sslcontext.init(null, trustAllCerts, new java.security.SecureRandom());
    return sslcontext;
  }
}
