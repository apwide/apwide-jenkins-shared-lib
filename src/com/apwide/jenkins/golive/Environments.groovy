package com.apwide.jenkins.golive

import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.RestClient
import com.apwide.jenkins.util.ScriptWrapper
import com.apwide.jenkins.util.auth.GoliveAuthenticator

import static com.apwide.jenkins.util.Utilities.urlEncode
import static java.util.stream.Collectors.joining

class Environments implements Serializable {
    private final RestClient jira

    Environments(ScriptWrapper script, Parameters parameters) {
        this.jira = new RestClient(script, parameters.getConfig(), new GoliveAuthenticator(script, parameters), parameters.getGoliveBaseUrl())
    }

    def findAll(Map criteria = null) {
        def environmentCriteria = criteria?.findAll { ['application', 'category'].contains(it.key) && it.value }
                .collect { it -> "${urlEncode(it.key + 'Name')}=${urlEncode(it.value)}" }
        def queryString = !environmentCriteria?.isEmpty() ?  environmentCriteria.stream().collect(joining('&', '?', '')) : ''
        queryString = queryString == '?' ? '' : queryString
        jira.get("/environments/search/paginated${queryString}").environments
    }

    def search(Map criteria = null) {
        jira.get("/environments/search/paginated${toQuery(criteria)}").environments
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
