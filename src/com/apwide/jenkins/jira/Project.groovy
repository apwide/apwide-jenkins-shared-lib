package com.apwide.jenkins.jira

import com.apwide.jenkins.util.RestClient

import static com.apwide.jenkins.util.Utilities.urlEncode

class Project implements Serializable {
    private final RestClient jira

    Project(Object script, Map jiraConfig) {
        jira = new RestClient(script, jiraConfig, '/rest/api/2/project')
    }

    def getAll() {
        jira.get()
    }

    def get(id) {
        jira.get("/${urlEncode(id)}")
    }

    def properties(id) {
        jira.get("/${urlEncode(id)}/properties")
    }

    def versions(id) {
        jira.get("/${urlEncode(id)}/versions")
    }
}
