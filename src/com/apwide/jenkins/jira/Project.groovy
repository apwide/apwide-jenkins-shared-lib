package com.apwide.jenkins.jira

import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.RestClient
import com.apwide.jenkins.util.ScriptWrapper
import com.apwide.jenkins.util.auth.JiraAuthenticator

import static com.apwide.jenkins.util.Utilities.urlEncode

class Project implements Serializable {
    private final RestClient jira

    Project(ScriptWrapper script, Parameters parameters) {
        jira = new RestClient(script, parameters.getConfig(), new JiraAuthenticator(script, parameters), parameters.getJiraAPIUrl('/project'))
    }

    Project(Object script, Map config) {
        this(script, new Parameters(script, config ?: [:]))
    }

    Project(Object script, Parameters parameters) {
        this(new ScriptWrapper(script, parameters), parameters)
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
