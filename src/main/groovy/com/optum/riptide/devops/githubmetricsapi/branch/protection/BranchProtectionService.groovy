package com.optum.riptide.devops.githubmetricsapi.branch.protection

import groovy.util.logging.Slf4j
import org.kohsuke.github.GHBranch
import org.kohsuke.github.GHBranchProtection
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

import javax.validation.constraints.NotNull

@Slf4j
@Service
class BranchProtectionService {
  @Value('${credentials_GIT_TOKEN}')
  String githubToken
  @Autowired
  GitHub githubEnterprise
  @Autowired
  RestTemplate sslRestTemplate

  String getBranchProtection(GHBranch branch) {
    String responseBody = null
    if (branch.isProtected()) {
      responseBody = githubEnterprise.createRequest()
          .method("GET")
          .setRawUrlPath(branch.getProtectionUrl().toString())
          .fetch(String.class)
//      HttpHeaders headers = new HttpHeaders()
//      headers.set('Accept', 'application/vnd.github+json')
//      headers.set('Authorization', "Bearer $githubToken")
//      HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(["new_owner": newOwnerName], headers)
//
//      ResponseEntity<String> responseEntity = sslRestTemplate.getForEntity(
//          "${githubEnterprise.getApiUrl()}/repos/{owner}/{repo}/branches/{branch}/protection",
//          String.class,
//          requestEntity,
//          [owner : branch.getOwner().getOwnerName(),
//           repo  : branch.getOwner().getName(),
//           branch: branch.getName()]
//      )
//      if (responseEntity.getStatusCode() == HttpStatus.OK) {
//        responseBody = responseEntity.getBody()
//      }
    }
    return responseBody
  }

  GHBranch protectBranch(@NotNull GHBranch branch, @NotNull GHBranchProtection protection) {
    branch.enableProtection()
        .addRequiredChecks(protection.getRequiredStatusChecks().getContexts())
        .includeAdmins(protection.getEnforceAdmins().isEnabled())
        .teamReviewDismissals(protection.getRequiredReviews().getDismissalRestrictions().getTeams())
        .userReviewDismissals(protection.getRequiredReviews().getDismissalRestrictions().getUsers())
        // 20220907 not supported: $.required_pull_request_reviews.dismissal_restrictions.apps
        .dismissStaleReviews(protection.getRequiredReviews().isDismissStaleReviews())
        .requireCodeOwnReviews(protection.getRequiredReviews().isRequireCodeOwnerReviews())
        .requiredReviewers(protection.getRequiredReviews().getRequiredReviewers())
        // 20220907 not supported: $.required_pull_request_reviews.bypass_pull_request_allowances.teams
        // 20220907 not supported: $.required_pull_request_reviews.bypass_pull_request_allowances.users
        .restrictPushAccess().teamPushAccess(protection.getRestrictions().getTeams())
        .restrictPushAccess().userPushAccess(protection.getRestrictions().getUsers())
        // 20220907 not supported: $.restrictions.apps
        // 20220907 not supported: $.required_linear_history
        // 20220907 not supported: $.allow_force_pushes
        // 20220907 not supported: $.allow_deletions
        // 20220907 not supported: $.block_creations
        // 20220907 not supported: $.required_conversation_resolution
    .enable()
    return branch
  }
}
