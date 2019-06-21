import com.apwide.jenkins.golive.Golive
import com.apwide.jenkins.util.Parameters

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { script, Parameters parameters ->
        return new Golive(this, parameters.config).updateEnvironment(parameters.environmentId, parameters.body)
    }
}