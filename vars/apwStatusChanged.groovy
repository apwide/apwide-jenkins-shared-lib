import com.apwide.jenkins.golive.Golive

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Closure body = null) {
    executeStep(this, body) { script, parameters ->
        return new Golive(this, parameters.config).updateEnvironmentStatus(parameters.params.application, parameters.params.category, parameters.params.status)
    }
}