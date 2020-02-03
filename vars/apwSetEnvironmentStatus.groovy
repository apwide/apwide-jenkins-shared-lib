import com.apwide.jenkins.golive.Environment
import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.ScriptWrapper

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { ScriptWrapper script, Parameters parameters ->
        return new Environment(script, parameters.config).setStatus(parameters.application, parameters.category, parameters.params.status)
    }
}