package com.apwide.jenkins.jira

import com.apwide.jenkins.util.RestClient
import com.apwide.jenkins.util.ScriptWrapper

import static com.apwide.jenkins.util.Utilities.urlEncode

class Version implements Serializable {
    private final RestClient jira

    Version(ScriptWrapper script, Map jiraConfig) {
        jira = new RestClient(script, jiraConfig, '/rest/api/2/version')
    }

    def create(Map version) {
        jira.post('', version)
    }

    def get(String id) {
        jira.get("/${urlEncode(id)}")
    }

    def update(String id, Map version) {
        jira.put("/${urlEncode(id)}", version)
    }

    def delete(String id) {
        jira.delete("/${urlEncode(id)}")
    }

    def release(String id) {
        throw new UnsupportedOperationException('not yet implemented')
    }
}
