package com.apwide.jenkins.golive

class Golive {
    final script
    final Categories categories
    final Environment environment
    final Environments environments

    Golive(Object script, Map jiraConfig) {
        this.script = script
        categories = new Categories(script, jiraConfig)
        environment = new Environment(script, jiraConfig)
        environments = new Environments(script, jiraConfig)
    }

    def createEnvironmentAndCategoryIfNotExist(applicationName, categoryName, permissionSchemeName, body = null) {
        def category = categories.get(categoryName)
        if (!category) {
            categories.create(categoryName)
        }
        def env = environments.search([
                applicationName: applicationName,
                categoryName: categoryName
        ])
        if (!env || env.isEmpty()) {
            return environment.create(applicationName, categoryName, permissionSchemeName, body)
        } else {
            return env[0]
        }
    }
}
