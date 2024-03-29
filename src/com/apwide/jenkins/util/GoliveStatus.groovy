package com.apwide.jenkins.util

class GoliveStatus {
  Version version
  boolean cloud
  Parameters parameters

  boolean supportsUnlimitedDescription() {
    return cloud || version?.isEqualOrHigherThan("9.1.0")
  }

  boolean supportsDeploymentIssues() {
    if (cloud) {
      return version?.isEqualOrHigherThan("9.12.0")
    } else {
      return version?.isEqualOrHigherThan("9.10.0")
    }
  }

  boolean mustAddIssuesAsDeploymentIssues() {
    return supportsDeploymentIssues() && !parameters.forceDeployIssuesInDescription()
  }
}
