package com.apwide.jenkins.util

import com.cloudbees.groovy.cps.NonCPS

class Parameters implements Serializable {
    final String httpMode
    final String path
    final Map body
    final boolean buildFailOnError
    final Map config
    final Map params

    private final JiraServerAPIBasePath = "/rest/api/2"
    private final JiraCloudAPIBasePath = "/rest/api/3"

    Parameters(script, Map params) {
        this.httpMode = params.httpMode ?: 'GET'
        this.path = params.path ?: ''
        this.body = params.body ?: null

        if (params.buildFailOnError != null) {
            this.buildFailOnError = params.buildFailOnError
        } else {
            this.buildFailOnError = script.env.APW_BUILD_FAIL_ON_ERROR == null || script.env.APW_BUILD_FAIL_ON_ERROR.toBoolean()
        }

        this.config = [
            jiraBaseUrl:              params.jiraBaseUrl              ?: params.config?.jiraBaseUrl              ?: script.env.APW_JIRA_BASE_URL               ?: 'http://localhost:2990/jira',
            jiraCredentialsId:        params.jiraCredentialsId        ?: params.config?.jiraCredentialsId        ?: script.env.APW_JIRA_CREDENTIALS_ID         ?: 'jira-credentials',
            jiraCloudCredentialsId:   params.jiraCloudCredentialsId   ?: params.config?.jiraCloudCredentialsId   ?: script.env.APW_JIRA_CLOUD_CREDENTIALS_ID   ?: null,
            jiraCloudBaseUrl:         params.jiraCloudBaseUrl         ?: params.config?.jiraCloudBaseUrl         ?: script.env.APW_JIRA_CLOUD_BASE_URL         ?: null,
            goliveCloudCredentialsId: params.goliveCloudCredentialsId ?: params.config?.goliveCloudCredentialsId ?: script.env.APW_GOLIVE_CLOUD_CREDENTIALS_ID ?: null,
            project:                  params.project                  ?: params.config?.project                  ?: script.env.APW_JIRA_PROJECT                ?: null,
            environmentId:            params.environmentId            ?: params.config?.environmentId            ?: script.env.APW_ENVIRONMENT_ID              ?: null,
            application:              params.application              ?: params.config?.application              ?: script.env.APW_APPLICATION                 ?: null,
            category:                 params.category                 ?: params.config?.category                 ?: script.env.APW_CATEGORY                    ?: null,
            permissionScheme:         params.permissionScheme         ?: params.config?.permissionScheme         ?: script.env.APW_PERMISSION_SCHEME           ?: null,
            applicationSchemeId:      params.applicationSchemeId      ?: params.config?.applicationSchemeId      ?: script.env.APW_APPLICATION_SCHEME_ID       ?: null,
            unavailableStatus:        params.unavailableStatus        ?: params.config?.unavailableStatus        ?: script.env.APW_UNAVAILABLE_STATUS          ?: 'Down',
            availableStatus:          params.availableStatus          ?: params.config?.availableStatus          ?: script.env.APW_AVAILABLE_STATUS            ?: 'Up',
            logLevel:                 params.logLevel                 ?: params.config?.logLevel                 ?: script.env.APW_LOG_LEVEL                   ?: 'DEBUG',
            dontTouchStatus:          params.dontTouchStatus          ?: params.config?.dontTouchStatus          ?: script.env.APW_DONT_TOUCH_STATUS           ?: null,
            forceGoliveServer:        params.forceGoliveServer        ?: params.config?.forceGoliveServer        ?: script.env.APW_FORCE_GOLIVE_SERVER         ?: false,

            buildFailOnError:  buildFailOnError,
            httpRequestOptions: params.httpRequestOptions ?: params.config?.httpRequestOptions
        ]

        this.params = params
    }

    @NonCPS
    Map getConfig() {
        config
    }

    @NonCPS
    String getGoliveBaseUrl() {
        isCloud() ? 'https://golive.apwide.net/api' :  "${config.jiraBaseUrl}/rest/apwide/tem/1.1"
    }

    @NonCPS
    String getGoliveCloudCredentialsId() {
        return config.goliveCloudCredentialsId
    }

    String getJiraCloudCredentialsId() {
        config.jiraCloudCredentialsId
    }

    String getJiraCredentialsId() {
        config.jiraCredentialsId
    }

    @NonCPS
    boolean isJiraCloud() {
        if (params.forceGoliveServer) {
          return false
        }
        if (params.jiraBaseUrl != null || params.jiraCredentialsId != null ) {
          return false
        }
        if (params.jiraCloudCredentialsId != null || params.jiraCloudBaseUrl!= null ) {
          return true
        }
        return config.jiraCloudCredentialsId != null && config.jiraCloudBaseUrl != null
    }

    @NonCPS
    boolean isCloud() {
        hasGoliveCloudCredentials() && !config.forceGoliveServer
    }

    @NonCPS
    boolean hasGoliveCloudCredentials() {
        config.goliveCloudCredentialsId != null
    }

    @NonCPS
    String getJiraUrl(String path = '') {
        isJiraCloud() ? "${config.jiraCloudBaseUrl}" : "${config.jiraBaseUrl}${path}"
    }

    @NonCPS
    String getJiraAPIUrl(String path = '') {
        isJiraCloud() ? "${config.jiraCloudBaseUrl}${JiraCloudAPIBasePath}${path}" : "${config.jiraBaseUrl}${JiraServerAPIBasePath}${path}"
    }

    String getApplication() {
        config.application
    }

    String getCategory() {
        config.category
    }

    String getEnvironmentId() {
        config.environmentId
    }

    String getUnavailableStatus() {
        config.unavailableStatus
    }

    String getAvailableStatus() {
        config.availableStatus
    }

    String getProject() {
        config.project
    }

    String getPermissionScheme() {
        config.permissionScheme
    }

    String getApplicationSchemeId() {
        config.applicationSchemeId
    }

    String logLevel() {
        config.logLevel
    }

    String getDontTouchStatus() {
        return config.dontTouchStatus
    }
}
