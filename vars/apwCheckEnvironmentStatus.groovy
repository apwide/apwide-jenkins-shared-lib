import com.apwide.jenkins.golive.Environment
import com.apwide.jenkins.util.Parameters

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { script, Parameters parameters ->
        return new Environment(this, parameters.config)
            .checkAndUpdateStatus(parameters.application, parameters.category, parameters.unavailableStatus, parameters.availableStatus,
                parameters.params.checkStatus)
    }
}