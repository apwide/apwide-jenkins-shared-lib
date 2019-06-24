import com.apwide.jenkins.golive.Environments
import com.apwide.jenkins.util.Parameters

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { script, Parameters parameters ->
        // take parameters from input params directly because we don't want to have global environment variable resolution here
        return new Environments(this, parameters.config).findAll(application: parameters.params.application, category: parameters.params.category)
    }
}