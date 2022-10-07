package com.apwide.jenkins.golive

import com.apwide.jenkins.jira.Issue
import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.RestClient
import com.apwide.jenkins.util.ScriptWrapper
import com.apwide.jenkins.util.auth.GoliveAuthenticator

import static com.apwide.jenkins.util.RestClient.checkUrl
import static com.apwide.jenkins.util.Utilities.urlEncode

class Environment implements Serializable {
    private final ScriptWrapper script
    private final RestClient golive
    private final Issue issue

    Environment(ScriptWrapper script, Parameters parameters) {
        this.script = script
        this.golive = new RestClient(script, parameters.getConfig(), new GoliveAuthenticator(script, parameters), parameters.getGoliveBaseUrl())
        this.issue = new Issue(script, parameters)
    }

    def update(id, body) {
        golive.put("/environment/${urlEncode(id)}", body)
    }

    def update(applicationName, categoryName, body) {
        def env = get(applicationName, categoryName)
        update(env.id, body)
    }

    def create(applicationName, categoryName, permissionSchemeName, body = null) {
        golive.post("/environment", [
                application                : [
                        name: applicationName
                ],
                category                   : [
                        name: categoryName
                ],
                environmentPermissionScheme: [
                        name: permissionSchemeName
                ]
        ] << (body ?: [:]))
    }

    def get(applicationName, categoryName) {
        golive.get("/environment?application=${urlEncode(applicationName)}&category=${urlEncode(categoryName)}", '200:304,404')
    }

    def get(environmentId) {
        golive.get("/environment/${environmentId}", '200:304,404')
    }

    def getStatus(environmentId) {
        golive.get("/status-change?environmentId=${environmentId}", '200:304,404')
    }

    def setStatus(environmentId, statusName) {
        golive.put("/status-change?environmentId=${environmentId}", [name: statusName])
    }

    /*def setDeployedVersion(applicationName, categoryName, deployedVersion, buildNumber, description, attributes) {
        golive.put("/deployment?application=${urlEncode(applicationName)}&category=${urlEncode(categoryName)}", [
                versionName: deployedVersion,
                buildNumber: buildNumber,
                description: description,
                attributes : attributes
        ])
    }*/

    def checkAndUpdateStatus(environmentId, unavailableStatus, availableStatus, String dontTouchStatus = null, Closure checkStatusOperation = null) {
        def env = get(environmentId)
        def applicationName = env.application.name
        def categoryName = env.category.name
        if (!checkStatusOperation && !env.url) {
            script.debug("No check nor url provided for environment ${env.application.name}-${env.category.name}, status won't be updated")
            return
        }
        if (dontTouchStatus != null && env.status*.name == dontTouchStatus) {
            script.debug("Environment ${applicationName} ${categoryName} is in dont touch state ${dontTouchStatus} and so, it's status won't be modified")
            return
        }
        def status = [:]
        try {
            status = getStatus(environmentId)
        } catch (err) {
            // no fail on status if not exist
        }
        def checkStatus = checkStatusOperation
        if (!checkStatus) {
            checkStatus = { environment ->
                checkUrl url: environment.url,
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
                return setStatus(environmentId, availableStatus)
            }
        } catch (err) {
            if (!unavailableStatus.equals(status?.statusName)) {
                script.debug("set status to ${unavailableStatus}")
                return setStatus(environmentId, unavailableStatus)
            } else {
                script.debug("unexpected error on checking status")
            }
        }
        return status
    }
}
