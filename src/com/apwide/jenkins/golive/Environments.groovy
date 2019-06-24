package com.apwide.jenkins.golive

import com.apwide.jenkins.util.RestClient

import java.util.stream.Collectors

class Environments implements Serializable {
    private final RestClient jira

    Environments(Object script, Map jiraConfig) {
        jira = new RestClient(script, jiraConfig, '/rest/apwide/tem/1.1')
    }

    def findAll(Map criteria = null) {
        def queryString =  criteria?.findAll { ['application', 'category'].contains(it.key) }
                .collect { it -> "${it.key}=${it.value}" }
                .stream().collect(Collectors.joining('&', '?', ''))
        jira.get("/environments${queryString}")
    }
}
