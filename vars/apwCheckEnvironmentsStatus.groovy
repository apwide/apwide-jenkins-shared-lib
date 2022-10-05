import com.apwide.jenkins.golive.Environment
import com.apwide.jenkins.golive.Environments
import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.ScriptWrapper

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { ScriptWrapper script, Parameters parameters ->

        def environmentClient = new Environment(script, parameters)
        def environmentsClient = new Environments(script, parameters)

        def environments
        if (parameters.params.criteria) {
            environments = environmentsClient.search(parameters.params.criteria as Map)
        } else {
            environments = environmentsClient.findAll application: parameters.application
        }

        script.debug "Environments json: ${environments.toString()}"

        for (environment in environments) {
            script.debug "Application : ${environment.application.name}"
            script.debug "Category: ${environment.category.name}"
            script.debug "Environment url: ${environment.url}"
            script.debug "Environment id: ${environment.id}"

            environmentClient.checkAndUpdateStatus(
                environment.id,
                parameters.unavailableStatus,
                parameters.availableStatus,
                parameters.dontTouchStatus,
                parameters.params.checkStatus)
        }
    }
}
