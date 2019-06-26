import com.apwide.jenkins.jira.Version
import com.apwide.jenkins.util.Parameters

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { script, Parameters parameters ->
        return new Version(this, parameters.config).create(parameters.body)
    }
}