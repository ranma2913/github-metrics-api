package com.optum.riptide.devops.githubmetricsapi.vitalsfile

import spock.lang.Specification

class VitalsFileSpec extends Specification {
  def "VitalsFile.toString"() {
    given:
    VitalsFile vitalsFile = new VitalsFile()
    def vitalsFileString = vitalsFile.toString()

    expect:
    vitalsFileString == '''apiVersion: "v1"
metadata:
  askId: "~"
  caAgileId: "poc"
  projectKey: "poc"
  projectFriendlyName: "poc"
  componentType: "code"
  targetQG: "ADOPTION"
'''
  }
}
