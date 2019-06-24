package com.apwide.jenkins.golive

import com.apwide.jenkins.util.RestClient

import static com.apwide.jenkins.util.RestClient.checkUrl
import static com.apwide.jenkins.util.Utilities.urlEncode

class Environment implements Serializable {
    private final script
    private final RestClient jira

    Environment(Object script, Map jiraConfig) {
        this.script = script
        this.jira = new RestClient(script, jiraConfig, '/rest/apwide/tem/1.1')
    }

    def update(id, body) {
        jira.put("/environment/${urlEncode(id)}", body)
    }

    def getEnvironment(applicationName, categoryName) {
        jira.get("/environment?application=${urlEncode(applicationName)}&category=${urlEncode(categoryName)}")
    }

    def getStatus(applicationName, categoryName) {
        jira.get("/status-change?application=${urlEncode(applicationName)}&category=${categoryName}")
    }

    def setStatus(applicationName, categoryName, statusName) {
        jira.put("/status-change?application=${urlEncode(applicationName)}&category=${urlEncode(categoryName)}", [name: statusName])
    }

    def setDeployedVersion(applicationName, categoryName, deployedVersion) {
        jira.put("/deployment?application=${urlEncode(applicationName)}&category=${urlEncode(categoryName)}", [versionName: deployedVersion])
    }

    def checkAndUpdateStatus(applicationName, categoryName, unavailableStatus, availableStatus, Closure checkStatusOperation = null) {
        def env = getEnvironment(applicationName, categoryName)
        def status = getStatus(applicationName, categoryName)
        def checkStatus = checkStatusOperation ?: { environment ->
            checkUrl url:environment.url,
                    nbRetry: 3,
                    httpMode: 'GET',
                    this.script
        }
        try {
            checkStatus(env)
        } catch (err) {
            if (!unavailableStatus.equals(status.statusName)) {
                return setStatus(applicationName, categoryName, unavailableStatus)
            }
        }
        if (!availableStatus.equals(status.statusName)) {
            return setStatus(applicationName, categoryName, availableStatus)
        } else {
            return status
        }
    }
}
