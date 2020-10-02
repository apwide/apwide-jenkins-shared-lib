import com.apwide.jenkins.golive.Applications
import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.ScriptWrapper

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { ScriptWrapper script, Parameters parameters ->
        return new Applications(script, parameters).delete(parameters.application)
    }
}