package com.apwide.jenkins.golive

import com.apwide.jenkins.util.RestClient

class Categories implements Serializable {
    private final script
    private final RestClient jira

    Categories(Object script, Map jiraConfig) {
        this.script = script
        this.jira = new RestClient(script, jiraConfig, '/rest/apwide/tem/1.1')
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

    def create(categoryName) {
        jira.post("/category", [
                name: categoryName
        ])
    }
}
