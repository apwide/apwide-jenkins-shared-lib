package com.apwide.jenkins.golive

import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.RestClient
import com.apwide.jenkins.util.ScriptWrapper

import static com.apwide.jenkins.util.RestClient.checkUrl
import static com.apwide.jenkins.util.Utilities.urlEncode

class Environment implements Serializable {
    private final script
    private final RestClient jira

    Environment(ScriptWrapper script, Parameters parameters) {
        this.script = script
        this.jira = new RestClient(script, parameters.getConfig(), parameters.getGoliveBaseUrl())
    }

    def update(id, body) {
        jira.put("/environment/${urlEncode(id)}", body)
    }

    def update(applicationName, categoryName, body) {
        def env = get(applicationName, categoryName)
        update(env.id, body)
    }

    def create(applicationName, categoryName, permissionSchemeName, body = null) {
        jira.post("/environment", [
                application: [
                        name: applicationName
                ],
                category: [
                        name: categoryName
                ],
                environmentPermissionScheme: [
                        name: permissionSchemeName
                ]
        ] << (body ?: [:]))
    }

    def get(applicationName, categoryName) {
        jira.get("/environment?application=${urlEncode(applicationName)}&category=${urlEncode(categoryName)}", '200:304,404')
    }

    def getStatus(applicationName, categoryName) {
        jira.get("/status-change?application=${urlEncode(applicationName)}&category=${urlEncode(categoryName)}", '200:304,404')
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
            script.debug("No check nor url provided for environment ${env.application.name}-${env.category.name}, status won't be updated")
            return
        }
        def status = [:]
        try {
            status = getStatus(applicationName, categoryName)
        } catch (err) {
            // no fail on status if not exist
        }
        def checkStatus = checkStatusOperation
        if (!checkStatus) {
            checkStatus = { environment ->
                checkUrl url:environment.url,
                        nbRetry: 3,
                        httpMode: 'GET',
                        this.script
            }
            script.debug("Status is going to be checked with default check status (environment URL)")
        } else {
            script.debug("status is going to be checked with custom check status body")
        }
        try {
            script.debug("check status")
            checkStatus(env)
            if (!availableStatus.equals(status?.statusName)) {
                script.debug("set status to ${availableStatus}")
                return setStatus(applicationName, categoryName, availableStatus)
            }
        } catch (err) {
            if (!unavailableStatus.equals(status?.statusName)) {
                script.debug("set status to ${unavailableStatus}")
                return setStatus(applicationName, categoryName, unavailableStatus)
            } else {
                script.debug("unexpected error on checking status")
            }
        }
        return status
    }
}
