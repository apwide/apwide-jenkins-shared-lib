package com.apwide.jenkins.jira

import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.RestClient
import com.apwide.jenkins.util.ScriptWrapper
import com.apwide.jenkins.util.auth.JiraAuthenticator

import static com.apwide.jenkins.util.Utilities.urlEncode

class Version implements Serializable {
    private final RestClient jira

    Version(ScriptWrapper script, Parameters parameters) {
        jira = new RestClient(script, parameters.getConfig(), new JiraAuthenticator(script, parameters), parameters.getJiraAPIUrl('/version'))
    }

    def create(Map version) {
        return jira.post('', version)
    }

    def get(String id) {
        return jira.get("/${urlEncode(id)}")
    }

    def update(String id, Map version) {
        return jira.put("/${urlEncode(id)}", version)
    }

    def delete(String id) {
        return jira.delete("/${urlEncode(id)}")
    }
}
