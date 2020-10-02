import com.apwide.jenkins.jira.Project
import com.apwide.jenkins.util.ScriptWrapper

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { ScriptWrapper script, parameters ->
        return new Project(script, parameters).get(parameters.params.id)
    }
}