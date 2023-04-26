package com.apwide.jenkins.golive

import com.apwide.jenkins.issue.ChangeLogIssueKeyExtractor
import com.apwide.jenkins.jira.Issue
import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.RestClient
import com.apwide.jenkins.util.ScriptWrapper
import com.apwide.jenkins.util.Version
import com.apwide.jenkins.util.auth.GoliveAuthenticator

import static com.apwide.jenkins.util.Utilities.urlEncode

class Deployment implements Serializable {
    private final ScriptWrapper script
    private final RestClient golive
    private final Issue issue
    private final boolean isCloud

    Deployment(ScriptWrapper script, Parameters parameters) {
        this.script = script
        this.golive = new RestClient(script, parameters.getConfig(), new GoliveAuthenticator(script, parameters), parameters.getGoliveBaseUrl())
        this.issue = new Issue(script, parameters)
        this.isCloud = parameters.isCloud()
    }

    def setDeployedVersion(environmentId, applicationName, categoryName, deployedVersion, buildNumber, description, attributes) {
        golive.put("/deployment?${environmentId?"environmentId=$environmentId":""}&application=${urlEncode(applicationName)}&category=${urlEncode(categoryName)}", [
                versionName: deployedVersion,
                buildNumber: buildNumber,
                description: description,
                attributes : attributes
        ])
    }

    def sendDeploymentInfo(environmentId, applicationName, categoryName, deployedVersion, buildNumber, description, attributes) {
        script.debug("apwSendDeploymentInfo to Golive...")
        try{
            def computedBuildNumber = buildNumber?:script.getBuildNumber()
            def computedDescription = description?:render(script, computedBuildNumber)
            script.debug("applicationName=${applicationName}, categoryName=${categoryName}, deployedVersion=${deployedVersion}, buildNumber=${computedBuildNumber}, description=${computedDescription}, attributes=${attributes}")
            return golive.put("/deployment?${environmentId ? "environmentId=$environmentId":""}&application=${urlEncode(applicationName)}&category=${urlEncode(categoryName)}", [
                    versionName: deployedVersion,
                    buildNumber: computedBuildNumber,
                    description: computedDescription,
                    attributes : attributes
            ])
        } catch (Throwable e){
            script.debug("Unexpected error in apwSendDeploymentInfo to Golive: ${e}")
            script.debug("Error message: ${e.getMessage()}")
            throw e
        }
    }

    private Version goliveVersion(){
        try {
            return Version.from(golive.get("/plugin").version)
        } catch (Throwable e){
            return null
        }
    }

    private def render(ScriptWrapper script, buildNumber) {
        def issueKeyExtractor = new ChangeLogIssueKeyExtractor()
        def issueKeys = issueKeyExtractor.extractIssueKeys(script)
        def text = """✅ Job #${buildNumber}"""
        def supportsUnlimitedDescription = isCloud || goliveVersion()?.isEqualOrHigherThan("9.1.0")
        issueKeys.each {it ->
            if (supportsUnlimitedDescription) {
                String issueInfo = issue.getIssueInfo(it)
                text += ("\n ${issueInfo != null ? issueInfo : it}")
            }else{
                text += ("\n ${it}")
            }
        }
        if (!supportsUnlimitedDescription && text.size() >= 255){
            text = text.substring(0, 252) + '...'
        }
        return text
    }

}
