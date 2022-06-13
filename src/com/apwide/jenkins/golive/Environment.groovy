package com.apwide.jenkins.golive

import com.apwide.jenkins.issue.ChangeLogIssueKeyExtractor
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

    def getStatus(applicationName, categoryName) {
        golive.get("/status-change?application=${urlEncode(applicationName)}&category=${urlEncode(categoryName)}", '200:304,404')
    }

    def setStatus(applicationName, categoryName, statusName) {
        golive.put("/status-change?application=${urlEncode(applicationName)}&category=${urlEncode(categoryName)}", [name: statusName])
    }

    def setDeployedVersion(applicationName, categoryName, deployedVersion, buildNumber, description, attributes) {
        golive.put("/deployment?application=${urlEncode(applicationName)}&category=${urlEncode(categoryName)}", [
                versionName: deployedVersion,
                buildNumber: buildNumber,
                description: description,
                attributes : attributes
        ])
    }

    def sendDeploymentInfo(applicationName, categoryName, deployedVersion, buildNumber, description, attributes) {
        script.debug("apwSendDeploymentInfo to Golive...")
        try{
            script.debug("applicationName=${applicationName}, categoryName=${categoryName}, deployedVersion=${deployedVersion}, buildNumber=${buildNumber}, description=${description}, attributes=${attributes}")
            golive.put("/deployment?application=${urlEncode(applicationName)}&category=${urlEncode(categoryName)}", [
                    versionName: deployedVersion,
                    buildNumber: buildNumber,
                    description: render(script, buildNumber),
                    attributes : attributes
            ])
        } catch (Throwable e){
            script.debug("Unexpected error in apwSendDeploymentInfo to Golive: ${e}")
            script.debug("Error message: ${e.getMessage()}")
            throw e
        }
    }

    private def render(ScriptWrapper script, buildNumber) {
        def issueKeyExtractor = new ChangeLogIssueKeyExtractor()
        def issueKeys = issueKeyExtractor.extractIssueKeys(script)
        def text = """✅ Job #${buildNumber}"""
        issueKeys.each {it ->
            String issueInfo = issue.getIssueInfo(it)
                text += ("\n ${issueInfo!=null?issueInfo:it}")
        }
        if (text.size() >= 255){ // TODO: length limitation removed in Golive 9.1.+
            text = text.substring(0, 252) + '...'
        }
        return text
    }

    def checkAndUpdateStatus(applicationName, categoryName, unavailableStatus, availableStatus, String dontTouchStatus = null, Closure checkStatusOperation = null) {
        def env = get(applicationName, categoryName)
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
            status = getStatus(applicationName, categoryName)
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
