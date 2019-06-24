import com.apwide.jenkins.jira.Project
import com.apwide.jenkins.util.Parameters

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { script, Parameters parameters ->
        return new Project(this, parameters.config).versions(parameters.project)
    }
}