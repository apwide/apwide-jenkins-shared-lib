import com.apwide.jenkins.golive.Applications
import com.apwide.jenkins.util.Parameters

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { script, Parameters parameters ->
        return new Applications(this, parameters.config).delete(parameters.application)
    }
}