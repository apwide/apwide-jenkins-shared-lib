package com.apwide.jenkins.util

import spock.lang.Specification

class VersionTest  extends Specification {

  def "parse snapshot version"() {
    given:
    def versionString = "10.0.1-SNAPSHOT"

    when:
    def version = Version.from(versionString)

    then:
    version != null
    version.major == 10
    version.minor == 0
    version.patch == 1
  }

  def "parse released version"() {
    given:
    def versionString = "10.0.1"

    when:
    def version = Version.from(versionString)

    then:
    version != null
    version.major == 10
    version.minor == 0
    version.patch == 1
  }

  def "null on invalid"() {
    given:
    def versionString = "10.0.1-dd"

    when:
    def version = Version.from(versionString)

    then:
    version == null
  }

  def "equal version"() {
    given:
    def version1 = Version.from("10.0.1-SNAPSHOT")
    def version2 = Version.from("10.0.1-SNAPSHOT")

    when:
    def areEquals = version1.isEqualTo(version2)

    then:
    areEquals
  }

  def "equal version with/without snapshot"() {
    given:
    def version1 = Version.from("10.0.1-SNAPSHOT")
    def version2 = Version.from("10.0.1")

    when:
    def areEquals = version1.isEqualTo(version2)

    then:
    areEquals
  }

  def "not equal version"() {
    given:
    def version1 = Version.from("10.0.1")
    def version2 = Version.from("10.0.2")

    when:
    def areEquals = version1.isEqualTo(version2)

    then:
    !areEquals
  }

  def "is higher than on major"() {
    given:
    def version1 = Version.from("11.0.1")
    def version2 = Version.from("10.3.4")

    when:
    def isHigher = version1.isHigherThan(version2)

    then:
    isHigher
  }

  def "is higher than on minor"() {
    given:
    def version1 = Version.from("11.5.1")
    def version2 = Version.from("11.3.4")

    when:
    def isHigher = version1.isHigherThan(version2)

    then:
    isHigher
  }

  def "is higher than on patch"() {
    given:
    def version1 = Version.from("11.5.5")
    def version2 = Version.from("11.5.4")

    when:
    def isHigher = version1.isHigherThan(version2)

    then:
    isHigher
  }

  def "equals is not higher"() {
    given:
    def version1 = Version.from("11.5.5")
    def version2 = Version.from("11.5.5")

    when:
    def isHigher = version1.isHigherThan(version2)

    then:
    !isHigher
  }

  def "equals or higher with equals"() {
    given:
    def version1 = Version.from("11.5.5")
    def version2 = Version.from("11.5.5")

    when:
    def isEqualOrHigher = version1.isEqualOrHigherThan(version2)

    then:
    isEqualOrHigher
  }

  def "equals or higher with higher"() {
    given:
    def version1 = Version.from("11.5.7")
    def version2 = Version.from("11.5.6")

    when:
    def isEqualOrHigher = version1.isEqualOrHigherThan(version2)

    then:
    isEqualOrHigher
  }

  def "is not higher"() {
    given:
    def version1 = Version.from("11.5.5")
    def version2 = Version.from("11.5.6")

    when:
    def isEqualOrHigher = version1.isEqualOrHigherThan(version2)

    then:
    !isEqualOrHigher
  }
}
