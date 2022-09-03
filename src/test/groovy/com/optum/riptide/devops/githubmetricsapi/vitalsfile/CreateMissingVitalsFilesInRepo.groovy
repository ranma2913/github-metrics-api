package com.optum.riptide.devops.githubmetricsapi.vitalsfile

import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
@ActiveProfiles(['compiletime-tests'])
class CreateMissingVitalsFilesInRepo extends Specification {
  @Autowired
  GitHub githubEnterprise
  @Autowired
  VitalsFileService vitalsFileService

  @Unroll("Create Missing Vitals File In Repo = #repoFullName")
  def "Create Missing Vitals File In Repo"() {
    given:
    GHRepository repo = githubEnterprise.getRepository(repoFullName)

    expect:
    repo
    vitalsFileService.createMissingVitalsFileInRepo(repo, enablePoc)

    where:
    repoFullName                                              | enablePoc
    'riptide-team/a4me-gateway'                               | true
    'riptide-team/acet-dashboard-v2'                          | true
    'riptide-team/acet-digital-contact-history-launcher'      | true
    'riptide-team/acip-client-list-processor'                 | true
    'riptide-team/acip-csv-batch-processor'                   | true
    'riptide-team/acip-csv-file-import'                       | true
    'riptide-team/acip-data-cache-processor'                  | true
    'riptide-team/acip-data-cache-starter'                    | true
    'riptide-team/acip-data-cache-status'                     | true
    'riptide-team/acip-data-cache-status-sink'                | true
    'riptide-team/acip-performance-testing'                   | true
    'riptide-team/acip-performance-utility-app'               | true
    'riptide-team/acip-ui-api-testing'                        | true
    'riptide-team/api-client-alerts'                          | true
    'riptide-team/api-client-consumer-eligibility'            | true
    'riptide-team/api-client-faro-services'                   | true
    'riptide-team/api-client-member-messages'                 | true
    'riptide-team/api-client-obapi-services'                  | true
    'riptide-team/api-client-opportunity'                     | true
    'riptide-team/api-client-read-consumer-eligibility'       | true
    'riptide-team/appeals-specialty'                          | true
    'riptide-team/caml-widget-iset'                           | true
    'riptide-team/caml-widget-yoda'                           | true
    'riptide-team/cdb-locked-accounts'                        | true
    'riptide-team/certs-riptide'                              | true
    'riptide-team/chs-eligibility-api'                        | true
    'riptide-team/claim-search-filter'                        | true
    'riptide-team/client-report'                              | true
    'riptide-team/client-report-db-engine'                    | true
    'riptide-team/client-report-email-service'                | true
    'riptide-team/client-report-mysql-db-connect'             | true
    'riptide-team/client-report-status-messenger'             | true
    'riptide-team/client-token-manager'                       | true
    'riptide-team/cns-mailbox'                                | true
    'riptide-team/cns-sm-api'                                 | true
    'riptide-team/commitmentverbiage-widget'                  | true
    'riptide-team/consent-forms-widget'                       | true
    'riptide-team/covid-19-vaccine-api'                       | true
    'riptide-team/covid-19-vaccine-widget'                    | true
    'riptide-team/cs-member-service'                          | true
    'riptide-team/dashboard-launcher'                         | true
    'riptide-team/data-widget'                                | true
    'riptide-team/default-radio-button'                       | true
    'riptide-team/digital-contact-history'                    | true
    'riptide-team/digital-contact-history-launcher'           | true
    'riptide-team/ehub-file-enhancement-batch'                | true
    'riptide-team/ei-member-service'                          | true
    'riptide-team/eligibility-hub-designation-api'            | true
    'riptide-team/eligibility-hub-routing-api'                | true
    'riptide-team/eligibility-hub-update-api'                 | true
    'riptide-team/eligibility-loader'                         | true
    'riptide-team/engaged-member-service'                     | true
    'riptide-team/espresso-atdd-utils'                        | true
    'riptide-team/espresso-performance-test'                  | true
    'riptide-team/event-status-history-service'               | true
    'riptide-team/ezcomm-admin-ui'                            | true
    'riptide-team/ezcomm-core'                                | true
    'riptide-team/ezcomm-launcher'                            | true
    'riptide-team/ezcomm-launcher-eligibility-letter'         | true
    'riptide-team/ezcomm-launcher-iset-forms-and-packets'     | true
    'riptide-team/ezcomm-launcher-maestro-appointment-sched'  | true
    'riptide-team/ezcomm-launcher-maestro-gpp-payment-header' | true
    'riptide-team/ezcomm-launcher-maestro-m-and-r'            | true
    'riptide-team/ezcomm-launcher-maestro-provider-info'      | true
    'riptide-team/ezcomm-launcher-maestro-review-rx-benefits' | true
    'riptide-team/financial-overview'                         | true
    'riptide-team/financial-overview-hub'                     | true
    'riptide-team/gatling-performance-test-template'          | true
    'riptide-team/go-to-tab'                                  | true
    'riptide-team/graceful-shutdown'                          | true
    'riptide-team/hca'                                        | true
    'riptide-team/hierarchy-service'                          | true
    'riptide-team/historical-family-link-messages'            | true
    'riptide-team/hover-info-txt-widget'                      | true
    'riptide-team/ibaag-benefits-widget'                      | true
    'riptide-team/ibaag-proxy'                                | true
    'riptide-team/ibaag-widget'                               | true
    'riptide-team/in-scope-policy-batch'                      | true
    'riptide-team/inscope-policy-service'                     | true
    'riptide-team/ite-enrichment-listener'                    | true
    'riptide-team/ite-kafka-producer'                         | true
    'riptide-team/ite-listener-db-api'                        | true
    'riptide-team/ite-listener-icue'                          | true
    'riptide-team/ite-listener-internal'                      | true
    'riptide-team/ite-listener-shared'                        | true
    'riptide-team/ite-milestone-verification'                 | true
    'riptide-team/ite-reprocessing-batch'                     | true
    'riptide-team/marketplace'                                | true
    'riptide-team/msg-campaign-db'                            | true
    'riptide-team/msg-campaign-eligibility'                   | true
    'riptide-team/msg-campaign-inbound'                       | true
    'riptide-team/msg-campaign-inbound-pr'                    | true
    'riptide-team/msg-campaign-outbound'                      | true
    'riptide-team/msg-campaign-outbound-pr'                   | true
    'riptide-team/msg-campaign-perf-test'                     | true
    'riptide-team/msg-campaign-service'                       | true
    'riptide-team/myuhc-indicator'                            | true
    'riptide-team/nb-rest-search'                             | true
    'riptide-team/ness-logger'                                | true
    'riptide-team/nginx-default-backend'                      | true
    'riptide-team/nginx-proxy-template'                       | true
    'riptide-team/notification-buddy'                         | true
    'riptide-team/notification-note-widget'                   | true
    'riptide-team/nps-survey'                                 | true
    'riptide-team/ocm-oam-service'                            | true
    'riptide-team/pafs'                                       | true
    'riptide-team/pafs-ldap-batch'                            | true
    'riptide-team/pass-proxy'                                 | true
    'riptide-team/pmc-widget'                                 | true
    'riptide-team/powerpoint-python-scripts'                  | true
    'riptide-team/providerinfo-messaging'                     | true
    'riptide-team/rest-clients'                               | true
    'riptide-team/riptide-apache'                             | true
    'riptide-team/riptide-docker-images'                      | true
    'riptide-team/riptide-ldap-service'                       | true
    'riptide-team/riptide-memory-utils'                       | true
    'riptide-team/riptide-micro-ui'                           | true
    'riptide-team/riptide-micro-ui-proxy'                     | true
    'riptide-team/riptide-micro-ui-widget-factory'            | true
    'riptide-team/rts-clean-up-app-batch'                     | true
    'riptide-team/rts-enrichment'                             | true
    'riptide-team/rts-monitoring'                             | true
    'riptide-team/rv-nhp-validation'                          | true
    'riptide-team/rx-best-price'                              | true
    'riptide-team/rx-processing-widget'                       | true
    'riptide-team/seed-project'                               | true
    'riptide-team/sens-eligibility-widget'                    | true
    'riptide-team/service-engine-c360'                        | true
    'riptide-team/service-engine-c360-iset'                   | true
    'riptide-team/service-engine-c360-yoda'                   | true
    'riptide-team/service-engine-ehub'                        | true
    'riptide-team/service-engine-ldap'                        | true
    'riptide-team/service-engine-oauth'                       | true
    'riptide-team/service-engine-sms'                         | true
    'riptide-team/service-engine-sql'                         | true
    'riptide-team/session-storage-widget'                     | true
    'riptide-team/sni-admin-portal'                           | true
    'riptide-team/sni-eligibility-check'                      | true
    'riptide-team/sni-eligibility-widget'                     | true
    'riptide-team/sni-kafka'                                  | true
    'riptide-team/sni-members-api'                            | true
    'riptide-team/specialty-go-to-tab'                        | true
    'riptide-team/splunk-universal-fowarder'                  | true
    'riptide-team/sso-manager'                                | true
    'riptide-team/text-email-utils'                           | true
    'riptide-team/tops-credential-api'                        | true
    'riptide-team/tops-dashboard'                             | true
    'riptide-team/tql-widget'                                 | true
    'riptide-team/trafficcop-api'                             | true
    'riptide-team/trafficcop-legacy'                          | true
    'riptide-team/url-optumizer'                              | true
    'riptide-team/widget-factory'                             | true
  }
}
