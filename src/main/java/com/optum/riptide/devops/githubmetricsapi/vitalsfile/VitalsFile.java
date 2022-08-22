package com.optum.riptide.devops.githubmetricsapi.vitalsfile;

import lombok.Data;

@Data
public class VitalsFile {
  private String apiVersion;
  private Metadata metadata;

  public String getApiVersion() {
    return apiVersion;
  }

  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  @Data
  public class Metadata {
    private String askId = "UHGWM110-017197";
    private String caAgileId = "poc";
    private String projectKey;
    private String projectFriendlyName;
    private String componentType = "code";
    private String targetQG = "ADOPTION";

    public String getAskId() {
      return askId;
    }

    public void setAskId(String askId) {
      this.askId = askId;
    }

    public String getCaAgileId() {
      return caAgileId;
    }

    public void setCaAgileId(String caAgileId) {
      this.caAgileId = caAgileId;
    }

    public String getProjectKey() {
      return projectKey;
    }

    public void setProjectKey(String projectKey) {
      this.projectKey = projectKey;
    }

    public String getProjectFriendlyName() {
      return projectFriendlyName;
    }

    public void setProjectFriendlyName(String projectFriendlyName) {
      this.projectFriendlyName = projectFriendlyName;
    }

    public String getComponentType() {
      return componentType;
    }

    public void setComponentType(String componentType) {
      this.componentType = componentType;
    }

    public String getTargetQG() {
      return targetQG;
    }

    public void setTargetQG(String targetQG) {
      this.targetQG = targetQG;
    }
  }
}
