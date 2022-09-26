package com.optum.riptide.devops.githubmetricsapi.vitalsfile

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import groovy.transform.AutoClone
import groovy.transform.EqualsAndHashCode
import org.apache.commons.lang3.StringUtils

@EqualsAndHashCode
@AutoClone
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

    if (map.metadata.askId instanceof Set && map.metadata.askId?.size() > 0) {
      // Set type is preferred
      this.metadata.askId = map.metadata.askId
    } else if (map.metadata.askId instanceof String && map.metadata.askId?.trim()) {
      // String type is still supported
      this.metadata.askId = map.metadata.askId
    } else {
      // fallback to default value.
      this.metadata.setAskId(this.metadata.askId)
    }

    this.metadata.caAgileId = map.metadata.caAgileId instanceof String && map.metadata.caAgileId?.trim() ? map.metadata.caAgileId.trim() : this.metadata.caAgileId

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

  @EqualsAndHashCode
  @AutoClone
  class Metadata {
    // String or Set<String>
    def askId = ['~']
    String caAgileId = 'poc'
    String projectKey = 'poc'
    String projectFriendlyName = 'poc'
    String componentType = 'code'
    String targetQG = 'ADOPTION'

    /**
     * Converts String param to Set.
     * @param askId String
     */
    void setAskId(String askId) {
      this.setAskId([askId] as Set)
    }

    /**
     * Replaces the value of askId
     * @param askIds Set<String>
     */
    void setAskId(Set<String> askIds) {
      this.askId = askIds
    }

    /**
     * puts askIds Set into askId. Set automatically handles duplicate values.
     * @param askIds Set<String>
     */
    void putAskId(Set<String> askIds) {
      if (this.askId instanceof Set) {
        this.askId += askIds
      } else if (this.askId instanceof String) {
        this.askId = [this.askId]
        this.askId += askIds
      }
    }

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
