import com.apwide.jenkins.jira.Version

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { script, parameters ->
        return new Version(this, parameters.config).update(parameters.params.id, parameters.body)
    }
}