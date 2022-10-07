package com.apwide.jenkins.golive

import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.RestClient
import com.apwide.jenkins.util.ScriptWrapper
import com.apwide.jenkins.util.auth.GoliveAuthenticator

import static com.apwide.jenkins.util.Utilities.urlEncode

class Categories implements Serializable {
    private final RestClient jira

    Categories(ScriptWrapper script, Parameters parameters) {
        this.jira = new RestClient(script, parameters.getConfig(), new GoliveAuthenticator(script, parameters), parameters.getGoliveBaseUrl())
    }

    def findAll() {
        return jira.get("/categories")
    }

    def get(categoryName) {
        def categories = findAll()
        def category = categories.find { it.name.equals(categoryName) }
        if (!category) {
            return null
        } else {
            return category
        }
    }

    def create(categoryName, body = null) {
        return jira.post("/category", [
                name: categoryName
        ] << (body ?: [:]))
    }

    def update(id, body = null) {
        jira.put("/category/${urlEncode(id)}", body)
    }

    def update(String categoryName, body = null) {
        def category = get(categoryName)
        if (!category) {
            return null
        }
        jira.put("/category/${urlEncode(category.id)}", [
                name: categoryName
        ] << (body ?: [:]))
    }

    def delete(String categoryName) {
        def category = get(categoryName)
        if (!category) {
            return null
        } else {
            jira.delete("/category/${urlEncode(category.id)}")
        }
    }
}
