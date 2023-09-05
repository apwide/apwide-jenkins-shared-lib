package com.apwide.jenkins.util

import java.util.regex.Pattern

// https://github.com/jenkinsci/script-security-plugin/tree/master/src/main/resources/org/jenkinsci/plugins/scriptsecurity/sandbox/whitelists
class Version {

  int major
  int minor
  int patch

  static Version from(String goliveVersion) {
    def pattern = Pattern.compile("(\\d+)\\.(\\d+).(\\d+)(-SNAPSHOT)?")
    def matcher = pattern.matcher(goliveVersion)

    if (!matcher.matches()) {
      return null
    }
    try {
      def major = matcher.group(1).toInteger()
      def minor = matcher.group(2).toInteger()
      def patch = matcher.group(3).toInteger()
      return new Version(major: major, minor: minor, patch: patch)
    } catch (RuntimeException ex) {
      // not able to parse version
      return null;
    }
  }

  boolean isEqualOrHigherThan(String version) {
    return isEqualOrHigherThan(Version.from(version))
  }

  boolean isEqualOrHigherThan(Version version) {
    if (version == null) {
      return false
    }
    return isEqualTo(version) || isHigherThan(version)
  }

  boolean isLowerThan(Version version) {
    if (version == null) {
      return false
    }
    return version.isHigherThan(this)
  }

  boolean isEqualOrLowerThan(Version version) {
    if (version == null) {
      return false
    }
    return version.isEqualOrHigherThan(this)
  }

  boolean isHigherThan(Version version) {
    if (version == null) {
      return false
    }
    if (major > version.major) {
      return true
    }
    if (minor > version.minor) {
      return true
    }
    return patch > version.patch
  }

  boolean isEqualTo(Version version) {
    if (Version == null) {
      return false
    }
    return major == version.major && minor == version.minor && patch == version.patch
  }
}
