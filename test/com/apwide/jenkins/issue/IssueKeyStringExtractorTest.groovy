package com.apwide.jenkins.issue

import spock.lang.Specification

class IssueKeyStringExtractorTest extends Specification {

    final IssueKeyStringExtractor extractor = new IssueKeyStringExtractor()

    def "should find 1 issueKey"() {
        given:
        def comment = "TEM-123 my comment"

        when:
        def issueKeys = extractor.extractIssueKeys(comment)

        then:
        issueKeys.size() == 1
    }

    def "should find 2 issueKeys"() {
        given:
        def comment = "TEM-123 my comment CS-234"

        when:
        def issueKeys = extractor.extractIssueKeys(comment)

        then:
        issueKeys.size() == 2
    }

}
