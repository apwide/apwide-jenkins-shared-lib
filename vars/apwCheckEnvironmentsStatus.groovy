import com.apwide.jenkins.golive.Environment
import com.apwide.jenkins.golive.Environments
import com.apwide.jenkins.util.Parameters

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { script, Parameters parameters ->

        def environmentClient = new Environment(this, parameters.config)
        def environmentsClient = new Environments(this, parameters.config)

        def environments
        if (parameters.params.criteria) {
            environments = environmentsClient.search(parameters.params.criteria as Map)
        } else {
            environments = environmentsClient.findAll application: parameters.application
        }

        echo "Environments json: ${environments.toString()}"

        for (environment in environments) {
            echo "Application : ${environment.application.name}"
            echo "Category: ${environment.category.name}"
            echo "Environment url: ${environment.url}"

            return environmentClient
                    .checkAndUpdateStatus(environment.application.name, environment.category.name, parameters.unavailableStatus, parameters.availableStatus)
        }
    }
}