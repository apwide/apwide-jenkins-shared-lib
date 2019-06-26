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
        this.buildFailOnError = params.buildFailOnError == null || params.buildFailOnError

        this.config = [
                jiraBaseUrl:       params.jiraBaseUrl       ?: params.config?.jiraBaseUrl       ?: script.env.APW_JIRA_BASE_URL       ?: 'http://localhost:2990/jira',
                jiraCredentialsId: params.jiraCredentialsId ?: params.config?.jiraCredentialsId ?: script.env.APW_JIRA_CREDENTIALS_ID ?: 'jira-credentials',
                project:           params.project           ?: params.config?.project           ?: script.env.APW_JIRA_PROJECT        ?: null,
                environmentId:     params.environmentId     ?: params.config?.environmentId     ?: script.env.APW_ENVIRONMENT_ID      ?: null,
                application:       params.application       ?: params.config?.application       ?: script.env.APW_APPLICATION         ?: null,
                category:          params.category          ?: params.config?.category          ?: script.env.APW_CATEGORY            ?: null,
                unavailableStatus: params.unavailableStatus ?: params.config?.unavailableStatus ?: script.env.APW_UNAVAILABLE_STATUS  ?: 'Down',
                availableStatus:   params.availableStatus   ?: params.config?.availableStatus   ?: script.env.APW_AVAILABLE_STATUS    ?: 'Up',
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
}
