package com.apwide.jenkins

import com.apwide.jenkins.golive.Deployment
import com.apwide.jenkins.golive.Environment
import com.apwide.jenkins.golive.Environments
import com.apwide.jenkins.golive.Golive
import com.apwide.jenkins.jira.Issue
import com.apwide.jenkins.jira.Project
import com.apwide.jenkins.jira.Version
import com.apwide.jenkins.util.MockHttpRequestPlugin
import com.apwide.jenkins.util.MockPipelineScript
import com.apwide.jenkins.util.MockReadJsonPlugin
import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.ScriptWrapper
import spock.lang.Specification

class JiraInstanceTest extends Specification {

    private final jiraConfig = [
            jiraBaseUrl: 'http://localhost:2990/jira',
            jiraCredentialsId: 'localhost-jira-admin'
    ]
    private final env = [
            JIRA_BASE_URL: 'http://localhost:2990/jira',
            JIRA_CREDENTIALS_ID: 'localhost-jira-admin'
    ]
    private final jenkinsScript = new MockPipelineScript(new MockHttpRequestPlugin(), new MockReadJsonPlugin())
    private final script = new ScriptWrapper(jenkinsScript, new Parameters(jenkinsScript, jiraConfig))

    def "get list of projects"() {
        given:
        def project = new Project(script, jiraConfig)

        when:
        def projects = project.getAll()

        then:
        projects != null
        projects.size() > 1
    }

    def "get list of environments"() {
        given:
        def golive = new Environments(script, new Parameters(script, jiraConfig))

        when:
        def environments = golive.findAll()

        then:
        environments != null
        environments.size() > 1
        print(environments)
    }

    def "get list of environments for eCommerce application"() {
        given:
        def golive = new Environments(script, new Parameters(script, jiraConfig))

        when:
        def environments = golive.findAll application:'eCommerce'

        then:
        environments != null
        environments.size() > 1
    }

    def "search for environments"() {
        given:
        def environments = new Environments(script, new Parameters(script, jiraConfig))

        when:
        def result = environments.search([
                applicationName:'eCommerce',
                statusName:['Up', 'Down'],
                '# servers':'1'
        ])

        then:
        result != null
        result.size() > 0
    }

    def "with environments"() {
        given:
        def environments = new Environments(script, new Parameters(script, jiraConfig))

        when:
        environments.withEnvironments([
                applicationName:'eCommerce',
                statusName:['Up', 'Down'],
                '# servers':'4'
        ]) { environment -> script.echo environment.url }

        then:
        true
    }

    def "get version information ECOM-3.1"() {
        given:
        def version = new Version(script, new Parameters(script, jiraConfig))

        when:
        def foundVersion = version.get("10210")

        then:
        foundVersion instanceof Map
    }

    def "get project versions"() {
        given:
        def project = new Project(script, jiraConfig)

        when:
        def foundVersions = project.versions('ECP')

        then:
        foundVersions != null
    }

    def "create version"() {
        given:
        def version = new Version(script, new Parameters(script, jiraConfig))
        def versionName = "Pipeline Version ${new Date().getTime()}"
        when:
        def createdVersion = version.create(
                description: 'An excellent version',
                name: versionName,
                project: 'ECP'
        )

        then:
        createdVersion instanceof Map
        createdVersion.name == versionName
    }

    def "check environment status"() {
        given:
        def environment = new Environment(script, new Parameters(script, jiraConfig))

        when:
        def updatedStatus = environment.checkAndUpdateStatus(58, 'Down', 'Up')

        then:
        updatedStatus != null
    }

    def "set status"() {
        given:
        def environment = new Environment(script, new Parameters(script, jiraConfig))
        def newStatus = 'Up'

        when:
        def updatedStatus = environment.setStatus(58, newStatus)

        then:
        updatedStatus.status.name == newStatus
    }

    def "concat map"() {
        given:
        Map params = [
                httpMethod: 'GET',
                path      : '/path',
                body      : null
        ]

        when:
        Map mergedConfig = (jiraConfig?: []) << (params?: [])
        Parameters parameters = new Parameters([env: env], mergedConfig)

        then:
        parameters != null
    }

    def "create environment and categories"() {
        given:
        def golive = new Golive(script, new Parameters(script, jiraConfig))

        when:
        def env = golive.createEnvironmentAndCategoryIfNotExist(
                'eCommerce',
                'Test 2',
                'Default EnvironmentPermission Scheme')

        then:
        env != null
    }

    def "create environment"() {
        given:
        def environment = new Environment(script, new Parameters(script, jiraConfig))

        when:
        def env = environment.create(
                'eCommerce',
                'Dev2',
                'Default EnvironmentPermission Scheme')

        then:
        env != null
    }

    def "get issue info"() {
        given:
        def issue = new Issue(script, new Parameters(script, jiraConfig))

        when:
        def issueInfo = issue.getIssueInfo("ITSM-1")

        then:
        issueInfo != null
    }

    def "send deployment info"() {
        given:
        def deployment = new Deployment(script, new Parameters(script, jiraConfig))
        def versionName = "V 23.23.23"
        def buildNumber = 299
        def environmentId = 16
        def description ="""✅ Job #308
<a href="https://apwide.atlassian.net/browse/TEM-2507" target="_blank">TEM-2507</a> Slack notifications fail when tags contains href + title is in the payload (PROD)
"""

        when:
        def deploymentResult = deployment.sendDeploymentInfo(environmentId+"",null, null, versionName, buildNumber, description, null)

        then:
        deploymentResult != null
        deploymentResult.environmentId == environmentId
        deploymentResult.versionName == versionName
        deploymentResult.description == description
    }

}
