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
    'riptide-team/acet-dashboard-v2'                          | false
    'riptide-team/acet-digital-contact-history-launcher'      | false
    'riptide-team/api-client-faro-services'                   | false
    'riptide-team/api-client-obapi-services'                  | false
    'riptide-team/appeals-specialty'                          | false
    'riptide-team/caml-widget-iset'                           | false
    'riptide-team/caml-widget-yoda'                           | false
    'riptide-team/cdb-locked-accounts'                        | false
    'riptide-team/chs-eligibility-api'                        | false
    'riptide-team/claim-search-filter'                        | false
    'riptide-team/client-report-db-engine'                    | false
    'riptide-team/client-report-email-service'                | false
    'riptide-team/client-report-mysql-db-connect'             | false
    'riptide-team/client-report-status-messenger'             | false
    'riptide-team/client-token-manager'                       | false
    'riptide-team/cns-mailbox'                                | false
    'riptide-team/cns-sm-api'                                 | false
    'riptide-team/commitmentverbiage-widget'                  | false
    'riptide-team/consent-forms-widget'                       | false
    'riptide-team/covid-19-vaccine-api'                       | false
    'riptide-team/covid-19-vaccine-widget'                    | false
    'riptide-team/cs-member-service'                          | false
    'riptide-team/dashboard-launcher'                         | false
    'riptide-team/data-widget'                                | false
    'riptide-team/default-radio-button'                       | false
    'riptide-team/digital-contact-history'                    | false
    'riptide-team/digital-contact-history-launcher'           | false
    'riptide-team/ehub-file-enhancement-batch'                | false
    'riptide-team/ei-member-service'                          | false
    'riptide-team/eligibility-hub-designation-api'            | false
    'riptide-team/eligibility-hub-routing-api'                | true
    'riptide-team/eligibility-hub-update-api'                 | false
    'riptide-team/eligibility-loader'                         | false
    'riptide-team/engaged-member-service'                     | false
    'riptide-team/espresso-atdd-utils'                        | false
    'riptide-team/espresso-performance-test'                  | false
    'riptide-team/event-status-history-service'               | false
    'riptide-team/ezcomm-admin-ui'                            | false
    'riptide-team/ezcomm-core'                                | false
    'riptide-team/ezcomm-launcher'                            | false
    'riptide-team/ezcomm-launcher-eligibility-letter'         | false
    'riptide-team/ezcomm-launcher-iset-forms-and-packets'     | false
    'riptide-team/ezcomm-launcher-maestro-appointment-sched'  | false
    'riptide-team/ezcomm-launcher-maestro-gpp-payment-header' | false
    'riptide-team/ezcomm-launcher-maestro-m-and-r'            | false
    'riptide-team/ezcomm-launcher-maestro-provider-info'      | false
    'riptide-team/ezcomm-launcher-maestro-review-rx-benefits' | false
    'riptide-team/financial-overview'                         | false
    'riptide-team/financial-overview-hub'                     | false
    'riptide-team/go-to-tab'                                  | false
    'riptide-team/graceful-shutdown'                          | false
    'riptide-team/hca'                                        | false
    'riptide-team/hierarchy-service'                          | false
    'riptide-team/historical-family-link-messages'            | false
    'riptide-team/hover-info-txt-widget'                      | false
    'riptide-team/ibaag-benefits-widget'                      | false
    'riptide-team/ibaag-widget'                               | false
    'riptide-team/in-scope-policy-batch'                      | false
    'riptide-team/inscope-policy-service'                     | false
    'riptide-team/ite-enrichment-listener'                    | false
    'riptide-team/ite-kafka-producer'                         | false
    'riptide-team/ite-listener-db-api'                        | false
    'riptide-team/ite-listener-icue'                          | false
    'riptide-team/ite-listener-internal'                      | false
    'riptide-team/ite-listener-shared'                        | false
    'riptide-team/ite-milestone-verification'                 | false
    'riptide-team/ite-reprocessing-batch'                     | false
    'riptide-team/marketplace'                                | false
    'riptide-team/msg-campaign-eligibility'                   | false
    'riptide-team/msg-campaign-inbound'                       | false
    'riptide-team/msg-campaign-inbound-pr'                    | false
    'riptide-team/msg-campaign-outbound'                      | false
    'riptide-team/msg-campaign-outbound-pr'                   | false
    'riptide-team/msg-campaign-service'                       | false
    'riptide-team/myuhc-indicator'                            | false
    'riptide-team/nb-rest-search'                             | false
    'riptide-team/ness-logger'                                | false
    'riptide-team/notification-buddy'                         | false
    'riptide-team/notification-note-widget'                   | false
    'riptide-team/nps-survey'                                 | false
    'riptide-team/ocm-oam-service'                            | false
    'riptide-team/pafs'                                       | false
    'riptide-team/pafs-ldap-batch'                            | false
    'riptide-team/pass-proxy'                                 | false
    'riptide-team/pmc-widget'                                 | false
    'riptide-team/providerinfo-messaging'                     | false
    'riptide-team/rest-clients'                               | false
    'riptide-team/riptide-apache'                             | false
    'riptide-team/riptide-ldap-service'                       | false
    'riptide-team/riptide-memory-utils'                       | false
    'riptide-team/riptide-micro-ui'                           | false
    'riptide-team/riptide-micro-ui-widget-factory'            | false
    'riptide-team/rts-clean-up-app-batch'                     | false
    'riptide-team/rts-enrichment'                             | false
    'riptide-team/rts-monitoring'                             | false
    'riptide-team/rv-nhp-validation'                          | false
    'riptide-team/rx-best-price'                              | false
    'riptide-team/rx-processing-widget'                       | false
    'riptide-team/seed-project'                               | false
    'riptide-team/sens-eligibility-widget'                    | false
    'riptide-team/service-engine-c360-iset'                   | false
    'riptide-team/service-engine-c360-yoda'                   | false
    'riptide-team/service-engine-ehub'                        | false
    'riptide-team/service-engine-ldap'                        | false
    'riptide-team/service-engine-oauth'                       | false
    'riptide-team/service-engine-sms'                         | false
    'riptide-team/session-storage-widget'                     | false
    'riptide-team/sni-admin-portal'                           | false
    'riptide-team/sni-eligibility-check'                      | false
    'riptide-team/sni-eligibility-widget'                     | false
    'riptide-team/sni-kafka'                                  | false
    'riptide-team/sni-members-api'                            | false
    'riptide-team/specialty-go-to-tab'                        | false
    'riptide-team/sso-manager'                                | false
    'riptide-team/text-email-utils'                           | false
    'riptide-team/tops-credential-api'                        | false
    'riptide-team/tops-dashboard'                             | false
    'riptide-team/tql-widget'                                 | false
    'riptide-team/trafficcop-api'                             | false
    'riptide-team/trafficcop-legacy'                          | false
    'riptide-team/url-optumizer'                              | false
    'riptide-team/widget-factory'                             | false
  }
}
