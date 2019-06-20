import com.apwide.jenkins.jira.Project

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Closure body = null) {
    executeStep(this, body) { script, parameters ->
        return new Project(this, parameters.config).get(parameters.params.id)
    }
}