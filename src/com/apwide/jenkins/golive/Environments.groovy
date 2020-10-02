package com.apwide.jenkins.golive

import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.RestClient
import com.apwide.jenkins.util.ScriptWrapper

import static com.apwide.jenkins.util.Utilities.urlEncode

import static java.util.stream.Collectors.joining

class Environments implements Serializable {
    private final RestClient jira

    Environments(ScriptWrapper script, Parameters parameters) {
        this.jira = new RestClient(script, parameters.getConfig(), parameters.getGoliveBaseUrl())
    }

    def findAll(Map criteria = null) {
        def queryString = criteria?.findAll { ['application', 'category'].contains(it.key) && it.value }
                .collect { it -> "${urlEncode(it.key)}=${urlEncode(it.value)}" }
                .stream().collect(joining('&', '?', ''))
        jira.get("/environments${queryString}")
    }

    def search(Map criteria = null) {
        jira.get("/environments/search${toQuery(criteria)}")
    }

    def withEnvironments(Map criteria = null, Closure task) {
        def environments = search(criteria)
        for (environment in environments) {
            task(environment)
        }
    }

    def toQuery(Map criteria = null) {
        if (!criteria || criteria.isEmpty()) {
            return ''
        }

        criteria.collect {
            criterion ->
                if (criterion.value in Collection) {
                    return criterion.value
                            .collect { value -> "${urlEncode(criterion.key)}=${urlEncode(value)}" }
                            .stream().collect(joining('&'))
                } else {
                    return "${urlEncode(criterion.key)}=${urlEncode(criterion.value)}"
                }
        }.stream().collect(joining('&', '?', ''))
    }
}
