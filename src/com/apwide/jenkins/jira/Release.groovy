package com.apwide.jenkins.jira

import com.apwide.jenkins.issue.ChangeLogIssueKeyExtractor
import com.apwide.jenkins.util.*
import com.apwide.jenkins.util.auth.JiraAuthenticator
import net.sf.json.JSONArray

class Release implements Serializable {
    private final ScriptWrapper script
    private final RestClient jira
    private final Issue issue
    private final Parameters parameters
    private final Version version
    private final Project project

    Release(ScriptWrapper script, Parameters parameters) {
        this.script = script
        this.jira = new RestClient(script, parameters.getConfig(), new JiraAuthenticator(script, parameters), parameters.getJiraAPIUrl('/'))
        this.issue = new Issue(script, parameters)
        this.version = new Version(script, parameters)
        this.project = new Project(script, parameters)
        this.parameters = parameters
    }

    def sendReleaseInfo(versionName, versionDescription, projectIdOrKey, startDate = new Date(), Collection<String> issueKeys, released = false, releaseDate) {
        script.debug("apwSendReleaseInfo to Jira...")
        try {
            def computedIssueKeys = issueKeys ?: new ChangeLogIssueKeyExtractor().extractIssueKeys(script) as String[]
            def computedDescription = versionDescription ?: renderDescription(issueKeys)
            script.debug("""
              versionName=${versionName},
              versionDescription=${computedDescription},
              projectIdOrKey=${projectIdOrKey}
              issueKeys=${computedIssueKeys}
            """.stripIndent())

            def projectVersions = ((JSONArray) project.versions(projectIdOrKey)).toArray()
            def targetVersion
            def existingVersion = projectVersions.find({ it -> versionName.equalsIgnoreCase(it.name) })
            if (existingVersion) {
                targetVersion = existingVersion
            } else {
                def project = project.get(projectIdOrKey)
                targetVersion = version.create([
                        name: versionName,
                        projectId  : project.id
                ])
            }

            def payload = [
                    description: computedDescription,
                    startDate  : startDate,
                    released   : released
            ]
            if (releaseDate || released) {
                payload.releaseDate = releaseDate ?: new Date()
            }

            targetVersion = version.update(targetVersion.id, payload)

            for (String issueKey : computedIssueKeys) {
                try {
                    issue.addFixVersion(issueKey, targetVersion.name)
                }
                catch (Throwable e) {
                    script.debug("Could not set fixVersion for this issueKey: ${issueKey} (${e})")
                }
            }

            return targetVersion


        } catch (Throwable e) {
            script.debug("Unexpected error in apwSendReleaseInfo to Jira: ${e}")
            script.debug("Error message: ${e.getMessage()}")
            throw e
        }
    }

    private def renderDescription(Collection<String> issueKeys) {
        def text = """✅ Release Notes"""
        issueKeys.each { it ->
            text += ("\n ${it}")
        }
        return text
    }

}
