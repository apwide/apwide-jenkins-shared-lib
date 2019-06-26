import com.apwide.jenkins.util.RestClient

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { script, parameters ->
        return new RestClient(this, parameters.config).request(parameters.httpMode, parameters.path, parameters.body)
    }
}