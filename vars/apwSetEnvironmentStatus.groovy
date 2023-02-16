import com.apwide.jenkins.golive.Environment
import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.ScriptWrapper

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { ScriptWrapper script, Parameters parameters ->

        def environmentClient = new Environment(script, parameters)
        def environmentId = parameters.getEnvironmentId() ?: environmentClient.get(parameters.getApplication(), parameters.getCategory()).id

        return environmentClient.setStatus(environmentId, parameters.params.status)
    }
}
