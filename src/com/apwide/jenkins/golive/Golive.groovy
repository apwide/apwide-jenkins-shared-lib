package com.apwide.jenkins.golive

import com.apwide.jenkins.util.RestClient

import static com.apwide.jenkins.util.Utilities.urlEncode

class Golive implements Serializable {
    private final RestClient jira

    Golive(Object script, Map jiraConfig) {
        jira = new RestClient(script, jiraConfig, '/rest/apwide/tem/1.1')
    }

    def updateEnvironment(id, body) {
        jira.put("/environment/${urlEncode(id)}", body)
    }

    def getEnvironmentStatus(applicationName, categoryName) {
        jira.get("/status-change?application=${urlEncode(applicationName)}&category=${categoryName}")
    }

    def updateEnvironmentStatus(applicationName, categoryName, statusName) {
        jira.put("/status-change?application=${urlEncode(applicationName)}&category=${urlEncode(categoryName)}", [name: statusName])
    }

    def updateDeployedVersion(applicationName, categoryName, deployedVersion) {
        jira.put("/deployment?application=${urlEncode(applicationName)}&category=${urlEncode(categoryName)}", [versionName: deployedVersion])
    }

    def checkAndUpdateEnvironmentStatus(applicationName, categoryName, unavailableStatus, availableStatus, Closure checkAction) {
        def env = getEnvironmentStatus(applicationName, categoryName)
        try {
            checkAction()
        } catch (err) {
            if (!unavailableStatus.equals(env.statusName)) {
                updateEnvironmentStatus(applicationName, categoryName, unavailableStatus)
            }
        }
        if (!availableStatus.equals(env.statusName)) {
            updateEnvironmentStatus(applicationName, categoryName, availableStatus)
        }
    }
}
