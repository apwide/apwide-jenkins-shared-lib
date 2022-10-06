package com.apwide.jenkins.golive

import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.RestClient
import com.apwide.jenkins.util.ScriptWrapper
import com.apwide.jenkins.util.auth.GoliveAuthenticator

import static com.apwide.jenkins.util.Utilities.urlEncode

class Applications implements Serializable {
    private final RestClient jira

    Applications(ScriptWrapper script, Parameters parameters) {
        this.jira = new RestClient(script, parameters.getConfig(), new GoliveAuthenticator(script, parameters), parameters.getGoliveBaseUrl())
    }

    def findAll() {
        return jira.get("/applications")
    }

    def get(applicationName) {
        def applications = findAll()
        def application = applications.find { it.name.equals(applicationName) }
        if (!application) {
            return null
        } else {
            return application
        }
    }

    def create(applicationName, applicationSchemeId, body = null) {
        jira.post("/application", [
                name: applicationName
        ] << (body ?: [:]))
    }

    def update(id, body = null) {
        jira.put("/application/${urlEncode(id)}", body)
    }

    def update(String applicationName, body = null) {
        def application = get(applicationName)
        if (!application) {
            return null
        }
        jira.put("/application/${urlEncode(application.id)}", [
                name: applicationName
        ] << (body ?: [:]))
    }

    def delete(String applicationName) {
        def application = get(applicationName)
        if (!application) {
            return null
        } else {
            jira.delete("/application/${urlEncode(application.id)}")
        }
    }
}
