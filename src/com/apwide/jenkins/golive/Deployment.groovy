package com.apwide.jenkins.golive

import com.apwide.jenkins.issue.ChangeLogIssueKeyExtractor
import com.apwide.jenkins.jira.Issue
import com.apwide.jenkins.jira.Issues
import com.apwide.jenkins.util.GoliveStatus
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
    private final Issues issues
    private final Parameters parameters
    private final boolean isCloud
    private final boolean forceDeployIssueInDescription

    Deployment(ScriptWrapper script, Parameters parameters) {
        this.script = script
        this.golive = new RestClient(script, parameters.getConfig(), new GoliveAuthenticator(script, parameters), parameters.getGoliveBaseUrl())
        this.issue = new Issue(script, parameters)
        this.issues = new Issues(script, parameters)
        this.isCloud = parameters.isCloud()
        this.parameters = parameters
        this.forceDeployIssueInDescription = parameters.forceDeployIssuesInDescription()
    }

    def setDeployedVersion(environmentId, applicationName, categoryName, deployedVersion, buildNumber, description, attributes) {
        def environmentIdentification = toEnvironmentIdentification(environmentId, applicationName, categoryName)
        golive.put("/deployment?${environmentIdentification}", [
                versionName: deployedVersion,
                buildNumber: buildNumber,
                description: description,
                attributes : attributes
        ])
    }

    def sendDeploymentInfo(environmentId, applicationName, categoryName, deployedVersion, buildNumber, description, attributes, deployedIssuesJql) {
        script.debug("apwSendDeploymentInfo to Golive...")
        try{
            def goliveStatus = goliveStatus()
            def computedBuildNumber = "${buildNumber ?: script.getBuildNumber()}"
            def changeLogsIssueKeys = new ChangeLogIssueKeyExtractor().extractIssueKeys(script) as String[]
            def computedDescription = description ?: renderDescription(changeLogsIssueKeys, computedBuildNumber, goliveStatus)
            script.debug("""
              environmentId=${environmentId},
              applicationName=${applicationName},
              categoryName=${categoryName},
              deployedVersion=${deployedVersion},
              buildNumber=${computedBuildNumber},
              description=${computedDescription},
              attributes=${attributes},
              deployedIssuesJql=${deployedIssuesJql}
            """.stripIndent())

            def payload = [
                versionName: deployedVersion,
                buildNumber: computedBuildNumber,
                description: computedDescription,
                attributes : attributes
            ]
            def issueKeys = []
            if (deployedIssuesJql){
                try {
                    def foundIssueKeys = issues.issueKeys(deployedIssuesJql)
                    script.debug("Found issue keys: ${foundIssueKeys} (jql: ${deployedIssuesJql})")
                    issueKeys.addAll(foundIssueKeys)
                }
                catch (Throwable e){
                    script.debug("Error searching for issueKeys using this jql: ${deployedIssuesJql} (${e}: ${e.message})")
                }
            }

            if (goliveStatus.mustAddIssuesAsDeploymentIssues()) {
                script.debug("changeLogsIssueKeys=${changeLogsIssueKeys}")
                issueKeys.addAll(changeLogsIssueKeys)
            }

            if (!issueKeys.isEmpty()){
                script.debug("issueKeys=${issueKeys}")
                payload.issueKeys = issueKeys
            }

            def environmentIdentification = toEnvironmentIdentification(environmentId, applicationName, categoryName)
            script.debug("payload: ${payload}")

            return golive.put("/deployment?${environmentIdentification}", payload)
        } catch (Throwable e){
            script.debug("Unexpected error in apwSendDeploymentInfo to Golive: ${e}")
            script.debug("Error message: ${e.getMessage()}")
            throw e
        }
    }

  private static String toEnvironmentIdentification(environmentId, applicationName, categoryName) {
    return [
        environmentId ? "environmentId=$environmentId" : "",
        applicationName ? "application=${urlEncode(applicationName)}" : "",
        categoryName ? "category=${urlEncode(categoryName)}" : ""
    ].join("&")
  }

  private GoliveStatus goliveStatus() {
      try {
        return new GoliveStatus(version: Version.from(golive.get("/plugin").version), cloud: isCloud, parameters: parameters)
      } catch (Throwable e){
        return new GoliveStatus(version: null, cloud: isCloud, parameters: parameters)
      }
    }

    private def renderDescription(String[] issueKeys, String buildNumber, GoliveStatus goliveStatus) {
        if(goliveStatus.mustAddIssuesAsDeploymentIssues()) {
          return """✅ Job #${buildNumber}"""
        }

        def text = """✅ Job #${buildNumber}"""
        issueKeys.each {it ->
            if (goliveStatus.supportsUnlimitedDescription()) {
                String issueInfo = issue.getIssueInfo(it)
                text += ("\n ${issueInfo != null ? issueInfo : it}")
            }else{
                text += ("\n ${it}")
            }
        }
        if (!goliveStatus.supportsUnlimitedDescription() && text.size() >= 255){
            text = text.substring(0, 252) + '...'
        }
        return text
    }

}
