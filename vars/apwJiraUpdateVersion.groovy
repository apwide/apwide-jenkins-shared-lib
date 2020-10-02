import com.apwide.jenkins.jira.Version
import com.apwide.jenkins.util.ScriptWrapper

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { ScriptWrapper script, parameters ->
        return new Version(script, parameters).update(parameters.params.id, parameters.body)
    }
}