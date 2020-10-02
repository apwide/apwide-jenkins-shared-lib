import com.apwide.jenkins.golive.Environments
import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.ScriptWrapper

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { ScriptWrapper script, Parameters parameters ->
        // take parameters from input params directly because we don't want to have global environment variable resolution here
        return new Environments(script, parameters).search(parameters.params.criteria as Map)
    }
}