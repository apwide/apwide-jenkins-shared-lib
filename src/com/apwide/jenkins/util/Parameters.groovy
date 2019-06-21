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
                baseUrl:         params.baseUrl       ?:  params.config?.baseUrl       ?: script.env.JIRA_BASE_URL       ?: 'http://localhost:8080',
                credentialsId:   params.credentialsId ?:  params.config?.credentialsId ?: script.env.JIRA_CREDENTIALS_ID ?: null,
                version:         params.version       ?:  params.config?.version       ?: script.env.JIRA_VERSION        ?: '8.0.2',
                environmentId:   params.environmentId ?:  params.config?.environmentId ?: script.env.APW_ENVIRONMENT_ID  ?: null,
                application:     params.application   ?:  params.config?.application   ?: script.env.APW_APPLICATION     ?: null,
                category:        params.category      ?:  params.config?.category      ?: script.env.APW_CATEGORY        ?: null
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
}
