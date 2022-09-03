package com.optum.riptide.devops.githubmetricsapi.enterprise

import com.optum.riptide.devops.githubmetricsapi.utils.FileWriterUtil
import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

@SpringBootTest
class GitHub_TransferRepositoriesSpec extends Specification {
  @Autowired
  GitHub githubEnterprise

  @Unroll("Transfer Repositories from org=#orgName to org=riptide-deprecated-apps")
  def "Transfer Repositories to riptide-deprecated-apps"() {
    given:
    def csvHeadRow = ['Repository', 'Repo URL', 'Last Updated'] // Header Row
    def csvData = []
    Path outputFilePath = Paths.get("target/${LocalDateTime.now().toString().replace(':', '')}_$outputFileName")
    Files.createDirectories(outputFilePath.getParent())
    outputFilePath = Files.createFile(outputFilePath)

    GHOrganization org = githubEnterprise.getOrganization("riptide-team")
    List<GHRepository> repositories = org.listRepositories(100).toList()

    Set<String> keepRepos = repoKeepList.split(',')

    List<GHRepository> reposToTransfer =
        repositories.stream()
        // filter repos to only those not found in repoKeepList
            .filter(repo -> {
              !keepRepos.contains(repo.getFullName())
            })
            .map(
                repo -> {
                  def csvRow = [repo.getFullName(), repo.getHtmlUrl(), repo.getUpdatedAt()]
                  csvData.add(csvRow)
                })
//            .map(
//                repo -> {
//                  //todo transfer repos
//                })
            .toList()

    FileWriterUtil.writeSimpleXlsxFile(outputFilePath, csvHeadRow, csvData, sheetName)

    expect:
    reposToTransfer

    where: 'Keep List'
    orgName        | outputFileName                     | sheetName           | repoKeepList
    'riptide-team' | 'riptide-team_transfer-repos.xlsx' | 'Ready to Transfer' | 'riptide-team/acip-performance-testing,riptide-team/a4me-gateway,acet-salesforce/acet-member-eligibility-api,acet-salesforce/acet-sfdc-util-sys,acet-salesforce/acet-update-case-jim-proc-api,acet-salesforce/beo-update-case-oeim-proc-api,acet-salesforce/middleware-mule-base,acet-salesforce/spire-cpu-proc-api,b360-team/b360-widget-usage-API,cirrus-data/cch-doc360-adapter,cirrus-data/cch-doc360-stream-service,cirrus-data/cch-enrichment-service,cirrus-data/cch-rest-service,cirrus-data/cch-ui,DigitalMemberships/apple-fitness-plus-api,DigitalMemberships/peloton-digital-membership,iset/cahc-widget,iset/enterprise-now-widget,iset/iset-core,iset/naviguard-note-widget,JourneyMicroservices-Team/usermind-c360-consumer,myuhc2-consumer/ei-mailbox,myuhc2-consumer/ei-mailbox-api,myuhc2-consumer/ei-mailbox-kana,riptide-deprecated-apps/DEPRECATED-auth-letter-widget,riptide-deprecated-apps/ezcomm-mnr,riptide-team/acet-dashboard-v2,riptide-team/acet-digital-contact-history-launcher,riptide-team/api-client-faro-services,riptide-team/api-client-obapi-services,riptide-team/appeals-specialty,riptide-team/caml-widget-iset,riptide-team/caml-widget-yoda,riptide-team/cdb-locked-accounts,riptide-team/chs-eligibility-api,riptide-team/claim-search-filter,riptide-team/client-report-db-engine,riptide-team/client-report-email-service,riptide-team/client-report-mysql-db-connect,riptide-team/client-report-status-messenger,riptide-team/client-token-manager,riptide-team/cns-mailbox,riptide-team/cns-sm-api,riptide-team/commitmentverbiage-widget,riptide-team/consent-forms-widget,riptide-team/covid-19-vaccine-api,riptide-team/covid-19-vaccine-widget,riptide-team/cs-member-service,riptide-team/dashboard-launcher,riptide-team/data-widget,riptide-team/default-radio-button,riptide-team/digital-contact-history,riptide-team/digital-contact-history-launcher,riptide-team/ehub-file-enhancement-batch,riptide-team/ei-member-service,riptide-team/eligibility-hub-designation-api,riptide-team/eligibility-hub-routing-api,riptide-team/eligibility-hub-update-api,riptide-team/eligibility-loader,riptide-team/engaged-member-service,riptide-team/espresso-atdd-utils,riptide-team/espresso-performance-test,riptide-team/event-status-history-service,riptide-team/ezcomm-admin-ui,riptide-team/ezcomm-core,riptide-team/ezcomm-launcher,riptide-team/ezcomm-launcher-eligibility-letter,riptide-team/ezcomm-launcher-iset-forms-and-packets,riptide-team/ezcomm-launcher-maestro-appointment-sched,riptide-team/ezcomm-launcher-maestro-gpp-payment-header,riptide-team/ezcomm-launcher-maestro-m-and-r,riptide-team/ezcomm-launcher-maestro-provider-info,riptide-team/ezcomm-launcher-maestro-review-rx-benefits,riptide-team/financial-overview,riptide-team/financial-overview-hub,riptide-team/go-to-tab,riptide-team/graceful-shutdown,riptide-team/hca,riptide-team/hierarchy-service,riptide-team/historical-family-link-messages,riptide-team/hover-info-txt-widget,riptide-team/ibaag-benefits-widget,riptide-team/ibaag-widget,riptide-team/in-scope-policy-batch,riptide-team/inscope-policy-service,riptide-team/ite-enrichment-listener,riptide-team/ite-kafka-producer,riptide-team/ite-listener-db-api,riptide-team/ite-listener-icue,riptide-team/ite-listener-internal,riptide-team/ite-listener-shared,riptide-team/ite-milestone-verification,riptide-team/ite-reprocessing-batch,riptide-team/marketplace,riptide-team/msg-campaign-eligibility,riptide-team/msg-campaign-inbound,riptide-team/msg-campaign-inbound-pr,riptide-team/msg-campaign-outbound,riptide-team/msg-campaign-outbound-pr,riptide-team/msg-campaign-service,riptide-team/myuhc-indicator,riptide-team/nb-rest-search,riptide-team/ness-logger,riptide-team/notification-buddy,riptide-team/notification-note-widget,riptide-team/nps-survey,riptide-team/ocm-oam-service,riptide-team/pafs,riptide-team/pafs-ldap-batch,riptide-team/pass-proxy,riptide-team/pmc-widget,riptide-team/providerinfo-messaging,riptide-team/rest-clients,riptide-team/riptide-apache,riptide-team/riptide-ldap-service,riptide-team/riptide-memory-utils,riptide-team/riptide-micro-ui,riptide-team/riptide-micro-ui-widget-factory,riptide-team/rts-clean-up-app-batch,riptide-team/rts-enrichment,riptide-team/rts-monitoring,riptide-team/rv-nhp-validation,riptide-team/rx-best-price,riptide-team/rx-processing-widget,riptide-team/seed-project,riptide-team/sens-eligibility-widget,riptide-team/service-engine-c360-iset,riptide-team/service-engine-c360-yoda,riptide-team/service-engine-ehub,riptide-team/service-engine-ldap,riptide-team/service-engine-oauth,riptide-team/service-engine-sms,riptide-team/session-storage-widget,riptide-team/sni-admin-portal,riptide-team/sni-eligibility-check,riptide-team/sni-eligibility-widget,riptide-team/sni-kafka,riptide-team/sni-members-api,riptide-team/specialty-go-to-tab,riptide-team/sso-manager,riptide-team/text-email-utils,riptide-team/tops-credential-api,riptide-team/tops-dashboard,riptide-team/tql-widget,riptide-team/trafficcop-api,riptide-team/trafficcop-legacy,riptide-team/url-optumizer,riptide-team/widget-factory'
  }

}