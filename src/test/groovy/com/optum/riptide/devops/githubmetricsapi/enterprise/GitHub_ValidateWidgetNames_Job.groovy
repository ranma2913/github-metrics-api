package com.optum.riptide.devops.githubmetricsapi.enterprise

import com.optum.riptide.devops.githubmetricsapi.utils.FileWriterUtil
import groovy.util.logging.Slf4j
import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

@Slf4j
@SpringBootTest
class GitHub_ValidateWidgetNames_Job extends Specification {
  @Autowired
  GitHub githubEnterprise

  def "Validate Widget Names"() {
    given:
    Path outputFilePath = Paths.get("target/${LocalDateTime.now().toString().replace(':', '')}_$outputFileName")
    def csvHeadRow = ['Widget Name', 'Repo URL', 'Git Org/Repo'] // Header Row
    def csvData = []
    GHOrganization org = githubEnterprise.getOrganization(orgName)

    widgetNames.split(',').each() { widgetName ->
      try {
        GHRepository repo = org.getRepository(widgetName)
        csvData.add([widgetName, repo.getHtmlUrl(), "${repo.getFullName()}"])
      } catch (e) {
        log.error("Unable to find repo with name = {}", widgetName)
        csvData.add([widgetName, URL.getResource("https://github.optum.com/$orgName"), ""])
      }
    }

    FileWriterUtil.writeSimpleXlsxFile(outputFilePath, csvHeadRow, csvData, sheetName)

    expect:
    githubEnterprise

    where:
    orgName        | outputFileName                        | sheetName | widgetNames
    'riptide-team' | 'Widget Name and Repo Full Name.xlsx' | 'Sheet 1' | ' acet-sfdc-util-sys, beo-update-case-oeim-proc-api,ACET Dashboard Launcher - Pilot,ACET Dashboard Launcher - Prod,acet-digital-contact-history-launcher,acet-member-eligibility-api,acet-update-case-jim-proc-api,API Client Faro Services,API Client OBAPI Services,Appeals Specialty,apple-fitness-plus-api,auth-letter-widget,b360-widget-usage-api,CAHC-Widget-V2,cahc-widget-V3,caml-widget-iset,caml-widget-yoda,cchdoc360adapter,cchdoc360streamservice,cchenrichmentservice,cchrestservice,cchui,CDB Locked Accounts,CHS Eligibility API,claim-search-filter,Client Token Manager,client-report-db-engine,client-report-email-service,client-report-mysql-db-connect,client-report-status-messenger,cns-mailbox,cns-sm-api,commitmentverbiage-widget,Consent Form,covid-19-vaccine-api,covid-19-vaccine-widget,Data Widget,Default Radio Button,Digital Contact History,digital-contact-history-launcher,Ei-member-service (dev),Eligibility-hub-designation-api (designation api),Eligibility-hub-designation-api (family link message),eligibility-hub-update-api,EnterpriseNow-Widget,ezcomm-acet-launcher-v3 (non-prod),ezcomm-admin-ui,ezcomm-core-iset-stage,ezcomm-core-v2,ezcomm-launcher-acet-v2,ezcomm-launcher-eligibility-v2,ezcomm-launcher-forms-and-packets-v3,ezcomm-launcher-iset-stage,ezcomm-launcher-maestro-appointment-sched-dev (non-prod),ezcomm-launcher-maestro-appt-sched,ezcomm-launcher-maestro-gpp-header,ezcomm-launcher-maestro-gpp-payment-header-dev,ezcomm-launcher-maestro-m-and-r,ezcomm-launcher-maestro-prov-info,ezcomm-launcher-maestro-provider-info-dev,ezcomm-launcher-maestro-review-rx-benefits-dev,ezcomm-launcher-maestro-rx-benefits,ezcomm-launcher-v2,ezcomm-launcher-v3 (non-prod),ezcomm-mnr-(prod),Financial Overview,Financial Overview Hub,Go-to-Next button,go-to-provider,Go-to-tab,graceful-shutdown,hca,Hover Info Text Widget,Ibaag Benefits Widget,ibaag-widget,In-scope-batch-dev,Inscope-policy-service-dev,ISET Core Break Fix,ISET Core Break Fix Cloud,ISET Core Dev,ISET Core Dev Cloud,ISET Core Dev2,ISET Core Pilot Cloud,ISET Core Pilot Legacy,ISET Core Production Cloud,ISET Core Production Legacy,ISET Core Stage,ISET Core Stage Cloud,ISET Core Test,ISET Core Test Cloud,ISET Core Test2,ISET Core UAT,ISET Core UAT Cloud,mailbox,mailbox-kana,MarketPlace,middleware-mule-base,msg-campaign-eligibility,msg-campaign-inbound,msg-campaign-inbound-pol,msg-campaign-inbound-pr,msg-campaign-outbound,msg-campaign-outbound-pr,msg-campaign-service,MyUHC Indicator-v2,MyUHC Indicator-v3,Naviguard Note,nb-rest-search,ness-logger,Net Promoter Score,notification-buddy,Notification-note-widget,o360-sfdc-sys-api,OCM OAM Service,pafs-ldap-batch,pafs-ldap-batch (cs service),pafs-ldap-batch (ei service),pafs-ldap-batch (engaged service),pafs-ldap-batch (hierarchy service),pafs-ldap-batch (inscope batch),pafs-ldap-batch (inscope service),Pass-proxy-widget,peloton-digital-membership,PHS Dashboard Launcher,PMC-Save-Widget,providerinfo-messaging,rest-clients,riptide-apache,riptide-ldap-service,riptide-memory-utils,riptide-micro-ui,riptide-micro-ui-widget-factory,RV-NHP Validation,rx-best-price,rx-processing-widget,seed-project,SENS Eligibility Indicator,service-engine-c360-iset,service-engine-c360-yoda,service-engine-ldap,service-engine-oauth,service-engine-sms,service-engine-v2(service-engine-ehub),session-storage-widget,sm-api,SNI (familylink attachments),SNI (kafka-sni),SNI (routing api),sni-admin-portal,sni-eligibility-check,sni-eligibility-widget,sni-members-api,specialty-go-to-tab,spire-cpu-proc-api,SSO Manager,text-email-utils,Tops Credentials API,Tops Dashboard,tql-widget,tql-widget-admin,Traffic-cop YODA,traffic-cop-iset,traffic-cop-miim,Traffic-cop-Yoda,trafficcop-api,url-optumizer,usermind-c360-consumer-(non-prod),widget-factory-IBAAG,widget-factory-ICUE,widget-factory-ISET,widget-factory-MIIM,widget-factory-PASS'
  }

}
