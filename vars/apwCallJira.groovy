import com.apwide.jenkins.util.RestClient
import com.apwide.jenkins.util.ScriptWrapper

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { ScriptWrapper script, parameters ->
        return new RestClient(script, parameters.config).request(parameters.httpMode, parameters.path, parameters.body)
    }
}