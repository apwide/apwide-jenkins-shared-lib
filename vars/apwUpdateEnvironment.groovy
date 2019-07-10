import com.apwide.jenkins.golive.Environment
import com.apwide.jenkins.util.Parameters

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { script, Parameters parameters ->
        if (parameters.environmentId) {
            return new Environment(this, parameters.config).update(parameters.environmentId, parameters.body)
        } else {
            return new Environment(this, parameters.config).update(parameters.application, parameters.category, parameters.body)
        }
    }
}