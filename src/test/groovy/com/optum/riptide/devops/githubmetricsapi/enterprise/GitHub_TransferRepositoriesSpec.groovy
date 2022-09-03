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
              !keepRepos.contains(repo.getName())
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
    'riptide-team' | 'riptide-team_transfer-repos.xlsx' | 'Ready to Transfer' | 'acet-dashboard-v2,acet-digital-contact-history-launcher,api-client-faro-services,api-client-obapi-services,appeals-specialty,caml-widget-iset,caml-widget-yoda,cdb-locked-accounts,chs-eligibility-api,claim-search-filter,client-report-db-engine,client-report-email-service,client-report-mysql-db-connect,client-report-status-messenger,client-token-manager,cns-mailbox,cns-sm-api,commitmentverbiage-widget,consent-forms-widget,covid-19-vaccine-api,covid-19-vaccine-widget,cs-member-service,dashboard-launcher,data-widget,default-radio-button,digital-contact-history,digital-contact-history-launcher,ehub-file-enhancement-batch,ei-member-service,eligibility-hub-designation-api,eligibility-hub-routing-api,eligibility-hub-update-api,eligibility-loader,engaged-member-service,espresso-atdd-utils,espresso-performance-test,event-status-history-service,ezcomm-admin-ui,ezcomm-core,ezcomm-launcher,ezcomm-launcher-eligibility-letter,ezcomm-launcher-iset-forms-and-packets,ezcomm-launcher-maestro-appointment-sched,ezcomm-launcher-maestro-gpp-payment-header,ezcomm-launcher-maestro-m-and-r,ezcomm-launcher-maestro-provider-info,ezcomm-launcher-maestro-review-rx-benefits,financial-overview,financial-overview-hub,go-to-tab,graceful-shutdown,hca,hierarchy-service,historical-family-link-messages,hover-info-txt-widget,ibaag-benefits-widget,ibaag-widget,in-scope-policy-batch,inscope-policy-service,ite-enrichment-listener,ite-kafka-producer,ite-listener-db-api,ite-listener-icue,ite-listener-internal,ite-listener-shared,ite-milestone-verification,ite-reprocessing-batch,marketplace,msg-campaign-eligibility,msg-campaign-inbound,msg-campaign-inbound-pr,msg-campaign-outbound,msg-campaign-outbound-pr,msg-campaign-service,myuhc-indicator,nb-rest-search,ness-logger,notification-buddy,notification-note-widget,nps-survey,ocm-oam-service,pafs,pafs-ldap-batch,pass-proxy,pmc-widget,providerinfo-messaging,rest-clients,riptide-apache,riptide-ldap-service,riptide-memory-utils,riptide-micro-ui,riptide-micro-ui-widget-factory,rts-clean-up-app-batch,rts-enrichment,rts-monitoring,rv-nhp-validation,rx-best-price,rx-processing-widget,seed-project,sens-eligibility-widget,service-engine-c360-iset,service-engine-c360-yoda,service-engine-ehub,service-engine-ldap,service-engine-oauth,service-engine-sms,session-storage-widget,sni-admin-portal,sni-eligibility-check,sni-eligibility-widget,sni-kafka,sni-members-api,specialty-go-to-tab,sso-manager,text-email-utils,tops-credential-api,tops-dashboard,tql-widget,trafficcop-api,trafficcop-legacy,url-optumizer,widget-factory'
  }

}