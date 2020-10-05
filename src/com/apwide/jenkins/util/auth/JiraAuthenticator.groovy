package com.apwide.jenkins.util.auth

import com.apwide.jenkins.util.Parameters
import com.apwide.jenkins.util.ScriptWrapper

class JiraAuthenticator implements Authenticator {

    final ScriptWrapper script
    final Parameters parameters

    JiraAuthenticator(ScriptWrapper script, Parameters parameters) {
        this.script = script
        this.parameters = parameters
    }

    @Override
    def authenticate(Closure request) {
        if (parameters.isJiraCloud()) {
            script.debug("Use Jira cloud authentication")
            def authContext = new AuthenticationContext(parameters.getJiraCloudCredentialsId(), null)
            return request(authContext)
        } else {
            script.debug("Use Jira server authentication")
            def authContext = new AuthenticationContext(parameters.getJiraCredentialsId(), null)
            return request(authContext)
        }
    }
}
