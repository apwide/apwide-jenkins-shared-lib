package com.apwide.jenkins.jira

import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.RestClient
import com.apwide.jenkins.util.ScriptWrapper
import com.apwide.jenkins.util.auth.JiraAuthenticator

import static com.apwide.jenkins.util.Utilities.urlEncode

class Issue implements Serializable {
    private final RestClient jira
    private final ScriptWrapper script
    private final String jiraUrl

    Issue(ScriptWrapper script, Parameters parameters) {
        this.script = script
        this.jira = new RestClient(script, parameters.getConfig(), new JiraAuthenticator(script, parameters), parameters.getJiraAPIUrl('/issue'))
        this.jiraUrl = parameters.getJiraUrl()
    }

    Issue(Object script, Map config) {
        this(script, new Parameters(script, config ?: [:]))
    }

    Issue(Object script, Parameters parameters) {
        this(new ScriptWrapper(script, parameters), parameters)
    }

    def get(issueIdOrKey, queryParams = "") {
        jira.get("/${urlEncode(issueIdOrKey)}?${queryParams}")
    }

    String issueUrl(String issueKey){
        return "${jiraUrl}/browse/${urlEncode(issueKey)}"
    }

    String getIssueInfo(issueIdOrKey){
        try {
            def issueResource = this.get(issueIdOrKey, "fields=summary,comment,status,issuetype")
            String issueKey = issueResource.key
            def issueSummary = issueResource.fields.summary
            return """<a href="${issueUrl(urlEncode(issueKey))}" target="_blank">${issueKey}</a> ${issueSummary}"""
        }
        catch (Throwable e){
            script.debug "Error getting issue with id=${issueIdOrKey} : ${e}"
            script.debug("Exception: ${e}")
            script.debug("Message: ${e.getMessage()}")
            script.debug("Cause: ${e.getCause()}")
            return null
        }

    }
}
