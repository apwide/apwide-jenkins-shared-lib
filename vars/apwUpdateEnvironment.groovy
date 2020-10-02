import com.apwide.jenkins.golive.Environment
import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.ScriptWrapper

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { ScriptWrapper script, Parameters parameters ->
        if (parameters.environmentId) {
            return new Environment(script, parameters).update(parameters.environmentId, parameters.body)
        } else {
            return new Environment(script, parameters).update(parameters.application, parameters.category, parameters.body)
        }
    }
}