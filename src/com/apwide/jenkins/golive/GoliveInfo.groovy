package com.apwide.jenkins.golive


import com.apwide.jenkins.util.*
import com.apwide.jenkins.util.auth.GoliveAuthenticator

class GoliveInfo implements Serializable {
  private final ScriptWrapper script
  private final RestClient golive
  private final RestClient privateGolive
  private final Parameters parameters
  private final boolean isCloud

  GoliveInfo(ScriptWrapper script, Parameters parameters) {
    this.script = script
    this.golive = new RestClient(script, parameters.getConfig(), new GoliveAuthenticator(script, parameters), parameters.getGoliveBaseUrl())
    this.privateGolive = new RestClient(script, parameters.getConfig(), new GoliveAuthenticator(script, parameters), parameters.getGolivePrivateBaseUrl())
    this.isCloud = parameters.isCloud()
    this.parameters = parameters
  }

  GoliveStatus status() {
    try {
      return new GoliveStatus(version: Version.from(privateGolive.get("/plugin").version), cloud: isCloud, parameters: parameters)
    } catch (Throwable e) {
      try {
        return new GoliveStatus(version: Version.from(golive.get("/plugin").version), cloud: isCloud, parameters: parameters)
      } catch (Throwable t) {
        return new GoliveStatus(version: null, cloud: isCloud, parameters: parameters)
      }
    }
  }
}
