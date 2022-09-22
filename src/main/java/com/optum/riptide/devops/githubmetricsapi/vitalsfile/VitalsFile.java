package com.optum.riptide.devops.githubmetricsapi.vitalsfile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

@Data
public class VitalsFile {
  private String apiVersion = "v1";
  private Metadata metadata = new Metadata();

  @SneakyThrows
  @Override
  public String toString() {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    return mapper.writeValueAsString(this);
  }

  @Data
  public class Metadata {
    private String askId = "~";
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
    @SneakyThrows
    @Override
    public String toString() {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      return mapper.writeValueAsString(this);
    }
  }
}
