package com.apwide.jenkins.util

class Parameters implements Serializable {
    final String httpMode
    final String path
    final Map body
    final boolean buildFailOnError
    final Map config
    final Map params

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
                jiraBaseUrl:         params.jiraBaseUrl         ?: params.config?.jiraBaseUrl         ?: script.env.APW_JIRA_BASE_URL         ?: 'http://localhost:2990/jira',
                jiraCredentialsId:   params.jiraCredentialsId   ?: params.config?.jiraCredentialsId   ?: script.env.APW_JIRA_CREDENTIALS_ID   ?: 'jira-credentials',
                project:             params.project             ?: params.config?.project             ?: script.env.APW_JIRA_PROJECT          ?: null,
                environmentId:       params.environmentId       ?: params.config?.environmentId       ?: script.env.APW_ENVIRONMENT_ID        ?: null,
                application:         params.application         ?: params.config?.application         ?: script.env.APW_APPLICATION           ?: null,
                category:            params.category            ?: params.config?.category            ?: script.env.APW_CATEGORY              ?: null,
                permissionScheme:    params.permissionScheme    ?: params.config?.permissionScheme    ?: script.env.APW_PERMISSION_SCHEME     ?: null,
                applicationSchemeId: params.applicationSchemeId ?: params.config?.applicationSchemeId ?: script.env.APW_APPLICATION_SCHEME_ID ?: null,
                unavailableStatus:   params.unavailableStatus   ?: params.config?.unavailableStatus   ?: script.env.APW_UNAVAILABLE_STATUS    ?: 'Down',
                availableStatus:     params.availableStatus     ?: params.config?.availableStatus     ?: script.env.APW_AVAILABLE_STATUS      ?: 'Up',
                logLevel:            params.logLevel            ?: params.config?.logLevel            ?: script.env.APW_LOG_LEVEL             ?: 'DEBUG',

                buildFailOnError:  buildFailOnError
        ]

        this.params = params
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
}
