package com.apwide.jenkins.jira

import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.RestClient
import com.apwide.jenkins.util.ScriptWrapper
import com.apwide.jenkins.util.auth.JiraAuthenticator

class Issues implements Serializable {
    private final RestClient jira
    private final ScriptWrapper script

    Issues(ScriptWrapper script, Parameters parameters) {
        this.script = script
        this.jira = new RestClient(script, parameters.getConfig(), new JiraAuthenticator(script, parameters), parameters.getJiraAPIUrl('/search'))
    }

    Issues(Object script, Map config) {
        this(script, new Parameters(script, config ?: [:]))
    }

    Issues(Object script, Parameters parameters) {
        this(new ScriptWrapper(script, parameters), parameters)
    }

    def issueKeys(jql) {
        script.debug("Find issues by jql: ${jql}")
        def result = jira.post("",[
                jql       : jql,
                startAt   : 0,
                maxResults: 500,
                fields    : [
                        "key"
                ]
        ])
        return result.issues.collect { it.key }
    }
}
