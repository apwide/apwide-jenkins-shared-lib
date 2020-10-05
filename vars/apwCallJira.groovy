import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.RestClient
import com.apwide.jenkins.util.ScriptWrapper
import com.apwide.jenkins.util.auth.JiraAuthenticator

import static com.apwide.jenkins.util.Utilities.executeStep

def call(Map config = null) {
    executeStep(this, config) { ScriptWrapper script, Parameters parameters ->
        script.debug("Call jira")
        return new RestClient(script, parameters.getConfig(), new JiraAuthenticator(script, parameters), parameters.getJiraUrl())
                .request(parameters.httpMode, parameters.path, parameters.body)
    }
}