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

    def get(applicationName, categoryName) {
        jira.get("/environment?application=${urlEncode(applicationName)}&category=${urlEncode(categoryName)}")
    }

    def getStatus(applicationName, categoryName) {
        jira.get("/status-change?application=${urlEncode(applicationName)}&category=${categoryName}", '200:304,404')
    }

    def setStatus(applicationName, categoryName, statusName) {
        jira.put("/status-change?application=${urlEncode(applicationName)}&category=${urlEncode(categoryName)}", [name: statusName])
    }

    def setDeployedVersion(applicationName, categoryName, deployedVersion) {
        jira.put("/deployment?application=${urlEncode(applicationName)}&category=${urlEncode(categoryName)}", [versionName: deployedVersion])
    }

    def checkAndUpdateStatus(applicationName, categoryName, unavailableStatus, availableStatus, Closure checkStatusOperation = null) {
        def env = get(applicationName, categoryName)
        if (!checkStatusOperation && !env.url) {
            script.echo("No check nor url provided for environment ${env.application.name}-${env.category.name}, status won't be updated")
            return
        }
        def status = [:]
        try {
            status = getStatus(applicationName, categoryName)
        } catch (err) {
            // no fail on status if not exist
        }
        def checkStatus = checkStatusOperation ?: { environment ->
            checkUrl url:environment.url,
                    nbRetry: 3,
                    httpMode: 'GET',
                    this.script
        }
        try {
            checkStatus(env)
        } catch (err) {
            if (!unavailableStatus.equals(status?.statusName)) {
                return setStatus(applicationName, categoryName, unavailableStatus)
            }
        }
        if (!availableStatus.equals(status?.statusName)) {
            return setStatus(applicationName, categoryName, availableStatus)
        } else {
            return status
        }
    }
}
