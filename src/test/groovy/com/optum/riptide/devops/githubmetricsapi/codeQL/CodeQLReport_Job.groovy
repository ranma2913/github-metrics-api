package com.optum.riptide.devops.githubmetricsapi.codeQL

import com.optum.riptide.devops.githubmetricsapi.utils.CellProps
import com.optum.riptide.devops.githubmetricsapi.utils.FileWriterUtil
import groovy.util.logging.Slf4j
import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

@SpringBootTest
@Slf4j
class CodeQLReport_Job extends Specification {
    @Autowired
    GitHub githubEnterprise
    @Autowired
    CodeQLService codeQLService
    @Autowired
    RestTemplate sslRestTemplate
    @Value('${credentials_GIT_TOKEN}')
    String githubToken

    @Unroll("Create csv of repos Code QL repo for org = #orgName")
    def "Create csv of repos Code QL status"() {
        given:
        List<CellProps> headerRow = [new CellProps('Repository', 'String'),
                                     new CellProps('Repo URL', 'String'),
                                     new CellProps('Default Branch', 'String'),
                                     new CellProps('Code QL Merge status', 'Boolean'),
                                     new CellProps('critical', 'String'),
                                     new CellProps('high', 'String'),
                                     new CellProps('medium', 'String'),
                                     new CellProps('low', 'String'),
                                     new CellProps('severity Error Count', 'String'),
                                     new CellProps('severity Note Count', 'String'),
                                     new CellProps('severity Warning Count', 'String'),
                                     new CellProps('Additional Notes', 'String'),]
        GHOrganization org = githubEnterprise.getOrganization(orgName)
        List<GHRepository> repositories = org.listRepositories(100).toList()

        List<List<CellProps>> dataRows = new LinkedList()

        when: 'read the data'
        dataRows.addAll(readCsvDataForOrg(orgName, repositories))

        then: 'export the file'
        Path outputFilePath = Paths.get("target/${LocalDateTime.now().toString().replace(':', '')}_$outputFileName")
        FileWriterUtil.writeXlsxFile(outputFilePath, headerRow, dataRows, sheetName)

        expect:
        repositories.size() == dataRows.size()

        where:
        orgName        | sheetName            | outputFileName
//    'riptide-deprecated-apps' | 'Needs vitals.yaml' | 'riptide-deprecated-apps_missing_vitals.xlsx'
//    'riptide-devops'          | 'Needs vitals.yaml' | 'riptide-devops_missing_vitals.xlsx'
//    'riptide-poc'             | 'Needs vitals.yaml' | 'riptide-poc_missing_vitals.xlsx'
        'riptide-team' | 'Needs code QL file' | 'riptide-team_codeQL_report.xlsx'
    }

    List<List<CellProps>> readCsvDataForOrg(String orgName, List<GHRepository> repositories) {
        def csvData = []
        List<GHRepository> filteredRepos =
                repositories.parallelStream()
                        .map(repo -> {
                            Map<String, Integer> codeSeverityCounts = getCodeQLIssueCount(repo, orgName)
                            List<CellProps> dataRow = [new CellProps("${(repo.getFullName()?.trim() ?: "${repo.getOwner()}/${repo.getName()}")}", 'String'),
                                                       new CellProps(repo.getHtmlUrl() as String, 'URL'),
                                                       new CellProps(repo.getDefaultBranch() as String, 'String'),
                                                       new CellProps(codeQLService.isCodeQLFileMerged(repo) as String, 'String'),
                                                       new CellProps(codeSeverityCounts.get("critical") as String, 'String'),
                                                       new CellProps(codeSeverityCounts.get("high") as String, 'String'),
                                                       new CellProps(codeSeverityCounts.get("medium") as String, 'String'),
                                                       new CellProps(codeSeverityCounts.get("low") as String, 'String'),
                                                       new CellProps(codeSeverityCounts.get("severity Error Count") as String, 'String'),
                                                       new CellProps(codeSeverityCounts.get("severity Note Count") as String, 'String'),
                                                       new CellProps(codeSeverityCounts.get("severity Warning Count") as String, 'String'),
                                                       new CellProps(codeSeverityCounts.get("Additional Notes") as String, 'String')]

                            // def csvRow = [repo.getFullName(), repo.getHtmlUrl(), repo.getDefaultBranch(), codeQLService.isCodeQLFileMerged(repo)]
                            csvData.add(dataRow)
                        })
                        .toList()
        return csvData
    }

    private Map<String, Integer> getCodeQLIssueCount(GHRepository repo, String orgName) {
        HttpHeaders headers = new HttpHeaders()
        headers.set('Accept', 'application/vnd.github+json')
        headers.set('Content-Type', 'application/json')
        headers.set('Authorization', "Bearer $githubToken")
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(headers)
        ResponseEntity<List<Object>> responseEntity
        Map<String, Integer> codeSeverityCountMap = new HashMap<>();

        try {
            responseEntity = sslRestTemplate.exchange("${githubEnterprise.getApiUrl()}/repos/{owner}/{repo}/code-scanning/alerts",
                    HttpMethod.GET,
                    requestEntity,
                    ArrayList.class,
                    [owner: repo.getOwnerName(), repo: repo.getName(), org: orgName])
            List<Objects> resp = responseEntity.getBody()

            if (resp.isEmpty()) {
                codeSeverityCountMap.put("Additional Notes", "This branch hasn't been scanned yet")
                log.info("This branch hasn't been scanned yet {} {}", repo.getName(), codeSeverityCountMap.toString())
                return codeSeverityCountMap;
            } else {
                getScanCounts(resp, codeSeverityCountMap)
                log.info("responseEntity.getStatusCode = {} {}", codeSeverityCountMap.toString(), repo.getName())
            }

        } catch (Exception e) {
            log.error("Exception calling code-scanning/alerts API: ", repo.getName())
            codeSeverityCountMap.put("Additional Notes", e.getMessage())
            log.info("responseEntity.getStatusCode = {} {}", codeSeverityCountMap.toString(), repo.getName())
        }
        return codeSeverityCountMap;
    }

    private void getScanCounts(List<Objects> resp, HashMap<String, Integer> codeSeverityCountMap) {
        Integer severityErrorCount = 0;
        Integer severityNoteCount = 0;
        Integer severityWarningCount = 0;
        Integer criticalCount = 0;
        Integer highCount = 0;
        Integer mediumCount = 0;
        Integer lowCount = 0;
        for (Map codeQlAlert : resp) {
            Map rule = codeQlAlert.get("rule");
            String severity = rule.get("severity")
            String security_severity_level = rule.get("security_severity_level")

            if ("error".equals(severity)) {
                severityErrorCount++;
                switch (security_severity_level) {
                    case "critical":
                        criticalCount++;
                        break;
                    case "high":
                        highCount++;
                        break;
                    case "medium":
                        mediumCount++;
                        break;
                    case "low":
                        lowCount++;
                        break;
                }
            } else if ("note".equals(severity)) {
                severityNoteCount++;
            } else if ("warning".equals(severity)) {
                severityWarningCount++;
            }
        }
        codeSeverityCountMap.put("critical", criticalCount);
        codeSeverityCountMap.put("high", highCount)
        codeSeverityCountMap.put("medium", mediumCount)
        codeSeverityCountMap.put("low", lowCount)
        codeSeverityCountMap.put("severity Error Count", severityErrorCount)
        codeSeverityCountMap.put("severity Note Count", severityNoteCount)
        codeSeverityCountMap.put("severity Warning Count", severityWarningCount)
    }
}
