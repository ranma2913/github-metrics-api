package com.optum.riptide.devops.githubmetricsapi.vitalsfile;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class VitalsFile {
  private String apiVersion = "v1";
  private Metadata metadata = new Metadata();

  @Data
  public class Metadata {
    private String askId = "UHGWM110-017197";
    private String caAgileId = "poc";
    private String projectKey = "poc";
    private String projectFriendlyName = "poc";
    private String componentType = "code";
    private String targetQG = "ADOPTION";

    public void setCaAgileId(String caAgileId) {
      this.caAgileId = StringUtils.isNotBlank(caAgileId) ? caAgileId : "poc";
    }

    public void setProjectKey(String projectKey) {
      this.projectKey = StringUtils.isNotBlank(projectKey) ? projectKey : "poc";
    }

    public void setProjectFriendlyName(String projectFriendlyName) {
      this.projectFriendlyName =
          StringUtils.isNotBlank(projectFriendlyName) ? projectFriendlyName : "poc";
    }
  }
}
