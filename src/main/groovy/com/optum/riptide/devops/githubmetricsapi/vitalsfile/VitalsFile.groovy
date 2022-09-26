package com.optum.riptide.devops.githubmetricsapi.vitalsfile

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.apache.commons.lang3.StringUtils

class VitalsFile {
  String apiVersion
  Metadata metadata

  VitalsFile() {
    this.apiVersion = 'v1'
    this.metadata = new Metadata()
  }

  VitalsFile(Map map) {
    this.apiVersion = map.apiVersion
    this.metadata = new Metadata()
    this.metadata.askId = map.metadata.askId?.trim() ?: this.metadata.askId
    this.metadata.caAgileId = map.metadata.caAgileId?.trim() ?: this.metadata.caAgileId
    this.metadata.projectKey = map.metadata.projectKey?.trim() ?: this.metadata.projectKey
    this.metadata.projectFriendlyName = map.metadata.projectFriendlyName?.trim() ?: this.metadata.projectFriendlyName
    this.metadata.componentType = map.metadata.componentType?.trim() ?: this.metadata.componentType
    this.metadata.targetQG = map.metadata.targetQG?.trim() ?: this.metadata.targetQG
  }

  String toString() {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
    String thisString = mapper.writeValueAsString(this)
    return thisString.replaceAll('^---\n', '')
  }

  class Metadata {
    String askId = '~'
    String caAgileId = 'poc'
    String projectKey = 'poc'
    String projectFriendlyName = 'poc'
    String componentType = 'code'
    String targetQG = 'ADOPTION'


    void setCaAgileId(String caAgileId) {
      this.caAgileId = StringUtils.isNotBlank(caAgileId) ? caAgileId : 'poc'
    }

    void setProjectKey(String projectKey) {
      this.projectKey = StringUtils.isNotBlank(projectKey) ? projectKey : 'poc'
    }

    void setProjectFriendlyName(String projectFriendlyName) {
      this.projectFriendlyName =
          StringUtils.isNotBlank(projectFriendlyName) ? projectFriendlyName : 'poc'
    }
  }
}
